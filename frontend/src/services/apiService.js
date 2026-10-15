/**
 * API Service for communication with the Spring Boot Backend.
 * Uses native Fetch API with structured error handling and abort timeouts.
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

/**
 * Fetch operational health status from backend.
 * @returns {Promise<{success: boolean, data?: any, error?: string, raw?: any}>}
 */
export async function fetchHealth() {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 6000);

  try {
    const response = await fetch(`${API_BASE_URL}/health`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      },
      signal: controller.signal
    });

    clearTimeout(timeoutId);

    if (!response.ok) {
      const errorBody = await response.json().catch(() => null);
      const message = errorBody?.error?.message || `HTTP ${response.status}: ${response.statusText}`;
      return {
        success: false,
        error: message,
        status: response.status
      };
    }

    const payload = await response.json();
    return {
      success: true,
      data: payload.data,
      timestamp: payload.timestamp
    };
  } catch (err) {
    clearTimeout(timeoutId);
    let message = 'Backend unreachable: Network connection failed.';
    if (err.name === 'AbortError') {
      message = 'Connection timed out (backend took longer than 6s to respond).';
    } else if (err.message) {
      message = `Connection failed: ${err.message}`;
    }
    return {
      success: false,
      error: message
    };
  }
}

/**
 * Fetch application metadata and architecture info from backend.
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchInfo() {
  try {
    const response = await fetch(`${API_BASE_URL}/info`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      return {
        success: false,
        error: `HTTP ${response.status}: Failed to retrieve app info`
      };
    }

    const payload = await response.json();
    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

/**
 * Register a new vehicle.
 * @param {Object} vehicleData
 * @returns {Promise<{success: boolean, data?: any, error?: string, status?: number}>}
 */
export async function registerVehicle(vehicleData) {
  try {
    const response = await fetch(`${API_BASE_URL}/vehicles`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(vehicleData)
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      const message = payload?.error?.message || `HTTP ${response.status}: Registration failed`;
      return {
        success: false,
        error: message,
        status: response.status
      };
    }

    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

/**
 * Retrieve a paginated list of registered vehicles.
 * @param {number} page
 * @param {number} size
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchVehicles(page = 0, size = 10) {
  try {
    const response = await fetch(`${API_BASE_URL}/vehicles?page=${page}&size=${size}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch vehicles`
      };
    }

    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

/**
 * Retrieve a vehicle by its ID.
 * @param {number|string} id
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchVehicleById(id) {
  try {
    const response = await fetch(`${API_BASE_URL}/vehicles/${id}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Vehicle not found`
      };
    }

    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

/**
 * Run simulated mock verification for a vehicle.
 * @param {Object} verificationData { vehicleId, vehicleNumber, make, model, manufacturingYear }
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function verifyVehicle(verificationData) {
  try {
    const response = await fetch(`${API_BASE_URL}/vehicles/verify`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(verificationData)
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Verification failed`
      };
    }

    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

/**
 * Ingest a batch of telematics points.
 * @param {Object} batchData { vehicleId, points }
 */
export async function ingestTelemetryBatch(batchData) {
  try {
    const response = await fetch(`${API_BASE_URL}/telemetry/ingest`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(batchData)
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Ingestion failed`
      };
    }

    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

/**
 * Simulate driving route and ingest synthetic telematics stream.
 * @param {Object} simData { vehicleId, profile, pointCount }
 */
export async function simulateTelemetry(simData) {
  try {
    const response = await fetch(`${API_BASE_URL}/telemetry/simulate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(simData)
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Simulation failed`
      };
    }

    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

/**
 * Fetch paginated telematics data points for a vehicle.
 * @param {number|string} vehicleId
 * @param {number} page
 * @param {number} size
 */
export async function fetchVehicleTelemetry(vehicleId, page = 0, size = 20) {
  try {
    const response = await fetch(`${API_BASE_URL}/telemetry/vehicles/${vehicleId}?page=${page}&size=${size}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch telemetry`
      };
    }

    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

/**
 * Fetch telematics summary and risk indicators for a vehicle.
 * @param {number|string} vehicleId
 */
export async function fetchVehicleTelemetrySummary(vehicleId) {
  try {
    const response = await fetch(`${API_BASE_URL}/telemetry/vehicles/${vehicleId}/summary`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch telemetry summary`
      };
    }

    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

/**
 * Clear all telemetry records for a vehicle.
 * @param {number|string} vehicleId
 */
export async function clearVehicleTelemetry(vehicleId) {
  try {
    const response = await fetch(`${API_BASE_URL}/telemetry/vehicles/${vehicleId}`, {
      method: 'DELETE',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to clear telemetry`
      };
    }

    return {
      success: true,
      data: payload.data
    };
  } catch (err) {
    return {
      success: false,
      error: `Network error: ${err.message}`
    };
  }
}

export { API_BASE_URL };
