# Usage-Based Insurance (UBI) Platform — API Specification (v1)

This document outlines the REST endpoints exposed by the UBI platform backend.

---

## Base URL
```
http://localhost:8080/api/v1
```

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
- **Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "vehicleNumber": "MH02AB1234",
    "make": "Toyota",
    "model": "Innova Crysta",
    "manufacturingYear": 2022,
    "fuelType": "DIESEL",
    "vehicleType": "SUV",
    "verificationStatus": "UNVERIFIED"
  }
}
```

### List Fleet Vehicles
- **Endpoint:** `GET /vehicles?page=0&size=10&sortBy=createdAt&direction=desc`
- **Description:** Returns a paginated list of registered policyholder vehicles.

### Get Vehicle by ID
- **Endpoint:** `GET /vehicles/{id}`

### Run Mock Verification
- **Endpoint:** `POST /vehicles/verify`
- **Description:** Runs mock verification against national registry rules and blacklist heuristics.
- **Request Body:**
```json
{
  "vehicleId": 1,
  "vehicleNumber": "MH02AB1234"
}
```

---

## 3. IoT Telematics Ingestion & Simulator Endpoints (Step 3)

### Ingest Telematics Batch
- **Endpoint:** `POST /telemetry/ingest`
- **Description:** Ingests a time-series batch of GPS, speed, and acceleration readings. Filters anomalies in real-time.
- **Request Body:**
```json
{
  "vehicleId": 1,
  "points": [
    {
      "timestamp": "2026-10-15T14:30:00Z",
      "latitude": 12.9716,
      "longitude": 77.5946,
      "speedKmh": 45.2,
      "accelerationG": 0.08,
      "heading": 90.0,
      "engineRpm": 1850,
      "fuelLevelPercent": 84.5
    }
  ]
}
```
- **Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "vehicleId": 1,
    "vehicleNumber": "MH02AB1234",
    "totalIngested": 1,
    "validPoints": 1,
    "anomalousPoints": 0,
    "message": "Successfully ingested 1 telematics points (1 valid, 0 filtered anomalies)."
  }
}
```

### Ingest Single Telematics Point
- **Endpoint:** `POST /telemetry/point`

### Simulate Driving Route
- **Endpoint:** `POST /telemetry/simulate`
- **Description:** Generates synthetic, physics-calibrated driving route and ingests it immediately.
- **Request Body:**
```json
{
  "vehicleId": 1,
  "profile": "SAFE_COMMUTE",
  "pointCount": 25
}
```
*Supported profiles:* `SAFE_COMMUTE`, `AGGRESSIVE_DRIVING`, `SPEEDING_HIGHWAY`, `ANOMALOUS_SENSOR`.

### Get Vehicle Telemetry History
- **Endpoint:** `GET /telemetry/vehicles/{vehicleId}?page=0&size=20&sortBy=timestamp&direction=desc`
- **Description:** Paginated raw and calibrated telematics audit logs with anomaly status codes.

### Get Vehicle Telematics Risk Summary
- **Endpoint:** `GET /telemetry/vehicles/{vehicleId}/summary`
- **Description:** Returns aggregate driving behavior metrics, harsh braking count (<= -0.35G), rapid acceleration count (>= +0.30G), and peak speed.
- **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "vehicleId": 1,
    "vehicleNumber": "MH02AB1234",
    "totalPoints": 25,
    "validPoints": 25,
    "anomalousPoints": 0,
    "maxSpeedKmh": 58.4,
    "averageSpeedKmh": 42.1,
    "maxDecelerationG": -0.18,
    "maxAccelerationG": 0.16,
    "harshBrakingEvents": 0,
    "rapidAccelerationEvents": 0,
    "speedingEvents": 0
  }
}
```

### Clear Vehicle Telemetry
- **Endpoint:** `DELETE /telemetry/vehicles/{vehicleId}`
- **Description:** Clears all telemetry records for a vehicle to reset simulation runs.

---

## 4. Standard Error Structure

All 4xx/5xx responses adhere to the standard error structure:
```json
{
  "success": false,
  "timestamp": "2026-10-15T14:35:00.123Z",
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Vehicle not found with ID: 999",
    "path": "/api/v1/telemetry/vehicles/999/summary",
    "status": 404
  }
}
```
