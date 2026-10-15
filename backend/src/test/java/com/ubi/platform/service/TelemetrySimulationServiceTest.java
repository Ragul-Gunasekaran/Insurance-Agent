package com.ubi.platform.service;

import com.ubi.platform.dto.TelematicsPointRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetrySimulationServiceTest {

    private TelemetrySimulationService simulationService;

    @BeforeEach
    void setUp() {
        simulationService = new TelemetrySimulationService();
    }

    @Test
    @DisplayName("Should generate requested number of simulation points for safe commute")
    void testSafeCommuteRoute() {
        List<TelematicsPointRequest> points = simulationService.generateSimulationRoute(1L, "SAFE_COMMUTE", 15);
        assertThat(points).hasSize(15);

        for (TelematicsPointRequest pt : points) {
            assertThat(pt.getVehicleId()).isEqualTo(1L);
            assertThat(pt.getLatitude()).isBetween(12.0, 14.0);
            assertThat(pt.getLongitude()).isBetween(77.0, 79.0);
            assertThat(pt.getSpeedKmh()).isGreaterThanOrEqualTo(0.0);
            assertThat(pt.getAccelerationG()).isBetween(-0.5, 0.5);
        }
    }

    @Test
    @DisplayName("Should generate aggressive driving with harsh braking events")
    void testAggressiveDrivingRoute() {
        List<TelematicsPointRequest> points = simulationService.generateSimulationRoute(2L, "AGGRESSIVE_DRIVING", 20);
        assertThat(points).hasSize(20);

        boolean hasHarshBraking = points.stream().anyMatch(pt -> pt.getAccelerationG() <= -0.35);
        boolean hasRapidAccel = points.stream().anyMatch(pt -> pt.getAccelerationG() >= 0.30);

        assertThat(hasHarshBraking).isTrue();
        assertThat(hasRapidAccel).isTrue();
    }

    @Test
    @DisplayName("Should generate speeding highway route with high velocity")
    void testSpeedingHighwayRoute() {
        List<TelematicsPointRequest> points = simulationService.generateSimulationRoute(3L, "SPEEDING_HIGHWAY", 10);
        assertThat(points).hasSize(10);

        boolean hasSpeeding = points.stream().anyMatch(pt -> pt.getSpeedKmh() >= 120.0);
        assertThat(hasSpeeding).isTrue();
    }

    @Test
    @DisplayName("Should generate anomalous route with injected sensor spikes")
    void testAnomalousSensorRoute() {
        List<TelematicsPointRequest> points = simulationService.generateSimulationRoute(4L, "ANOMALOUS_SENSOR", 15);
        assertThat(points).hasSize(15);

        boolean hasSpeedSpike = points.stream().anyMatch(pt -> pt.getSpeedKmh() > 220.0);
        boolean hasGpsAnomaly = points.stream().anyMatch(pt -> pt.getLatitude() > 90.0);
        boolean hasAccelAnomaly = points.stream().anyMatch(pt -> pt.getAccelerationG() < -1.5);

        assertThat(hasSpeedSpike).isTrue();
        assertThat(hasGpsAnomaly).isTrue();
        assertThat(hasAccelAnomaly).isTrue();
    }
}
