package com.ubi.platform.service;

import com.ubi.platform.dto.TelematicsPointRequest;
import com.ubi.platform.entity.TelematicsValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetryValidationServiceTest {

    private TelemetryValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TelemetryValidationService();
    }

    @Test
    @DisplayName("Should accept valid calibrated telemetry point")
    void testValidPoint() {
        TelematicsPointRequest request = new TelematicsPointRequest(
                1L,
                Instant.now().minus(10, ChronoUnit.SECONDS),
                12.9716,
                77.5946,
                48.5,
                0.08,
                90.0,
                1800,
                82.0
        );

        TelemetryValidationService.ValidationResult result = validationService.validatePoint(request);
        assertThat(result.isValid()).isTrue();
        assertThat(result.status()).isEqualTo(TelematicsValidationStatus.VALID);
        assertThat(result.notes()).contains("Clean and calibrated");
    }

    @Test
    @DisplayName("Should detect GPS coordinate outside latitude boundary")
    void testInvalidLatitude() {
        TelematicsPointRequest request = new TelematicsPointRequest(
                1L,
                Instant.now(),
                95.5, // > 90.0
                77.5946,
                45.0,
                0.0,
                0.0,
                1500,
                80.0
        );

        TelemetryValidationService.ValidationResult result = validationService.validatePoint(request);
        assertThat(result.isValid()).isFalse();
        assertThat(result.status()).isEqualTo(TelematicsValidationStatus.GPS_OUT_OF_BOUNDS);
        assertThat(result.notes()).contains("outside valid boundaries");
    }

    @Test
    @DisplayName("Should detect impossible high speed spike")
    void testSpeedSpike() {
        TelematicsPointRequest request = new TelematicsPointRequest(
                1L,
                Instant.now(),
                12.9716,
                77.5946,
                290.0, // > 220 km/h
                0.1,
                0.0,
                5500,
                80.0
        );

        TelemetryValidationService.ValidationResult result = validationService.validatePoint(request);
        assertThat(result.isValid()).isFalse();
        assertThat(result.status()).isEqualTo(TelematicsValidationStatus.SPEED_ANOMALY);
        assertThat(result.notes()).contains("Speed 290.0 km/h is implausible");
    }

    @Test
    @DisplayName("Should detect negative speed anomaly")
    void testNegativeSpeed() {
        TelematicsPointRequest request = new TelematicsPointRequest(
                1L,
                Instant.now(),
                12.9716,
                77.5946,
                -12.0,
                0.0,
                0.0,
                900,
                80.0
        );

        TelemetryValidationService.ValidationResult result = validationService.validatePoint(request);
        assertThat(result.isValid()).isFalse();
        assertThat(result.status()).isEqualTo(TelematicsValidationStatus.SPEED_ANOMALY);
    }

    @Test
    @DisplayName("Should detect acceleration spike exceeding physical traction threshold")
    void testAccelerationAnomaly() {
        TelematicsPointRequest request = new TelematicsPointRequest(
                1L,
                Instant.now(),
                12.9716,
                77.5946,
                50.0,
                2.2, // > 1.5G
                0.0,
                3000,
                80.0
        );

        TelemetryValidationService.ValidationResult result = validationService.validatePoint(request);
        assertThat(result.isValid()).isFalse();
        assertThat(result.status()).isEqualTo(TelematicsValidationStatus.ACCELERATION_ANOMALY);
        assertThat(result.notes()).contains("exceeds vehicle traction threshold");
    }

    @Test
    @DisplayName("Should detect future timestamp skew")
    void testFutureTimestamp() {
        TelematicsPointRequest request = new TelematicsPointRequest(
                1L,
                Instant.now().plus(1, ChronoUnit.HOURS),
                12.9716,
                77.5946,
                50.0,
                0.1,
                0.0,
                2000,
                80.0
        );

        TelemetryValidationService.ValidationResult result = validationService.validatePoint(request);
        assertThat(result.isValid()).isFalse();
        assertThat(result.status()).isEqualTo(TelematicsValidationStatus.TIMESTAMP_ANOMALY);
    }
}
