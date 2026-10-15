package com.ubi.platform.entity;

/**
 * Categorization of telematics point validation outcomes after anomaly detection.
 */
public enum TelematicsValidationStatus {
    /** Telematics point is physically plausible and within valid geospatial bounds. */
    VALID,

    /** Latitude outside [-90, 90] or Longitude outside [-180, 180]. */
    GPS_OUT_OF_BOUNDS,

    /** Speed exceeds plausible automotive upper threshold (> 220 km/h) or is negative. */
    SPEED_ANOMALY,

    /** Acceleration magnitude exceeds physical tire/vehicle traction limits (> 1.5G). */
    ACCELERATION_ANOMALY,

    /** Chronological inversion or corrupt timestamp sequence. */
    TIMESTAMP_ANOMALY
}
