# Usage-Based Insurance (UBI) Platform — Architecture Design

## 1. Executive Summary & Problem Context

**Problem Statement:** PS-008 (Usage-Based Insurance — InsurTech & IoT Telematics)  
**Objective:** Transform traditional static auto insurance pricing into dynamic, risk-aligned actuarial premiums driven by real-world driving behavior, distance traveled, and verified vehicle profiles.

Traditional auto insurance relies on static demographic indicators (age, zip code, vehicle make/year) that fail to reward safe drivers or accurately price high-risk behavior. This platform introduces a fair, transparent, and data-driven insurance model powered by telematics.

---

## 2. High-Level Telematics & Pricing Pipeline

The platform follows a unidirectional, decoupled processing pipeline:

```
[ Vehicle Registration ]
         │
         ▼
[ Vehicle Verification (Authorized Integration / Mock) ]
         │
         ▼
[ Telematics Data Ingestion (GPS, Accelerometer, Speed, OBD-II) ]
         │
         ▼
[ Telematics Validation & Cleaning (Spike filters, schema check) ]
         │
         ▼
[ Trip Stitching & Geospatial Context ]
         │
         ▼
[ Feature Engineering (Harsh brakes, rapid accels, night driving, distance) ]
         │
         ▼
[ Risk Scoring Engine (Safety Score: 0 - 100) ]
         │
         ▼
[ Actuarial Pricing Engine (Base premium × Risk Multiplier + Distance charge) ]
         │
         ▼
[ Explanation Engine (Human-readable driver breakdown & premium factors) ]
         │
         ▼
[ React Dashboards (Driver Portal & Insurer Actuarial Portal) ]
```

---

## 3. Core Architectural Modules & Responsibilities

To maintain strict separation of concerns, the backend is organized into distinct domain modules within a modular monolith:

| Module | Core Responsibility | Planned Future Inputs & Outputs |
| :--- | :--- | :--- |
| **1. Vehicle Registration & Verification** | Manage policyholder vehicles, validate VINs, engine specs, and vehicle classifications. | **In:** VIN, Make, Model, Year<br>**Out:** Verified Vehicle Profile |
| **2. Telemetry Ingestion** | Ingest batched or streaming telematics payloads containing timestamps, coordinates, speeds, and acceleration vectors. | **In:** Raw JSON/Protobuf telemetry points<br>**Out:** Ingestion event receipts |
| **3. Data Validation & Cleaning** | Filter out sensor anomalies (GPS drift, impossible speeds, clock jumps, duplicate pings). | **In:** Raw telemetry stream<br>**Out:** Cleaned, calibrated telemetry sequence |
| **4. Trip Stitching** | Segment continuous GPS/speed pings into distinct trips based on ignition on/off and idle timeouts. | **In:** Time-series telemetry points<br>**Out:** Structured Trip Sessions (Start, End, Distance, Duration) |
| **5. Feature Engineering** | Extract statistical and behavioral risk features (harsh braking events, sudden accelerations, cornering G-force, night driving ratio, peak-hour exposure). | **In:** Trip sessions<br>**Out:** Behavioral Feature Vector |
| **6. Risk Scoring Engine** | Evaluate driver risk and compute a normalized **Safety Score** ($0 - 100$). Completely decoupled from monetary pricing rules. | **In:** Feature Vector<br>**Out:** Normalized Safety Score & Risk Tier |
| **7. Actuarial Pricing Engine** | Apply insurer-approved underwriting rules, regulatory boundaries, base rates, and telematics discounts to estimate monthly/annual premium. | **In:** Safety Score, Base Rate, Mileage<br>**Out:** Itemized Estimated Premium |
| **8. Explanation Engine** | Generate transparent, human-understandable explanations showing why a score was awarded and how specific behaviors affected the premium. | **In:** Safety Score breakdown, Pricing Factors<br>**Out:** Explanatory Insights & Actionable Tips |

---

## 4. Software Architecture & Design Patterns

### Architecture Style: Modular Monolith
- Built as a single cohesive Spring Boot application for rapid development, zero deployment friction during the prototype phase, and immediate local execution.
- High cohesion and loose coupling between packages ensure that modules can later be split into standalone microservices or serverless functions if scale demands it.
- **No premature complexity:** Distributed message brokers (Kafka, MQTT), distributed stream processing (Flink, Spark), and container orchestrators (Kubernetes) are deliberately deferred to later development phases once the core business logic is validated.

### Layered Application Structure
```
Frontend (React 18 / Vite / Vanilla CSS)
       │ HTTP / JSON REST APIs (CORS Controlled)
       ▼
Controllers (REST API Endpoints, Input Validation, DTO mapping)
       │
Services (Business logic, orchestration, feature calculation)
       │
Repositories (Spring Data JPA, Hibernate, JPQL / Native queries)
       │ JDBC / Connection Pool (HikariCP)
       ▼
Database (MySQL 8+ Relational Schema)
```

---

## 5. Authentication, Authorization & Security Architecture (Step 8)

### 5.1 Architecture Style & Session Management
- **Framework:** Spring Security 6 integration within Spring Boot 3.3.4.
- **Session-Based Authentication:** Employs secure, server-managed HTTP sessions (`JSESSIONID`) with `HttpOnly`, `SameSite=Lax`, and `Secure` (in HTTPS environments) attributes.
- **Stateless Tokens vs Sessions:** Server-managed sessions were selected for the React SPA to eliminate client-side token storage vulnerabilities (such as tokens in `localStorage` vulnerable to XSS).
- **CSRF Protection:** Configured with `CookieCsrfTokenRepository.withHttpOnlyFalse()` supporting Single Page Applications. The backend issues an `XSRF-TOKEN` cookie, and mutating frontend requests supply the matching `X-XSRF-TOKEN` header. An explicit `/api/v1/auth/csrf` token warmup endpoint is also provided.
- **CORS Policy:** Strict origin whitelisting (`http://localhost:5173`) with credentials allowed (`allowCredentials=true`). Arbitrary wildcards with credentials are systematically rejected.

### 5.2 Role-Based Access Control (RBAC)
Two primary roles are enforced:
1. `ROLE_CUSTOMER`:
   - Can register public accounts (`POST /api/v1/auth/register`).
   - Strictly isolated to accessing only their own registered vehicles, trips, telematics, risk scores, pricing quotes, and scorecards.
   - Any attempt to access, ingest telemetry for, or trigger calculations on another customer's vehicle returns `404 Not Found` (`RESOURCE_NOT_FOUND`) to prevent enumeration attacks.
   - Forbidden (`403 Forbidden`) from accessing insurer analytics or administrative fleet endpoints.
2. `ROLE_INSURER`:
   - Restricted to underwriter and actuarial personnel.
   - Full read and analytics access to fleet-wide underwriting distribution, risk tier statistics, and pricing aggregates (`/api/v1/dashboard/insurer/**`).
   - Account assignment is strictly controlled via environment-configured bootstrap (`InsurerBootstrapService`) or administrative channels—public registration of `INSURER` accounts is permanently blocked at the service boundary.

### 5.3 Customer Data Ownership Enforcement Model
Ownership is enforced at both controller and service boundaries through `OwnershipService`:
- Every `Vehicle` entity maintains a direct `@ManyToOne` relationship with `User owner` (`idx_vehicles_owner`).
- Operations verifying vehicle or trip access (`OwnershipService.verifyVehicleAccess`, `verifyTripAccess`):
  - If principal has `ROLE_INSURER`: access is permitted for fleet oversight.
  - If principal has `ROLE_CUSTOMER`: the entity's `owner.id` is compared against `principal.getUserId()`.
  - If a customer attempts to query or mutate a vehicle owned by another user, the service immediately throws `ResourceNotFoundException("Vehicle not found with ID: " + id)`. Returning `404` rather than `403` ensures an attacker cannot determine whether an arbitrary vehicle ID exists in the system.
- Telemetry ingestion, route simulation, trip stitching, feature engineering, risk scoring, and quote generation all pass through this ownership boundary.

### 5.4 Account Lifecycle & Password Security
- **BCrypt Hashing:** Passwords are never stored in plaintext and are hashed using Spring Security's `BCryptPasswordEncoder` (10 rounds).
- **Email Normalization:** All emails are trimmed and converted to lowercase before database persistence and lookup.
- **Credential Protection:** `UserResponse` DTO deliberately excludes password hash fields; passwords and session identifiers are never output to application logs.
- **Account Status:** Enforces `AccountStatus` (`ACTIVE`, `SUSPENDED`, `LOCKED`).

### 5.5 Insurer Bootstrap & Legacy Record Handling
- `InsurerBootstrapService` runs on application startup (`ApplicationRunner`).
- If `app.security.bootstrap-insurer=true`, it checks if an insurer account exists. If not, it safely creates an initial account using configured environment variables (`app.security.insurer-email`, `app.security.insurer-password`).
- For legacy unowned vehicle records from Steps 1–7, the bootstrapper automatically assigns them to an initialized demo customer (`customer@trustdrive.internal`), preventing unowned orphaned records while maintaining backwards compatibility with existing test seeds.

---

## 6. Trip Stitching & Feature Engineering Specifications (Step 4)

### Trip Identification Rules & Boundary Logic
- **Segmentation by Inactivity:** Consecutive valid telemetry points belong to the same journey while the time gap $\Delta t \le \text{inactivityThreshold}$ (default 10 minutes, configurable via `app.trips.inactivity-threshold-minutes`).
- **In-Progress vs. Completed:** If the newest record cluster is within 10 minutes of current time, the trip is marked `IN_PROGRESS` with null `endTime`. Once inactivity exceeds the threshold, it is automatically finalized as `COMPLETED`.
- **Idempotency:** Re-running trip stitching deletes prior vehicle trip sessions and re-segments raw points deterministically without duplicate inserts.

### Haversine Distance & Jump Filtering
- Great-circle distance calculated via spherical trigonometry ($R = 6,371,000\text{ m}$).
- **Jump Safeguard:** If segment displacement implies an impossible velocity ($> 220\text{ km/h} = 61.1\text{ m/s}$) or $> 25\text{ m}$ instantaneous jump in $0\text{ s}$, the jump is flagged and omitted from mileage accumulation to prevent GPS multipath noise from corrupting distance.

### Debounced Event Counters & Normalization
- **Hysteresis Debounce:** A continuous 3-second harsh braking event (accel $\le -0.35\text{G}$) spanning multiple consecutive 1s/5s pings is counted as **1 distinct event** rather than inflating the count per ping. The same debouncing applies to rapid acceleration ($\ge +0.30\text{G}$).
- **Safe Normalization:** Normalized event rates per 100 km are computed as $\frac{\text{events}}{\text{distanceKm}} \times 100$. If trip distance $< 0.1\text{ km}$, rates evaluate to `null` to avoid division by zero.

### Night Driving Exposure
- Night window: 22:00 to 05:00 local time in configured timezone (`app.features.timezone=Asia/Kolkata`).
- Only valid driving intervals $\le 10\text{ minutes}$ are accumulated, preventing long parking gaps from being mistakenly counted as night driving.

---

## 7. Behavioral Risk Scoring Engine (Step 5)

### Scoring Philosophy & Architecture
- **Higher Score = Safer Driver**: Score runs from 0.0 to 100.0, where 100 indicates perfect compliance and zero observed behavioral risk factors.
- **Strict Decoupling**: Strictly rule-based and deterministic. Insurance policy pricing, vehicle valuation, engine capacity, and actuarial rating multipliers are kept separate (Step 6).
- **Explainability**: Every deduction has a recorded factor name, observed metric, deduction value, and plain-English explanation.

### Mathematical Formula
$$\text{Safety Score} = \text{clamp}\left(100.0 - \sum \text{Penalties}, 0.0, 100.0\right)$$

Where penalties are calculated as:
1. **Harsh Braking Penalty**:
   $$\text{Penalty}_{\text{HB}} = \min\left(\text{harshBrakeRatePer100Km} \times 2.0, 30.0\right)$$
2. **Rapid Acceleration Penalty**:
   $$\text{Penalty}_{\text{RA}} = \min\left(\text{rapidAccelRatePer100Km} \times 1.5, 25.0\right)$$
3. **Speeding Penalty**:
   $$\text{Base} = \left(\frac{\text{speedingCount}}{\text{distanceKm}} \times 100\right) \times 2.5$$
   $$\text{Severity} = \max\left(0, \text{maxSpeedKmh} - 130.0\right) \times 0.5$$
   $$\text{Penalty}_{\text{Speed}} = \min\left(\text{Base} + \text{Severity}, 35.0\right)$$
4. **Night Driving Penalty**:
   $$\text{Penalty}_{\text{Night}} = \min\left(\text{nightDrivingRatio} \times 5.0, 5.0\right)$$

### Score Categorization
- **85.0 – 100.0**: `EXCELLENT` (Consistently smooth control, adherence to limits, low collision hazard)
- **70.0 – 84.9**: `GOOD` (Normal safe driving with minor isolated infractions)
- **50.0 – 69.9**: `MODERATE` (Frequent harsh maneuvers or speeding; moderate risk)
- **0.0 – 49.9**: `HIGH_RISK` (Aggressive deceleration, excessive throttle, or sustained high speed)
- **`null`**: `UNRATED` (Assigned when statistical evidence is insufficient)

### Data Quality & Insufficient Observations Handling
Missing or corrupted data is **never** penalized as risky driving:
- **Ongoing Journeys (`IN_PROGRESS_TRIP`)**: Returns `score = null`, `isScorable = false`.
- **Minimum Distance (`INSUFFICIENT_DISTANCE`)**: Distance $< 0.5\text{ km}$ returns `score = null`.
- **Minimum Telemetry (`INSUFFICIENT_TELEMETRY`)**: Valid pings $< 10$ returns `score = null`.
- **Minimum Duration (`INSUFFICIENT_DURATION`)**: Journey $< 60\text{ s}$ returns `score = null`.
- **High Anomaly Proportion (`HIGH_ANOMALY_RATE`)**: Data quality score $< 50\%$ returns `score = null`.

> [!WARNING]
> **Prototype Heuristic Notice**: The weights and penalty caps in this scoring engine are illustrative heuristic assumptions developed for hackathon problem statement PS-008. They are not actuarially validated loss-distribution models and must not be used as production underwriting or pricing guidance.

---

## 8. Actuarial Pricing Engine Specifications (Step 6)

### Pricing Formula Architecture
The actuarial engine translates eligible driving behavior risk scores and vehicle mileage exposure into an explainable annual premium estimate in INR (₹):

$$\text{Estimated Annual Premium} = \text{clamp}\left(\text{Base Annual Premium} \times M_{\text{risk}} \times M_{\text{usage}}, \text{Min Premium}, \text{Max Premium}\right)$$

Where:
- **Base Annual Premium:** Configurable baseline tariff for standard passenger vehicles (`₹12,000.00 INR`).
- **$M_{\text{risk}}$ (Risk Multiplier):** Derived from the vehicle's distance-weighted aggregate safety score.
- **$M_{\text{usage}}$ (Usage Multiplier):** Derived from projected or user-supplied annual distance road exposure.
- **Bounds:** Minimum floor (`₹5,000.00 INR`) and maximum ceiling (`₹35,000.00 INR`).

### Risk Multiplier Schedule (Illustrative Heuristic)
| Aggregate Safety Score | Risk Category | Multiplier ($M_{\text{risk}}$) | Actuarial Rationale |
| :--- | :--- | :---: | :--- |
| **85.0 – 100.0** | `EXCELLENT` | **`0.9000`** | 10.0% safe-driver discount for disciplined driving |
| **70.0 – 84.9** | `GOOD` | **`1.0000`** | Standard actuarial baseline tariff |
| **50.0 – 69.9** | `MODERATE` | **`1.1500`** | 15.0% risk surcharge for frequent harsh maneuvers |
| **0.0 – 49.9** | `HIGH_RISK` | **`1.3000`** | 30.0% high-risk surcharge for severe aggressive driving |
| **`null` / Unscorable** | `UNRATED` | **Ineligible** | Never mapped to safe category; produces `INSUFFICIENT_RISK_DATA` |

### Usage Multiplier Schedule (Annual Mileage Road Exposure)
| Projected Annual Mileage | Usage Multiplier ($M_{\text{usage}}$) | Exposure Classification |
| :--- | :---: | :--- |
| **$< 5,000\text{ km}$** | **`0.8500`** | Low road exposure discount (15.0% reduction) |
| **$5,000\text{ to } 7,999\text{ km}$** | **`0.9200`** | Below-average road exposure (8.0% reduction) |
| **$8,000\text{ to } 12,000\text{ km}$** | **`1.0000`** | Standard average passenger vehicle baseline |
| **$12,001\text{ to } 18,000\text{ km}$** | **`1.1000`** | Above-average road exposure (10.0% surcharge) |
| **$> 18,000\text{ km}$** | **`1.2500`** | High road exposure surcharge (25.0% surcharge) |

### Telemetry Annualization Safeguards
1. **Minimum Projection Threshold:** Telemetry is annualized via $\left(\frac{\text{observedKm}}{\text{observationDays}}\right) \times 365.25$ **only** when observed mileage $\ge 20.0\text{ km}$ and observation span $\ge 7\text{ days}$.
2. **Short Telemetry Handling:** If telemetry is below the threshold and no manual assumption is provided, the quote returns status `INSUFFICIENT_USAGE_DATA` with `finalPremium = null`. It **never** silently assumes zero mileage.
3. **User Overrides:** Users or actuaries can explicitly provide an `annualDistanceKmAssumption`, clearly tagged as `USER_SPECIFIED`.

### Monetary Precision & Reproducibility
- All calculations use `java.math.BigDecimal` with `RoundingMode.HALF_UP` scaled to 2 decimal places.
- Every quote is saved as an immutable snapshot in `premium_quotes` containing formula version `pricing-v1`, calculation timestamp, exact input values, multipliers, and factor explanations JSON.

> [!WARNING]
> **Prototype Actuarial Notice**: This is a hackathon demonstration model developed for Problem Statement PS-008. The formula and multipliers are illustrative prototype assumptions and are not actuarially certified, regulator-approved, or legally binding insurance offers.

---

## 8. Customer Driver Scorecards & Insurer Dashboards Architecture (Step 7)

### Separation of Concerns: Customer vs. Insurer Views

The dashboard architecture provides two distinct, purpose-built interfaces sharing a unified backend aggregation engine:

1. **Customer Driver Scorecard (`/api/v1/dashboard/customer/...`)**:
   - Focuses on individual policyholder transparency, driving performance feedback, and behavioral insights.
   - Never invents missing data; explicitly presents `UNRATED` or `NOT_AVAILABLE` when observations are insufficient.
   - Discloses individual penalty points and plain-English factor explanations grounded in actual telematics records.
   - Provides paginated trip drill-downs to inspect event counts, duration, and data quality per journey.

2. **Prototype Insurer Analytics (`/api/v1/dashboard/insurer/...`)**:
   - Focuses on macro portfolio risk distributions, fleet-wide usage coverage, and aggregate actuarial metrics.
   - Strictly operates on real database records via JPQL aggregations—zero hardcoded mock chart values.
   - Employs distance-weighted rate calculations ($\frac{\sum \text{events}}{\sum \text{distance}} \times 100$) rather than naive averages of normalized trip rates.
   - Differentiates between trip counts and vehicle counts to eliminate double-counting in multi-trip fleets.
   - Computes robust portfolio pricing metrics including both mean and median illustrative premiums.

```
                    ┌──────────────────────────────┐
                    │    Dashboard Aggregation     │
                    │   Service & REST Endpoints   │
                    └──────────────┬───────────────┘
                                   │
              ┌────────────────────┴────────────────────┐
              ▼                                         ▼
┌───────────────────────────────┐       ┌───────────────────────────────┐
│   Customer Driver Scorecard   │       │  Prototype Insurer Analytics  │
├───────────────────────────────┤       ├───────────────────────────────┤
│ • Vehicle Mock Verification   │       │ • Fleet Macro Metrics         │
│ • Safety Score (0-100/Unrated)│       │ • Trip vs. Vehicle Risk Dists │
│ • Distance-Weighted Rates     │       │ • Weighted Event Rates/100km  │
│ • Factor Breakdown Penalties  │       │ • Telematics Anomaly Rate (%) │
│ • Chronological Trendline     │       │ • Mileage Coverage Breakdown  │
│ • Paginated Trip Drill-Down   │       │ • Mean & Median Premiums (₹)  │
│ • Latest Illustrative Quote   │       │ • Pricing Model Versions      │
└───────────────────────────────┘       └───────────────────────────────┘
```

### Insurer Access Boundary & Privacy Notice
- **Prototype Status:** Clearly labeled as a *Simulated Insurer Demonstration*. Since this hackathon implementation does not include production role-based access control (RBAC), the limitation is transparently stated rather than claiming simulated access security.
- **Privacy Safeguards:** Aggregate insurer views omit driver personal identifiable information (PII) and raw GPS coordinates, displaying only anonymized counts, mileage buckets, and portfolio distributions.
- **Non-Automatic Premium Alteration:** Dashboard loads are strictly read-only; viewing analytics does not recalculate or modify existing quotes, and does not alter insurance policies.

