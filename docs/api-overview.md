# Usage-Based Insurance (UBI) Platform — API Specification (v1)

This document outlines the REST endpoints exposed by the UBI platform backend.

---

## Base URL
```
http://localhost:8080/api/v1
```

---

## 0. Authentication & Session Endpoints (Step 8)

All state-changing requests (`POST`, `PUT`, `DELETE`) require standard session cookies (`JSESSIONID`) and CSRF validation via the `X-XSRF-TOKEN` header.

### Register Customer Account
- **Endpoint:** `POST /auth/register`
- **Access:** Public
- **Description:** Registers a new `CUSTOMER` user account. Public registration cannot assign the `INSURER` role.
- **Request Body:**
```json
{
  "email": "customer@example.com",
  "password": "SecurePassword123!"
}
```
- **Response (201 Created):**
```json
{
  "success": true,
  "timestamp": "2026-10-15T15:00:00.000Z",
  "data": {
    "id": 2,
    "email": "customer@example.com",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "createdAt": "2026-10-15T15:00:00.000Z"
  }
}
```

### User Login
- **Endpoint:** `POST /auth/login`
- **Access:** Public
- **Description:** Authenticates credentials and establishes a server-side session (`HttpOnly` cookie).
- **Request Body:**
```json
{
  "email": "customer@example.com",
  "password": "SecurePassword123!"
}
```
- **Response (200 OK):** Returns authenticated user profile and sets `JSESSIONID` cookie.

### User Logout
- **Endpoint:** `POST /auth/logout`
- **Access:** Authenticated (`CUSTOMER` or `INSURER`)
- **Description:** Terminates and invalidates the active server session and clears cookies.
- **Response (200 OK):**
```json
{
  "success": true,
  "data": "Logged out successfully"
}
```

### Current User Profile
- **Endpoint:** `GET /auth/me`
- **Access:** Authenticated (`CUSTOMER` or `INSURER`)
- **Description:** Retrieves the profile of the currently authenticated principal. Returns `401 Unauthorized` if anonymous.
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "email": "customer@example.com",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "createdAt": "2026-10-15T15:00:00.000Z"
  }
}
```

### CSRF Token Warmup
- **Endpoint:** `GET /auth/csrf`
- **Access:** Public
- **Description:** Retrieves and seeds the CSRF token cookie for single-page applications.

---

## 1. System Health & Info

### System Health Check
- **Endpoint:** `GET /health`
- **Produces:** `application/json`
- **Description:** Returns operational health status of JVM, memory, and database connectivity.

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
      "engine": "MySQL",
      "catalog": "usage_based_insurance"
    },
    "runtime": {
      "javaVersion": "21.0.10",
      "uptimeSeconds": 42
    }
  }
}
```

### Platform Descriptor & Roadmap
- **Endpoint:** `GET /info`
- **Description:** Returns metadata, active profile, and list of platform modules.

---

## 2. Vehicle Fleet & Verification Endpoints (Step 2)

### Register Vehicle
- **Endpoint:** `POST /vehicles`
- **Description:** Registers a new vehicle into the platform with normalized registration number.
- **Request Body:**
```json
{
  "vehicleNumber": "MH02AB1234",
  "make": "Toyota",
  "model": "Innova Crysta",
  "manufacturingYear": 2022,
  "engineCapacityCc": 2393,
  "fuelType": "DIESEL",
  "vehicleType": "SUV"
}
```

### List Fleet Vehicles
- **Endpoint:** `GET /vehicles?page=0&size=10&sortBy=createdAt&direction=desc`
- **Description:** Returns a paginated list of registered policyholder vehicles.

### Get Vehicle by ID
- **Endpoint:** `GET /vehicles/{id}`

### Run Mock Verification
- **Endpoint:** `POST /vehicles/verify`

---

## 3. IoT Telematics Ingestion & Simulator Endpoints (Step 3)

### Ingest Telematics Batch
- **Endpoint:** `POST /telemetry/ingest`
- **Description:** Ingests a time-series batch of GPS, speed, and acceleration readings. Filters anomalies in real-time.

### Ingest Single Telematics Point
- **Endpoint:** `POST /telemetry/point`

### Simulate Driving Route
- **Endpoint:** `POST /telemetry/simulate`
- **Description:** Generates synthetic, physics-calibrated driving route and ingests it immediately.
- **Supported profiles:** `SAFE_COMMUTE`, `AGGRESSIVE_DRIVING`, `SPEEDING_HIGHWAY`, `ANOMALOUS_SENSOR`.

### Get Vehicle Telemetry History
- **Endpoint:** `GET /telemetry/vehicles/{vehicleId}?page=0&size=20&sortBy=timestamp&direction=desc`

### Get Vehicle Telematics Risk Summary
- **Endpoint:** `GET /telemetry/vehicles/{vehicleId}/summary`

### Clear Vehicle Telemetry
- **Endpoint:** `DELETE /telemetry/vehicles/{vehicleId}`

---

## 4. Trip Stitching & Lifecycle Management Endpoints (Step 4: Phase B & C)

### Stitch Telemetry into Trips
- **Endpoint:** `POST /trips/stitch/{vehicleId}`
- **Description:** Evaluates chronological telematics for a vehicle, detects movement journeys, segments by inactivity threshold (default 10 min), applies Haversine distance with GPS jump filtering, and returns trips.
- **Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 101,
      "vehicleId": 1,
      "vehicleNumber": "MH02AB1234",
      "startTime": "2026-10-15T14:00:00Z",
      "endTime": "2026-10-15T14:25:30Z",
      "startLatitude": 12.9716,
      "startLongitude": 77.5946,
      "endLatitude": 12.9850,
      "endLongitude": 77.6100,
      "durationSeconds": 1530,
      "durationFormatted": "25m 30s",
      "distanceMeters": 14820.5,
      "distanceKm": 14.82,
      "validRecordCount": 50,
      "anomalousRecordCount": 0,
      "status": "COMPLETED",
      "featuresCalculated": false
    }
  ]
}
```

### List Vehicle Trips (Paginated)
- **Endpoint:** `GET /trips/vehicles/{vehicleId}?page=0&size=10&sortBy=startTime&direction=desc`
- **Description:** Returns paginated list of journeys for an insured vehicle.

### Get Trip by ID
- **Endpoint:** `GET /trips/{tripId}`
- **Description:** Returns journey session details including bounding coordinates and lifecycle status.

---

## 5. Driving Feature Engineering Endpoints (Step 4: Phase D)

### Calculate Features for Trip
- **Endpoint:** `POST /features/trips/{tripId}/calculate`
- **Description:** Extracts trip features: Haversine distance, average/max speed, debounced harsh braking, rapid acceleration, speeding %, and night driving exposure in configured timezone. Recalculation is safe and idempotent.
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 201,
    "tripId": 101,
    "vehicleId": 1,
    "calculationVersion": "v1.0",
    "calculatedAt": "2026-10-15T14:30:00Z",
    "distanceKm": 14.82,
    "durationSeconds": 1530,
    "durationFormatted": "25m 30s",
    "averageSpeedKmh": 48.5,
    "maxSpeedKmh": 78.0,
    "harshBrakingCount": 1,
    "rapidAccelerationCount": 2,
    "speedingObservationsCount": 0,
    "harshBrakeRatePer100Km": 6.7,
    "rapidAccelRatePer100Km": 13.5,
    "speedingPct": 0.0,
    "nightDrivingMinutes": 0,
    "nightDrivingRatio": 0.0,
    "validRecordCount": 50,
    "anomalousRecordCount": 0,
    "dataQualityScore": 100.0,
    "dataQualityStatus": "EXCELLENT"
  }
}
```

### Get Stored Trip Features
- **Endpoint:** `GET /features/trips/{tripId}`

### Batch Calculate Features for Vehicle
- **Endpoint:** `POST /features/vehicles/{vehicleId}/calculate`
- **Description:** Processes and calculates features for all trips belonging to a vehicle.

---

## 6. Behavioral Risk Scoring Endpoints (Step 5)

### Calculate Trip Risk Score
- **Endpoint:** `POST /risk-scores/trips/{tripId}/calculate`
- **Description:** Converts driving behavioral features into an explainable 0–100 safety score.
- **Example Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "tripId": 101,
    "vehicleId": 1,
    "vehicleRegistrationNumber": "MH02AB1234",
    "score": 85.5,
    "scoreCategory": "EXCELLENT",
    "scoringVersion": "v1.0",
    "calculatedAt": "2026-10-15T14:40:00Z",
    "dataQualityStatus": "SUFFICIENT",
    "isScorable": true,
    "harshBrakingPenalty": 6.0,
    "rapidAccelerationPenalty": 4.5,
    "speedingPenalty": 4.0,
    "nightDrivingPenalty": 0.0,
    "totalPenalties": 14.5,
    "summaryExplanation": "Safety score of 85.5/100 (EXCELLENT). Base score 100.0 minus 14.5 total deductions: Harsh Braking (-6.0), Rapid Accel (-4.5), Speeding (-4.0), Night Driving (-0.0).",
    "factorExplanations": [
      {
        "factorName": "HARSH_BRAKING",
        "observedValue": 3.0,
        "unit": "events/100km",
        "penaltyApplied": 6.0,
        "maxPenaltyCap": 30.0,
        "included": true,
        "explanation": "Observed 3.0 harsh braking events/100km resulting in a 6.0 point deduction (cap: 30.0)."
      },
      {
        "factorName": "RAPID_ACCELERATION",
        "observedValue": 3.0,
        "unit": "events/100km",
        "penaltyApplied": 4.5,
        "maxPenaltyCap": 25.0,
        "included": true,
        "explanation": "Observed 3.0 rapid acceleration events/100km resulting in a 4.5 point deduction (cap: 25.0)."
      },
      {
        "factorName": "SPEEDING",
        "observedValue": 1.6,
        "unit": "events/100km",
        "penaltyApplied": 4.0,
        "maxPenaltyCap": 35.0,
        "included": true,
        "explanation": "Observed 1 speeding observations (1.6/100km) with max speed 124.0 km/h, incurring a 4.0 point deduction (cap: 35.0)."
      },
      {
        "factorName": "NIGHT_DRIVING",
        "observedValue": 0.0,
        "unit": "%",
        "penaltyApplied": 0.0,
        "maxPenaltyCap": 5.0,
        "included": true,
        "explanation": "Trip conducted entirely during daylight/safe operating hours; zero night penalty."
      },
      {
        "factorName": "DATA_QUALITY",
        "observedValue": 100.0,
        "unit": "%",
        "penaltyApplied": 0.0,
        "maxPenaltyCap": 0.0,
        "included": true,
        "explanation": "Data quality is 100.0% (EXCELLENT) with 50 valid and 0 anomalous records."
      }
    ]
  }
}
```

### Get Stored Trip Risk Score
- **Endpoint:** `GET /risk-scores/trips/{tripId}`
- **Description:** Returns the persisted risk score and factor explanations for a trip.

### Batch Calculate Risk Scores for Vehicle
- **Endpoint:** `POST /risk-scores/vehicles/{vehicleId}/calculate`
- **Description:** Scores all eligible completed trips associated with a vehicle.

### Get Paginated Risk Scores for Vehicle
- **Endpoint:** `GET /risk-scores/vehicles/{vehicleId}?page=0&size=10&sortBy=calculatedAt&direction=desc`
- **Description:** Returns paginated scoring history for an insured vehicle.

---

## 7. Actuarial Pricing Endpoints (Step 6)

### Calculate Vehicle Premium Quote
- **Endpoint:** `POST /pricing/vehicles/{vehicleId}/quotes`
- **Description:** Calculates and persists an actuarial premium quote snapshot in INR (₹) using eligible behavioral safety scores and mileage exposure.
- **Request Body (Optional):**
```json
{
  "annualDistanceKmAssumption": 10000.0
}
```
- **Example Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "quoteReference": "QTE-1773756200000-A1B2C3",
    "vehicleId": 1,
    "vehicleRegistrationNumber": "MH02AB1234",
    "vehicleMake": "Honda",
    "vehicleModel": "City",
    "status": "READY",
    "isEligible": true,
    "baseAnnualPremiumInr": 12000.00,
    "safetyScore": 88.0,
    "riskCategory": "EXCELLENT",
    "riskMultiplier": 0.9000,
    "observedDistanceKm": 45.0,
    "observationDays": 14,
    "projectedAnnualDistanceKm": 10000.0,
    "annualDistanceSource": "USER_SPECIFIED",
    "usageMultiplier": 1.0000,
    "rawAnnualPremiumInr": 10800.00,
    "finalAnnualPremiumInr": 10800.00,
    "currency": "INR",
    "pricingVersion": "pricing-v1",
    "calculatedAt": "2026-10-15T15:00:00Z",
    "excludedTripsCount": 0,
    "summaryExplanation": "Estimated annual premium of ₹10,800.00 INR calculated from base ₹12,000.00 × 0.9000 (risk) × 1.0000 (usage).",
    "factorExplanations": [
      {
        "factorName": "BASE_PREMIUM",
        "description": "Baseline Annual Premium Tariff",
        "appliedValue": "₹12,000.00",
        "status": "APPLIED",
        "explanation": "Standard baseline tariff of ₹12,000.00 per year for this vehicle category."
      },
      {
        "factorName": "RISK_MULTIPLIER",
        "description": "Driving Safety Risk Factor",
        "appliedValue": "0.9000",
        "status": "APPLIED",
        "explanation": "Safety score of 88.0/100 (EXCELLENT) yields risk multiplier 0.9000 (10.0% safe driver discount)."
      },
      {
        "factorName": "USAGE_MULTIPLIER",
        "description": "Annual Mileage Road Exposure",
        "appliedValue": "1.0000",
        "status": "APPLIED",
        "explanation": "Projected annual distance of 10,000 km (User-supplied assumption) results in usage multiplier 1.0000."
      },
      {
        "factorName": "PREMIUM_BOUNDS",
        "description": "Policy Floor and Ceiling Limits",
        "appliedValue": "₹5,000.00 to ₹35,000.00",
        "status": "WITHIN_BOUNDS",
        "explanation": "Raw premium is within policy floor and ceiling bounds."
      }
    ]
  }
}
```

### Get Premium Quote by ID
- **Endpoint:** `GET /pricing/quotes/{quoteId}`
- **Description:** Retrieves an existing quote snapshot by its primary database ID.

### Get Paginated Quote History for Vehicle
- **Endpoint:** `GET /pricing/vehicles/{vehicleId}/quotes?page=0&size=10&sortBy=calculatedAt&direction=desc`
- **Description:** Returns paginated historical quotes for an insured vehicle.

### Get Safe Pricing Configuration
- **Endpoint:** `GET /pricing/configuration`
- **Description:** Exposes safe, non-sensitive prototype assumptions, baseline tariffs, limits, and multipliers.

---

## 8. Dashboard Aggregation Endpoints (Step 7)

### Customer Driver Scorecard
- **Endpoint:** `GET /dashboard/customer/vehicles/{vehicleId}`
- **Description:** Aggregates vehicle summary, mock verification status, distance-weighted behavioral event rates, latest safety score, latest quote snapshot, and chronological score progression.
- **Example Response:**
```json
{
  "success": true,
  "data": {
    "vehicleId": 1,
    "vehicleNumber": "MH02AB1234",
    "make": "Honda",
    "model": "City",
    "manufacturingYear": 2022,
    "fuelType": "PETROL",
    "verificationStatus": "VERIFIED (Mock Verification)",
    "isMockVerification": true,
    "completedTripsCount": 5,
    "totalObservedDistanceKm": 85.4,
    "latestSafetyScore": 88.0,
    "latestRiskCategory": "EXCELLENT",
    "scoringVersion": "v1.0",
    "harshBrakeRatePer100Km": 1.2,
    "rapidAccelRatePer100Km": 0.8,
    "speedingObsPer100Km": 2.1,
    "nightDrivingRatio": 0.12,
    "nightDrivingMinutes": 24,
    "latestEstimatedAnnualPremiumInr": 10800.00,
    "scoreHistory": [
      { "tripId": 101, "score": 85.0, "riskCategory": "EXCELLENT", "distanceKm": 15.0, "isScorable": true },
      { "tripId": 102, "score": 88.0, "riskCategory": "EXCELLENT", "distanceKm": 20.4, "isScorable": true }
    ],
    "disclaimer": "Prototype score and illustrative quote. Not a binding insurance policy or guaranteed underwriting offer."
  }
}
```

### Customer Paginated Trip History
- **Endpoint:** `GET /dashboard/customer/vehicles/{vehicleId}/trips?page=0&size=10&sortBy=startTime&direction=desc`
- **Description:** Returns paginated list of trips for an insured vehicle with embedded driving features and safety score breakdown.

### Insurer Fleet Overview
- **Endpoint:** `GET /dashboard/insurer/overview`
- **Description:** Aggregates portfolio macro metrics: total registered vehicles, vehicles with telematics, completed trips, observed distance, scorable/unrated trips, quoted vehicles, and fleet average safety score.

### Insurer Risk Distributions
- **Endpoint:** `GET /dashboard/insurer/risk-distribution`
- **Description:** Returns counts and percentages for both trip-level score categories (EXCELLENT, GOOD, MODERATE, HIGH_RISK, UNRATED) and vehicle-level portfolio risk distribution.

### Insurer Driving Behavior
- **Endpoint:** `GET /dashboard/insurer/behavior`
- **Description:** Returns distance-weighted fleet event rates per 100 km (harsh braking, rapid acceleration, speeding), night driving exposure, and telematics anomaly rates.

### Insurer Usage & Mileage Coverage
- **Endpoint:** `GET /dashboard/insurer/usage`
- **Description:** Returns total observed distance, sufficient vs. insufficient usage counts, and a breakdown per vehicle.

### Insurer Illustrative Pricing Analytics
- **Endpoint:** `GET /dashboard/insurer/pricing`
- **Description:** Computes portfolio premium metrics across eligible quotes including average and median annual premiums in INR, min/max range, risk category breakdown, and pricing versions.

---

## 9. Standard Error Structure

All 4xx/5xx responses adhere to the standard error structure:
```json
{
  "success": false,
  "timestamp": "2026-10-15T14:35:00.123Z",
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Trip not found with ID: 999",
    "path": "/api/v1/trips/999",
    "status": 404
  }
}
```
