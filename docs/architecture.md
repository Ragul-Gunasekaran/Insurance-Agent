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

## 5. Security & Configuration Architecture

1. **Defense in Depth**:
   - Environment variables isolate credentials from source code (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`).
   - CORS is explicitly restricted to authorized local frontend origins (e.g., `http://localhost:5173`).
2. **Graceful Connection Handling**:
   - HikariCP connection pool is configured with non-blocking initialization fail timeouts, allowing the application to start and expose diagnostic health information even before the database is spun up.
3. **Centralized Exception Handling**:
   - Global `@RestControllerAdvice` intercepts unhandled exceptions, sanitizing error output and preventing internal stack traces or database connection strings from leaking to clients.
