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

## 13. Future Implementation Roadmap

- [x] **Step 1:** Foundation Setup & Architecture Design (Modular monolith, CORS, MySQL pool resiliency, health checks).
- [x] **Step 2:** Vehicle Registration & Verification Module (Vehicle JPA entities, duplicate protection, mock national verification, React fleet management UI).
- [x] **Step 3:** IoT Telematics Data Ingestion & Calibration (GPS boundaries, speed anomaly filters, traction limits, route simulator, live stream audit UI).
- [ ] **Step 4:** Trip Stitching & Feature Engineering (Session segmentation, harsh brake/accel detection, distance aggregation).
- [ ] **Step 5:** Risk Scoring Engine (Safety score algorithm 0-100, weighting matrix).
- [ ] **Step 6:** Actuarial Pricing & Explanation Engine (Dynamic premiums, actuarial discounts, factor breakdown).
- [ ] **Step 7:** Customer & Insurer React Dashboards (Driver scorecards, trip history, insurer actuarial adjustments).
