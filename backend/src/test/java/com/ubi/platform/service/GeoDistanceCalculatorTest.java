package com.ubi.platform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoDistanceCalculatorTest {

    private GeoDistanceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new GeoDistanceCalculator();
    }

    @Test
    @DisplayName("Should accurately calculate distance between known nearby coordinates")
    void testKnownCoordinatesDistance() {
        // Point A: Vidhana Soudha, Bangalore [12.9797, 77.5908]
        // Point B: MG Road Metro, Bangalore [12.9756, 77.6066]
        // Expected Haversine distance is approximately 1.77 - 1.79 km
        double distanceMeters = calculator.calculateDistanceMeters(12.9797, 77.5908, 12.9756, 77.6066);
        double distanceKm = calculator.calculateDistanceKm(12.9797, 77.5908, 12.9756, 77.6066);

        assertThat(distanceMeters).isCloseTo(1775.0, within(25.0));
        assertThat(distanceKm).isCloseTo(1.775, within(0.025));
    }

    @Test
    @DisplayName("Should return 0.0 for identical coordinates")
    void testIdenticalCoordinates() {
        double dist = calculator.calculateDistanceMeters(12.9716, 77.5946, 12.9716, 77.5946);
        assertThat(dist).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return 0.0 when coordinates are null or missing")
    void testMissingCoordinates() {
        assertThat(calculator.calculateDistanceMeters(null, 77.5946, 12.9716, 77.5946)).isEqualTo(0.0);
        assertThat(calculator.calculateDistanceMeters(12.9716, null, 12.9716, 77.5946)).isEqualTo(0.0);
        assertThat(calculator.calculateDistanceMeters(12.9716, 77.5946, null, 77.5946)).isEqualTo(0.0);
        assertThat(calculator.calculateDistanceMeters(12.9716, 77.5946, 12.9716, null)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return 0.0 for out-of-bounds invalid coordinates")
    void testInvalidCoordinates() {
        assertThat(calculator.calculateDistanceMeters(95.0, 77.5946, 12.9716, 77.5946)).isEqualTo(0.0);
        assertThat(calculator.calculateDistanceMeters(12.9716, 195.0, 12.9716, 77.5946)).isEqualTo(0.0);
        assertThat(calculator.calculateDistanceMeters(-95.0, 77.5946, 12.9716, 77.5946)).isEqualTo(0.0);
        assertThat(calculator.calculateDistanceMeters(12.9716, -195.0, 12.9716, 77.5946)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should correctly identify plausible vs suspicious GPS teleportation jumps")
    void testSuspiciousGpsJumps() {
        // Plausible: 100 meters in 5 seconds (20 m/s = 72 km/h)
        assertThat(calculator.isPlausibleJump(100.0, 5)).isTrue();

        // Plausible: stationary / slow movement
        assertThat(calculator.isPlausibleJump(10.0, 5)).isTrue();

        // Implausible: 5000 meters in 5 seconds (1000 m/s = 3600 km/h teleportation jump)
        assertThat(calculator.isPlausibleJump(5000.0, 5)).isFalse();

        // Negative distance is always invalid
        assertThat(calculator.isPlausibleJump(-10.0, 5)).isFalse();

        // Instantaneous displacement: 10m is acceptable drift, 200m in 0s is impossible
        assertThat(calculator.isPlausibleJump(10.0, 0)).isTrue();
        assertThat(calculator.isPlausibleJump(200.0, 0)).isFalse();
    }
}
