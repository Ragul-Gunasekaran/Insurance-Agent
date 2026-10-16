# Usage-Based Insurance (UBI) Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java 21 LTS](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-purple.svg)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)

---

## 1. Project Overview

The **Usage-Based Insurance (UBI) Platform** is a full-stack, production-oriented prototype designed to modernize automotive insurance. Instead of relying purely on static demographics (such as age, gender, and home zip code), this platform leverages IoT telematics, driving behavior metrics, and transparent actuarial models to compute personalized insurance premiums.

---

## 2. Problem Statement (PS-008)

> **PS-008: Usage-Based Insurance (InsurTech & IoT Telematics)**  
> Develop an intelligent, fair, and transparent usage-based insurance engine that ingests vehicle and telematics data, evaluates real-world driving risks (distance, harsh braking, rapid acceleration, speeding), calculates a safety score, applies insurer-approved pricing rules, and delivers transparent, explainable premium estimates to consumers and actuaries.

---

## 3. Technology Stack

### Frontend
- **Framework:** React 18
- **Build Tool / Bundler:** Vite 5
- **Language:** Modern JavaScript (ES Modules)
- **Styling:** Custom Vanilla CSS Design System (dark-mode friendly, glassmorphic cards, responsive grid)
- **HTTP Client:** Native Fetch API with error boundary handling

### Backend
- **Framework:** Spring Boot 3.3.4
- **Language:** Java 21 LTS
- **Build & Dependency Management:** Apache Maven / Maven Wrapper
- **Data Persistence:** Spring Data JPA / Hibernate
- **Database Driver:** MySQL Connector/J (`com.mysql:mysql-connector-j`)
- **Validation:** Spring Boot Validation (`jakarta.validation`)
- **Monitoring & Metrics:** Spring Boot Actuator

### Database
- **Engine:** MySQL 8.0+ (or wire-compatible MariaDB)
- **Configuration:** Fully environment-driven via `.env` / system variables (no hardcoded credentials)

---

## 4. Architecture Workflow

```
Vehicle Registration
  └── Vehicle Verification (VIN / Specs check)
        └── Telematics Data Ingestion (GPS, Speed, G-Force)
              └── Telematics Validation & Cleaning
                    └── Trip Stitching & Route Context
                          └── Feature Engineering (Brakes, Accels, Mileage)
                                └── Risk Scoring Engine (Safety Score: 0 - 100)
                                      └── Actuarial Pricing Engine (Premium Calc)
                                            └── Explanation Engine (Factor Breakdown)
                                                  └── Customer & Insurer Dashboards
```

---

## 5. Folder Structure

```
usage-based-insurance/
├── .gitignore                     # Root gitignore (protects secrets and builds)
├── .env.example                   # Root environment variable template
├── README.md                      # Comprehensive project documentation
├── docs/
│   ├── architecture.md            # System architecture and domain breakdown
│   ├── setup.md                   # Step-by-step setup guide (Windows & Linux)
│   └── api-overview.md            # Foundational API specification
├── backend/
│   ├── .env.example               # Backend environment template
│   ├── pom.xml                    # Maven build file with Spring Boot dependencies
│   ├── mvnw / mvnw.cmd            # Maven Wrapper executables
│   ├── .mvn/wrapper/              # Maven Wrapper properties
│   └── src/
│       ├── main/
│       │   ├── java/com/ubi/platform/
│       │   │   ├── UsageBasedInsuranceApplication.java
│       │   │   ├── config/        # CORS and Database configuration
│       │   │   ├── controller/    # REST API endpoints (Health, Info)
│       │   │   ├── service/       # Service layer logic
│       │   │   ├── repository/    # Spring Data JPA repositories (planned)
│       │   │   ├── entity/        # Database JPA entities (planned)
│       │   │   ├── dto/           # Standardized API response DTOs
│       │   │   ├── exception/     # Centralized global exception handler
│       │   │   ├── validation/    # Custom input validators (planned)
│       │   │   ├── telemetry/     # IoT telemetry ingestion (planned)
│       │   │   ├── scoring/       # Behavioral safety scoring (planned)
│       │   │   ├── pricing/       # Actuarial pricing engine (planned)
│       │   │   └── explanation/   # Premium transparency engine (planned)
│       │   └── resources/
│       │       └── application.properties # Environment-driven properties
│       └── test/                  # Unit and integration test suites
└── frontend/
    ├── .env.example               # Frontend environment template
    ├── index.html                 # Single page application entry
    ├── package.json               # Frontend dependencies and npm scripts
    ├── vite.config.js             # Vite configuration with proxy support
    └── src/
        ├── main.jsx               # React DOM root
        ├── App.jsx                # Application root container
        ├── index.css              # Vanilla CSS design tokens & theme
        ├── components/            # Reusable UI cards, navigation, badges
        ├── pages/                 # Landing and dashboard views
        ├── services/              # API communication layer (Fetch API)
        └── hooks/                 # Custom React hooks (useBackendStatus)
```

---

## 6. Prerequisites

Ensure you have the following installed on your machine:
- **Java Development Kit (JDK):** Version 21 LTS (`C:\Program Files\Java\jdk-21.0.10`)
- **Node.js:** Version 18+ (Detected: v24.18.0)
- **npm:** Version 9+ (Detected: 11.16.0)
- **MySQL Server:** Version 8.0+ or MariaDB

---

## 7. MySQL Setup Instructions

1. Start your local MySQL service (e.g., via MySQL Community Server or XAMPP).
2. Connect to MySQL and create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS usage_based_insurance
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_unicode_ci;
   ```
3. Set your database credentials in `backend/.env` (or pass as environment variables).

---

## 8. Backend Installation and Startup

```powershell
# In PowerShell:
cd d:\Trust_Drive\usage-based-insurance\backend

# Set Java 21 LTS
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Build the application
.\mvnw.cmd clean package -DskipTests

# Start the Spring Boot server
.\mvnw.cmd spring-boot:run
```

The backend server listens on `http://localhost:8080`.

---

## 9. Frontend Installation and Startup

```powershell
# In a separate PowerShell window:
cd d:\Trust_Drive\usage-based-insurance\frontend

# Install node dependencies
npm install

# Start the Vite development server
npm run dev
```

The frontend application opens at `http://localhost:5173`.

---

## 10. Environment Configuration

### Backend Environment Variables (`backend/.env`):
| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | HTTP port for Spring Boot |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `usage_based_insurance` | Target database schema |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | *(empty)* | Database password |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origin |

### Frontend Environment Variables (`frontend/.env`):
| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` | Backend REST API root URL |

---

## 11. API Endpoints and Example Responses

### Health Check Endpoint
`GET http://localhost:8080/api/v1/health`
```json
{
  "success": true,
  "timestamp": "2026-10-15T14:35:00.123Z",
  "data": {
    "status": "UP",
    "application": "usage-based-insurance-backend",
    "version": "1.0.0-SNAPSHOT",
    "database": {
      "status": "CONNECTED",
      "engine": "MySQL"
    }
  }
}
```

### Application Info Endpoint
`GET http://localhost:8080/api/v1/info`
```json
{
  "success": true,
  "timestamp": "2026-10-15T14:35:00.123Z",
  "data": {
    "name": "Usage-Based Insurance Platform",
    "problemStatement": "PS-008: InsurTech & IoT Telematics",
    "version": "1.0.0-SNAPSHOT",
    "plannedModules": ["vehicle-registration", "telemetry-ingestion", "risk-scoring", "actuarial-pricing"]
  }
}
```

---

## 12. Troubleshooting Instructions

- **Port Conflict (8080 or 5173 in use):** Change `SERVER_PORT` in `backend/.env` or run `npm run dev -- --port 5174` in frontend.
- **Database Connection Refused:** Ensure MySQL server is started on port 3306. Note that the Spring Boot application will still start and gracefully report database offline status in `/api/v1/health`.
- **CORS Blocked in Browser:** Ensure your frontend URL matches `CORS_ALLOWED_ORIGINS` in the backend configuration.

---

## 13. Implementation Roadmap

- [x] **Step 1:** Foundation Setup & Architecture Design (Modular monolith, CORS, MySQL pool resiliency, health checks).
- [x] **Step 2:** Vehicle Registration & Verification Module (Vehicle JPA entities, duplicate protection, mock national verification, React fleet management UI).
- [x] **Step 3:** IoT Telematics Data Ingestion & Calibration (GPS boundaries, speed anomaly filters, traction limits, route simulator, live stream audit UI).
- [x] **Step 4:** Trip Stitching & Feature Engineering Engine (Session segmentation by inactivity threshold, Haversine distance with GPS jump filtering, debounced harsh event counting, night driving exposure in local timezone, and trip insights UI).
- [x] **Step 5:** Behavioral Risk Scoring Engine (Deterministic 0–100 safety score, harsh braking / rapid accel / speeding / night driving deductions, penalty caps, data quality safeguards, explainability vectors, and Risk Scoring UI dashboard).
- [x] **Step 6:** Actuarial Pricing & Explanation Engine (Illustrative UBI premium calculation in INR ₹, base tariff, risk multiplier, usage multiplier, floor/ceiling bounds, immutable quote snapshots, and Pricing & Quotes UI dashboard).
- [x] **Step 7:** Customer Driver Scorecards & Insurer Dashboards (Driver safety scorecards, distance-weighted rates, paginated trip table with feature/penalty drill-down, chronological score progression, and Prototype Insurer Analytics with fleet macro metrics, trip/vehicle risk distributions, mileage coverage, and mean/median illustrative premium analytics).
- [x] **Step 8:** Authentication, Authorization & Security Hardening (Spring Security 6 session-based authentication, BCrypt password hashing, customer data ownership enforcement with 404 cross-owner isolation, role-based access control protecting insurer analytics with 403 Forbidden, CSRF SPA protection with HttpOnly session cookies, initial insurer bootstrap, and authenticated React experience with demo autofill modals).
- [x] **Step 9:** Final Integration, End-to-End Validation & Hackathon Readiness (153 automated tests passing, end-to-end customer 12-step workflow validation, insurer analytics isolation, data quality safeguards, production build validation, and hackathon presentation checklist).

---

## 14. Authentication & Security

The application enforces genuine authentication and server-side role-based access control (RBAC).

### Security Model
- **Roles:**
  - `CUSTOMER`: Access restricted strictly to own vehicles, trips, telematics, risk scores, pricing quotes, and scorecards. Any attempt to access or mutate another customer's vehicle returns `404 Not Found` to prevent resource enumeration.
  - `INSURER`: Authorized to inspect fleet-wide underwriting distribution, risk tier statistics, and pricing aggregates (`/api/v1/dashboard/insurer/**`).
- **Session Architecture:** Secure server-managed sessions with `HttpOnly`, `SameSite=Lax` cookies, and CSRF protection via `CookieCsrfTokenRepository` (`X-XSRF-TOKEN`).
- **Zero Hardcoded Passwords:** Insurer initialization is managed via environment-driven bootstrap (`InsurerBootstrapService`).

### Pre-Configured Demo Accounts (Development & Hackathon)
| Role | Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@trustdrive.internal` | `CustomerSecure2026!` | Manage own vehicles, ingest telemetry, run trip stitching, view private driver scorecard and quotes. |
| **Insurer** | `insurer@trustdrive.internal` | `InsurerSecure2026!` | Inspect fleet-wide actuarial analytics, risk tier distributions, and underwriter metrics. |

### Running the Automated Test Suite (153 Tests)
```powershell
cd d:\Trust_Drive\usage-based-insurance\backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd test
```
*Current test suite status: 153 passed, 0 failures, 0 errors.*

---

## 15. Hackathon Demonstration Checklist

Follow this sequential workflow to showcase the end-to-end platform during the hackathon presentation:

1. **Customer Registration & Login**:
   - Open [http://localhost:5173](http://localhost:5173).
   - Click **Sign In** and use the **"Demo Customer"** quick-fill button (`customer@trustdrive.internal` / `CustomerSecure2026!`).
   - Notice the status pill updates to show authenticated Customer session and email.
2. **Vehicle Onboarding (Step 2)**:
   - Navigate to the **Vehicles** tab.
   - Onboard a vehicle (e.g., `MH02AB1234`, Tata Nexon, 2023).
   - Click **Run Verification** to show simulated registry checks and `VERIFIED` status.
3. **IoT Telematics Ingestion & Simulator (Step 3)**:
   - Navigate to the **IoT Telematics** tab.
   - Select the onboarded vehicle and choose behavioral route profile `SAFE_COMMUTE` or `AGGRESSIVE_DRIVING`.
   - Click **Simulate & Ingest Telemetry** to stream GPS, speed, and G-force points with real-time anomaly detection.
4. **Trip Stitching & Feature Engineering (Step 4)**:
   - Navigate to the **Trips** tab.
   - Click **Stitch Inactive Journeys** to segment GPS points into distinct trip sessions.
   - Click **Compute Behavioral Features** to extract debounced harsh braking, rapid acceleration, speeding, and night exposure.
5. **Behavioral Risk Scoring (Step 5)**:
   - Navigate to the **Risk Scores** tab.
   - Click **Calculate Safety Score** to observe the explainable 0–100 behavioral safety score and itemized factor penalties.
6. **Actuarial Pricing Engine (Step 6)**:
   - Navigate to the **Pricing** tab.
   - Enter an annual mileage estimate (e.g., 10,000 km) and click **Calculate Policy Quote**.
   - Review the itemized premium breakdown in INR (₹) showing safe driver discounts and distance multipliers.
7. **Customer Driver Scorecard (Step 7)**:
   - Navigate to the **Scorecard** tab.
   - Inspect the policyholder's personalized driver dashboard, historical score progression, and trip-level penalty drilldowns.
8. **Insurer Underwriter Analytics & RBAC Isolation (Step 7 & 8)**:
   - Attempt to click **Insurer** tab while logged in as Customer: observe the `403 Forbidden` access notice.
   - Click **Sign Out**, then sign in with the **"Demo Insurer"** quick-fill button (`insurer@trustdrive.internal` / `InsurerSecure2026!`).
   - Open **Insurer** tab to inspect macro fleet metrics, risk tier distribution, driving behavior rates, and underwriting premium aggregates.

---

## 16. Prototype Limitations & Disclaimers

1. **Illustrative Actuarial Calculations:** All computed insurance premiums and safety scores are illustrative estimates generated for hackathon demonstration. They do not constitute certified insurance policies, underwriting commitments, or regulated financial products.
2. **Simulated Hardware Telematics:** Telemetry streams are produced via calibrated physical simulation models (or direct JSON ingestion) rather than physical OBD-II / CAN-bus dongles.
3. **Mock Vehicle Registry:** Verification utilizes deterministic mock lookup algorithms rather than live Indian government mParivahan / VAHAN API integrations.
4. **Single-Tenant Insurer Mode:** The prototype enforces role-based access separating customers from insurer underwriters; multi-tenant multi-carrier data isolation is not implemented.

