package com.ubi.platform.service;

import com.ubi.platform.dto.TelematicsPointRequest;
import com.ubi.platform.entity.TelematicsValidationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Service responsible for validating raw IoT telematics data points,
 * filtering sensor noise, and identifying physical anomalies (Step 3: Ingestion & Cleaning).
 */
@Service
public class TelemetryValidationService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryValidationService.class);

    public static final double MIN_LATITUDE = -90.0;
    public static final double MAX_LATITUDE = 90.0;
    public static final double MIN_LONGITUDE = -180.0;
    public static final double MAX_LONGITUDE = 180.0;

    public static final double MIN_SPEED_KMH = 0.0;
    public static final double MAX_PLAUSIBLE_SPEED_KMH = 220.0;

    public static final double MAX_PLAUSIBLE_ACCEL_G = 1.5;
    public static final double MIN_PLAUSIBLE_ACCEL_G = -1.5;

    public record ValidationResult(TelematicsValidationStatus status, String notes) {
        public boolean isValid() {
            return status == TelematicsValidationStatus.VALID;
        }
    }

    /**
     * Validates an individual telemetry point against physical, geospatial, and chronological constraints.
     */
    public ValidationResult validatePoint(TelematicsPointRequest point) {
        if (point == null) {
            return new ValidationResult(TelematicsValidationStatus.TIMESTAMP_ANOMALY, "Point payload is null");
        }

        // 1. Timestamp validation (cannot be in the far future)
        if (point.getTimestamp() == null) {
            return new ValidationResult(TelematicsValidationStatus.TIMESTAMP_ANOMALY, "Timestamp is missing");
        }
        Instant now = Instant.now();
        if (point.getTimestamp().isAfter(now.plus(Duration.ofMinutes(5)))) {
            return new ValidationResult(TelematicsValidationStatus.TIMESTAMP_ANOMALY,
                    "Future timestamp detected: " + point.getTimestamp() + " (skew exceeds 5 minutes)");
        }

        // 2. Geospatial coordinates validation
        if (point.getLatitude() == null || point.getLongitude() == null) {
            return new ValidationResult(TelematicsValidationStatus.GPS_OUT_OF_BOUNDS, "Latitude or Longitude is null");
        }
        if (point.getLatitude() < MIN_LATITUDE || point.getLatitude() > MAX_LATITUDE ||
            point.getLongitude() < MIN_LONGITUDE || point.getLongitude() > MAX_LONGITUDE) {
            return new ValidationResult(TelematicsValidationStatus.GPS_OUT_OF_BOUNDS,
                    String.format("GPS coordinates [%.4f, %.4f] outside valid boundaries",
                            point.getLatitude(), point.getLongitude()));
        }

        // 3. Velocity / Speed plausibility
        if (point.getSpeedKmh() == null) {
            return new ValidationResult(TelematicsValidationStatus.SPEED_ANOMALY, "Speed value is null");
        }
        if (point.getSpeedKmh() < MIN_SPEED_KMH || point.getSpeedKmh() > MAX_PLAUSIBLE_SPEED_KMH) {
            return new ValidationResult(TelematicsValidationStatus.SPEED_ANOMALY,
                    String.format("Speed %.1f km/h is implausible (must be between 0 and %.0f km/h)",
                            point.getSpeedKmh(), MAX_PLAUSIBLE_SPEED_KMH));
        }

        // 4. Acceleration plausibility (maximum physical traction envelope)
        if (point.getAccelerationG() == null) {
            return new ValidationResult(TelematicsValidationStatus.ACCELERATION_ANOMALY, "Acceleration is null");
        }
        if (point.getAccelerationG() < MIN_PLAUSIBLE_ACCEL_G || point.getAccelerationG() > MAX_PLAUSIBLE_ACCEL_G) {
            return new ValidationResult(TelematicsValidationStatus.ACCELERATION_ANOMALY,
                    String.format("Acceleration %.2fG exceeds vehicle traction threshold (limits: +/-%.1fG)",
                            point.getAccelerationG(), MAX_PLAUSIBLE_ACCEL_G));
        }

        return new ValidationResult(TelematicsValidationStatus.VALID, "Clean and calibrated");
    }
}
