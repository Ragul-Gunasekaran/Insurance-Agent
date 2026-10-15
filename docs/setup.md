# Usage-Based Insurance (UBI) Platform — Setup & Execution Guide

This guide provides instructions tailored for **Windows 11 (PowerShell)** along with POSIX (macOS / Linux) equivalents.

---

## 1. System Requirements & Detected Environment

The environment inspection conducted for this machine detected:
- **Operating System:** Windows 11 64-bit
- **Java Runtime:** Java 21 LTS installed at `C:\Program Files\Java\jdk-21.0.10`
- **Node.js:** v24.18.0 (Installed)
- **npm:** 11.16.0 (Installed)
- **Maven:** Available via the local cache / project wrapper (`mvnw.cmd`)
- **MySQL:** Needs local service configuration (steps below)

---

## 2. MySQL Database Setup

The backend expects a relational MySQL database named `usage_based_insurance`.

### Option A: Using Installed MySQL Server or MySQL Workbench
1. Open PowerShell or MySQL Command Line Client:
   ```powershell
   mysql -u root -p
   ```
2. Enter your MySQL root password and execute:
   ```sql
   CREATE DATABASE IF NOT EXISTS usage_based_insurance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   SHOW DATABASES;
   EXIT;
   ```

### Option B: Using XAMPP / MariaDB / Laragon
1. Start MySQL service via XAMPP or Laragon Control Panel.
2. Open `http://localhost/phpmyadmin` in your browser.
3. Click **New**, type `usage_based_insurance` as the database name, and click **Create**.

### Option C: Installing via Winget (if not yet installed)
If you do not have MySQL installed on Windows, you can install MariaDB (fully MySQL wire-compatible) via winget:
```powershell
winget install MariaDB.Server --accept-source-agreements --accept-package-agreements
```

---

## 3. Environment Variable Configuration

### Backend Environment Configuration
Navigate to `usage-based-insurance/backend` and copy `.env.example` to `.env`:
```powershell
Copy-Item .env.example .env
```
Edit `.env` (or set environment variables in your shell) with your database credentials:
```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=usage_based_insurance
DB_USERNAME=root
DB_PASSWORD=your_actual_password
SERVER_PORT=8080
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

> **Note on Fallbacks:** If environment variables are omitted, Spring Boot safely defaults to `localhost:3306`, database name `usage_based_insurance`, and username `root`.

### Frontend Environment Configuration
Navigate to `usage-based-insurance/frontend`:
```powershell
Copy-Item .env.example .env
```
Ensure `.env` contains:
```properties
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

---

## 4. Building and Running the Backend

Open a PowerShell terminal:

```powershell
# Navigate to the backend directory
cd d:\Trust_Drive\usage-based-insurance\backend

# Set JAVA_HOME to Java 21 LTS if needed
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Build the project using Maven Wrapper
.\mvnw.cmd clean package -DskipTests

# Run the Spring Boot application
.\mvnw.cmd spring-boot:run
```

Once running, the backend will be accessible at:
- **Base URL:** `http://localhost:8080`
- **Health Check:** `http://localhost:8080/api/v1/health`
- **Application Info:** `http://localhost:8080/api/v1/info`

---

## 5. Building and Running the Frontend

Open a second PowerShell terminal:

```powershell
# Navigate to the frontend directory
cd d:\Trust_Drive\usage-based-insurance\frontend

# Install dependencies
npm install

# Start Vite development server
npm run dev
```

The frontend will start at:
- **Local URL:** `http://localhost:5173`

---

## 6. Verifying the Full Stack

1. Open `http://localhost:5173` in your browser.
2. The page displays the **Usage-Based Insurance Platform** landing view.
3. Observe the **Backend System Health** card.
4. Click **"Check Backend Connection"** or test the live ping.
5. The card fetches real-time diagnostic data from `http://localhost:8080/api/v1/health` and `http://localhost:8080/api/v1/info`.
6. Both backend status and database connectivity status are clearly displayed.

---

## 7. Troubleshooting

| Symptom | Cause | Solution |
| :--- | :--- | :--- |
| `java.lang.UnsupportedClassVersionError` | Running with wrong Java version | Set `$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"` before running Maven. |
| Backend starts, but reports `Database: DISCONNECTED` | MySQL service is not running or wrong password | Start MySQL service or check `DB_PASSWORD` in your `.env`. The backend continues serving HTTP requests gracefully even when the database is offline. |
| Frontend displays `Connection Failed: Network Error` | Backend is stopped or port 8080 blocked | Ensure Spring Boot is running in terminal 1 on port 8080. |
| CORS Error in browser console | Origin mismatch | Verify that `frontend` is on `http://localhost:5173` matching `CORS_ALLOWED_ORIGINS` in backend configuration. |
