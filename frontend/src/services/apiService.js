/**
 * API Service for communication with the Spring Boot Backend.
 * Uses native Fetch API with structured error handling and abort timeouts.
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

/**
 * Helper extracting the Spring Security XSRF-TOKEN cookie.
 */
function getCsrfToken() {
  if (typeof document === 'undefined') return null;
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : null;
}

/**
 * Universal secure fetch wrapper that guarantees credentials: 'include' (cookie sessions)
 * and automatically injects X-XSRF-TOKEN on mutating HTTP methods.
 */
export async function secureFetch(url, options = {}) {
  const headers = { ...options.headers };
  const method = (options.method || 'GET').toUpperCase();

  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
    const csrf = getCsrfToken();
    if (csrf && !headers['X-XSRF-TOKEN']) {
      headers['X-XSRF-TOKEN'] = csrf;
    }
  }

  return fetch(url, {
    ...options,
    headers,
    credentials: 'include'
  });
}

/**
 * Fetch operational health status from backend.
 * @returns {Promise<{success: boolean, data?: any, error?: string, raw?: any}>}
 */
export async function fetchHealth() {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 6000);

  try {
    const response = await secureFetch(`${API_BASE_URL}/health`, {
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
    const response = await secureFetch(`${API_BASE_URL}/info`, {
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
    const response = await secureFetch(`${API_BASE_URL}/vehicles`, {
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
    const response = await secureFetch(`${API_BASE_URL}/vehicles?page=${page}&size=${size}`, {
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
    const response = await secureFetch(`${API_BASE_URL}/vehicles/${id}`, {
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
    const response = await secureFetch(`${API_BASE_URL}/vehicles/verify`, {
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
    const response = await secureFetch(`${API_BASE_URL}/telemetry/ingest`, {
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
    const response = await secureFetch(`${API_BASE_URL}/telemetry/simulate`, {
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
    const response = await secureFetch(`${API_BASE_URL}/telemetry/vehicles/${vehicleId}?page=${page}&size=${size}`, {
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
    const response = await secureFetch(`${API_BASE_URL}/telemetry/vehicles/${vehicleId}/summary`, {
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
    const response = await secureFetch(`${API_BASE_URL}/telemetry/vehicles/${vehicleId}`, {
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

// ==============================================================================
// Step 4: Trip Stitching & Feature Engineering APIs
// ==============================================================================

/**
 * Stitch raw telemetry pings into trip sessions for a vehicle.
 * @param {number|string} vehicleId
 */
export async function stitchTrips(vehicleId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/trips/stitch/${vehicleId}`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to stitch trips`
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
 * Fetch paginated list of trips for a vehicle.
 * @param {number|string} vehicleId
 * @param {number} page
 * @param {number} size
 */
export async function fetchTripsForVehicle(vehicleId, page = 0, size = 10) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/trips/vehicles/${vehicleId}?page=${page}&size=${size}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch trips`
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
 * Fetch single trip by its ID.
 * @param {number|string} tripId
 */
export async function fetchTripById(tripId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/trips/${tripId}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Trip not found`
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
 * Calculate or recalculate driving behavioral features for a trip.
 * @param {number|string} tripId
 */
export async function calculateTripFeatures(tripId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/features/trips/${tripId}/calculate`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to calculate features`
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
 * Fetch calculated features for a trip.
 * @param {number|string} tripId
 */
export async function fetchTripFeatures(tripId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/features/trips/${tripId}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Features not found`
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
 * Calculate features for all trips belonging to a vehicle.
 * @param {number|string} vehicleId
 */
export async function calculateVehicleFeatures(vehicleId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/features/vehicles/${vehicleId}/calculate`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Batch feature calculation failed`
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
 * Calculate behavioral safety risk score for a trip (Step 5).
 * @param {number|string} tripId
 */
export async function calculateTripRiskScore(tripId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/risk-scores/trips/${tripId}/calculate`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Risk score calculation failed`
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
 * Fetch behavioral safety risk score for a trip (Step 5).
 * @param {number|string} tripId
 */
export async function fetchTripRiskScore(tripId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/risk-scores/trips/${tripId}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Risk score not found`
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
 * Batch calculate risk scores for all eligible completed trips of a vehicle (Step 5).
 * @param {number|string} vehicleId
 */
export async function calculateVehicleRiskScores(vehicleId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/risk-scores/vehicles/${vehicleId}/calculate`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Vehicle risk calculation failed`
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
 * Fetch paginated risk score history for a vehicle (Step 5).
 * @param {number|string} vehicleId
 * @param {number} page
 * @param {number} size
 */
export async function fetchVehicleRiskScores(vehicleId, page = 0, size = 10) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/risk-scores/vehicles/${vehicleId}?page=${page}&size=${size}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch risk scores`
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
 * Calculate and persist an actuarial premium quote for a vehicle (Step 6).
 * @param {number|string} vehicleId
 * @param {object} [requestBody] e.g. { annualDistanceKmAssumption: 10000.0 }
 */
export async function calculatePremiumQuote(vehicleId, requestBody = null) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/pricing/vehicles/${vehicleId}/quotes`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: requestBody ? JSON.stringify(requestBody) : JSON.stringify({})
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to calculate quote`
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
 * Fetch an existing quote snapshot by its ID (Step 6).
 * @param {number|string} quoteId
 */
export async function fetchQuoteById(quoteId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/pricing/quotes/${quoteId}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Quote not found`
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
 * Fetch paginated quote history for a vehicle (Step 6).
 * @param {number|string} vehicleId
 * @param {number} page
 * @param {number} size
 */
export async function fetchVehicleQuotes(vehicleId, page = 0, size = 10) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/pricing/vehicles/${vehicleId}/quotes?page=${page}&size=${size}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch vehicle quotes`
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
 * Fetch safe prototype pricing configuration and multipliers (Step 6).
 */
export async function fetchPricingConfiguration() {
  try {
    const response = await secureFetch(`${API_BASE_URL}/pricing/configuration`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch pricing configuration`
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

// ==========================================
// Step 7: Customer Driver Scorecards & Insurer Analytics APIs
// ==========================================

/**
 * Fetch Customer Driver Scorecard summary for a vehicle (Step 7).
 * @param {number} vehicleId
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchCustomerScorecard(vehicleId) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/dashboard/customer/vehicles/${vehicleId}`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });
    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch customer scorecard`
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Fetch paginated customer trips with safety scores and driving features (Step 7).
 * @param {number} vehicleId
 * @param {number} page
 * @param {number} size
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchCustomerTrips(vehicleId, page = 0, size = 10) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/dashboard/customer/vehicles/${vehicleId}/trips?page=${page}&size=${size}`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });
    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch customer trips`
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Fetch Insurer Analytics: Fleet Overview macro metrics (Step 7).
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchInsurerOverview() {
  try {
    const response = await secureFetch(`${API_BASE_URL}/dashboard/insurer/overview`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });
    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch fleet overview`
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Fetch Insurer Analytics: Risk Distribution across trips and vehicles (Step 7).
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchInsurerRiskDistribution() {
  try {
    const response = await secureFetch(`${API_BASE_URL}/dashboard/insurer/risk-distribution`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });
    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch risk distribution`
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Fetch Insurer Analytics: Aggregated driving behavior and weighted rates (Step 7).
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchInsurerDrivingBehavior() {
  try {
    const response = await secureFetch(`${API_BASE_URL}/dashboard/insurer/behavior`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });
    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch driving behavior analytics`
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Fetch Insurer Analytics: Usage and observed mileage coverage (Step 7).
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchInsurerUsageAnalytics() {
  try {
    const response = await secureFetch(`${API_BASE_URL}/dashboard/insurer/usage`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });
    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch usage analytics`
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Fetch Insurer Analytics: Actuarial premium quotes, average and median (Step 7).
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function fetchInsurerPricingAnalytics() {
  try {
    const response = await secureFetch(`${API_BASE_URL}/dashboard/insurer/pricing`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });
    const payload = await response.json().catch(() => null);

    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Failed to fetch pricing analytics`
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Register a new customer account (Step 8).
 * @param {{ email: string, password: string, fullName?: string }} credentials
 * @returns {Promise<{success: boolean, data?: any, error?: string, status?: number}>}
 */
export async function registerCustomer(credentials) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(credentials)
    });
    const payload = await response.json().catch(() => null);
    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Registration failed`,
        status: response.status
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Authenticate with credentials and initialize HttpOnly session cookie (Step 8).
 * @param {{ email: string, password: string }} credentials
 * @returns {Promise<{success: boolean, data?: any, error?: string, status?: number}>}
 */
export async function loginUser(credentials) {
  try {
    const response = await secureFetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(credentials)
    });
    const payload = await response.json().catch(() => null);
    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || 'Invalid email or password.',
        status: response.status
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Logout and invalidate backend session (Step 8).
 * @returns {Promise<{success: boolean, error?: string}>}
 */
export async function logoutUser() {
  try {
    const response = await secureFetch(`${API_BASE_URL}/auth/logout`, {
      method: 'POST',
      headers: { 'Accept': 'application/json' }
    });
    const payload = await response.json().catch(() => null);
    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Logout failed`
      };
    }
    return { success: true };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Fetch authenticated user profile using active session cookie (Step 8).
 * @returns {Promise<{success: boolean, data?: any, error?: string, status?: number}>}
 */
export async function fetchCurrentUser() {
  try {
    const response = await secureFetch(`${API_BASE_URL}/auth/me`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });
    const payload = await response.json().catch(() => null);
    if (!response.ok) {
      return {
        success: false,
        error: payload?.error?.message || `HTTP ${response.status}: Unauthenticated`,
        status: response.status
      };
    }
    return { success: true, data: payload.data };
  } catch (err) {
    return { success: false, error: `Network error: ${err.message}` };
  }
}

/**
 * Trigger backend CSRF cookie initialization (Step 8).
 * @returns {Promise<{success: boolean}>}
 */
export async function fetchCsrfToken() {
  try {
    await secureFetch(`${API_BASE_URL}/auth/csrf`, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    });
    return { success: true };
  } catch (err) {
    return { success: false, error: err.message };
  }
}

export { API_BASE_URL };


