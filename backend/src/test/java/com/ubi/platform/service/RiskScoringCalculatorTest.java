package com.ubi.platform.service;

import com.ubi.platform.dto.RiskFactorExplanationDto;
import com.ubi.platform.entity.DrivingFeature;
import com.ubi.platform.entity.FuelType;
import com.ubi.platform.entity.Trip;
import com.ubi.platform.entity.TripStatus;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.entity.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the pure deterministic RiskScoringCalculator engine (Step 5).
 */
class RiskScoringCalculatorTest {

    private RiskScoringCalculator calculator;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        calculator = new RiskScoringCalculator();
        vehicle = new Vehicle("MH02AB1234", "Honda", "City", 2022, 1498, FuelType.PETROL, VehicleType.CAR);
        vehicle.setId(1L);
    }

    private Trip createCompletedTrip(double distanceKm, long durationSeconds, int validRecords, int anomalousRecords) {
        Trip trip = new Trip(
                vehicle,
                Instant.parse("2026-10-15T08:00:00Z"),
                Instant.parse("2026-10-15T08:30:00Z"),
                18.5204, 73.8567,
                18.5500, 73.8800,
                durationSeconds,
                distanceKm * 1000.0,
                validRecords,
                anomalousRecords,
                TripStatus.COMPLETED
        );
        trip.setId(10L);
        return trip;
    }

    private DrivingFeature createFeature(Trip trip, double distanceKm, long durationSeconds,
                                         double avgSpeed, double maxSpeed,
                                         int harshBrakes, int rapidAccels, int speedingCount,
                                         Double harshBrakeRate, Double rapidAccelRate,
                                         double nightRatio, int validRecords, int anomalousRecords,
                                         double dataQualityScore) {
        return new DrivingFeature(
                trip,
                trip.getVehicle(),
                "v1.0",
                distanceKm,
                durationSeconds,
                avgSpeed,
                maxSpeed,
                harshBrakes,
                rapidAccels,
                speedingCount,
                harshBrakeRate,
                rapidAccelRate,
                0.0,
                (long) (durationSeconds * nightRatio / 60),
                nightRatio,
                validRecords,
                anomalousRecords,
                dataQualityScore,
                dataQualityScore >= 90.0 ? "EXCELLENT" : "DEGRADED"
        );
    }

    @Test
    void testPerfectObservedBehaviourWithSufficientData() {
        Trip trip = createCompletedTrip(20.0, 1800L, 100, 0);
        DrivingFeature feature = createFeature(
                trip, 20.0, 1800L, 45.0, 65.0,
                0, 0, 0, 0.0, 0.0,
                0.0, 100, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertTrue(result.isScorable());
        assertEquals("SUFFICIENT", result.getDataQualityStatus());
        assertEquals(100.0, result.getScore());
        assertEquals("EXCELLENT", result.getScoreCategory());
        assertEquals(0.0, result.getTotalPenalties());
        assertEquals(0.0, result.getHarshBrakingPenalty());
        assertEquals(0.0, result.getRapidAccelerationPenalty());
        assertEquals(0.0, result.getSpeedingPenalty());
        assertEquals(0.0, result.getNightDrivingPenalty());

        // Verify explanations
        List<RiskFactorExplanationDto> factors = result.getFactorExplanations();
        assertNotNull(factors);
        assertEquals(5, factors.size());
        assertTrue(factors.stream().allMatch(RiskFactorExplanationDto::getIncluded));
    }

    @Test
    void testHarshBrakingPenaltyApplied() {
        Trip trip = createCompletedTrip(20.0, 1800L, 100, 0);
        // 5.0 harsh braking events per 100km * weight 2.0 = 10.0 deduction
        DrivingFeature feature = createFeature(
                trip, 20.0, 1800L, 45.0, 65.0,
                1, 0, 0, 5.0, 0.0,
                0.0, 100, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertTrue(result.isScorable());
        assertEquals(10.0, result.getHarshBrakingPenalty());
        assertEquals(10.0, result.getTotalPenalties());
        assertEquals(90.0, result.getScore());
        assertEquals("EXCELLENT", result.getScoreCategory());
    }

    @Test
    void testRapidAccelerationPenaltyApplied() {
        Trip trip = createCompletedTrip(20.0, 1800L, 100, 0);
        // 8.0 rapid acceleration events per 100km * weight 1.5 = 12.0 deduction
        DrivingFeature feature = createFeature(
                trip, 20.0, 1800L, 45.0, 65.0,
                0, 2, 0, 0.0, 8.0,
                0.0, 100, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertTrue(result.isScorable());
        assertEquals(12.0, result.getRapidAccelerationPenalty());
        assertEquals(12.0, result.getTotalPenalties());
        assertEquals(88.0, result.getScore());
    }

    @Test
    void testSpeedingPenaltyAndSeverityApplied() {
        Trip trip = createCompletedTrip(10.0, 900L, 50, 0);
        // 2 speeding observations over 10km = 20.0 per 100km * weight 2.5 = 50.0 capped at 35.0
        // max speed 140 km/h: severity extra (140 - 130) * 0.5 = 5.0
        // total raw = 55.0 capped at 35.0
        DrivingFeature feature = createFeature(
                trip, 10.0, 900L, 80.0, 140.0,
                0, 0, 2, 0.0, 0.0,
                0.0, 50, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertTrue(result.isScorable());
        assertEquals(35.0, result.getSpeedingPenalty()); // Capped at 35.0
        assertEquals(35.0, result.getTotalPenalties());
        assertEquals(65.0, result.getScore());
        assertEquals("MODERATE", result.getScoreCategory());
    }

    @Test
    void testNightDrivingPenaltyApplied() {
        Trip trip = createCompletedTrip(20.0, 1800L, 100, 0);
        // 50% night driving ratio * max penalty 5.0 = 2.5 deduction
        DrivingFeature feature = createFeature(
                trip, 20.0, 1800L, 45.0, 65.0,
                0, 0, 0, 0.0, 0.0,
                0.5, 100, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertTrue(result.isScorable());
        assertEquals(2.5, result.getNightDrivingPenalty());
        assertEquals(2.5, result.getTotalPenalties());
        assertEquals(97.5, result.getScore());
    }

    @Test
    void testIndividualPenaltyCapsEnforced() {
        Trip trip = createCompletedTrip(10.0, 900L, 50, 0);
        // Extreme values:
        // Harsh brake rate 50.0/100km * 2.0 = 100.0 -> cap 30.0
        // Rapid accel rate 40.0/100km * 1.5 = 60.0 -> cap 25.0
        // Speeding rate 10 observations over 10km = 100.0/100km * 2.5 = 250.0 -> cap 35.0
        // Night ratio 1.0 * 5.0 = 5.0 -> cap 5.0
        // Total penalties = 30 + 25 + 35 + 5 = 95.0
        DrivingFeature feature = createFeature(
                trip, 10.0, 900L, 90.0, 150.0,
                5, 4, 10, 50.0, 40.0,
                1.0, 50, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertTrue(result.isScorable());
        assertEquals(30.0, result.getHarshBrakingPenalty());
        assertEquals(25.0, result.getRapidAccelerationPenalty());
        assertEquals(35.0, result.getSpeedingPenalty());
        assertEquals(5.0, result.getNightDrivingPenalty());
        assertEquals(95.0, result.getTotalPenalties());
        assertEquals(5.0, result.getScore());
        assertEquals("HIGH_RISK", result.getScoreCategory());
    }

    @Test
    void testScoreBoundsClampedBetweenZeroAndOneHundred() {
        Trip trip = createCompletedTrip(10.0, 900L, 50, 0);
        // Set weights artificially very high to exceed 100 total penalty
        calculator.setHarshBrakingCap(60.0);
        calculator.setRapidAccelCap(60.0);

        DrivingFeature feature = createFeature(
                trip, 10.0, 900L, 90.0, 120.0,
                10, 10, 0, 100.0, 100.0,
                0.0, 50, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        // Clamped at 0.0, never negative
        assertEquals(0.0, result.getScore());
        assertEquals("HIGH_RISK", result.getScoreCategory());
    }

    @Test
    void testInsufficientDistanceReturnsUnavailableScore() {
        // Distance 0.3 km < 0.5 km threshold
        Trip trip = createCompletedTrip(0.3, 120L, 20, 0);
        DrivingFeature feature = createFeature(
                trip, 0.3, 120L, 15.0, 25.0,
                0, 0, 0, 0.0, 0.0,
                0.0, 20, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertFalse(result.isScorable());
        assertNull(result.getScore());
        assertEquals("UNRATED", result.getScoreCategory());
        assertEquals("INSUFFICIENT_DISTANCE", result.getDataQualityStatus());
        assertTrue(result.getSummaryExplanation().contains("minimum required distance"));
        // Factors excluded
        assertTrue(result.getFactorExplanations().stream().noneMatch(RiskFactorExplanationDto::getIncluded));
    }

    @Test
    void testInsufficientTelemetrySampleReturnsUnavailableScore() {
        // Only 5 valid records < 10 threshold
        Trip trip = createCompletedTrip(5.0, 300L, 5, 0);
        DrivingFeature feature = createFeature(
                trip, 5.0, 300L, 40.0, 50.0,
                0, 0, 0, 0.0, 0.0,
                0.0, 5, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertFalse(result.isScorable());
        assertNull(result.getScore());
        assertEquals("INSUFFICIENT_TELEMETRY", result.getDataQualityStatus());
    }

    @Test
    void testHighAnomalyRateReturnsUnavailableScore() {
        // Data quality score 40.0% < 50.0% threshold
        Trip trip = createCompletedTrip(10.0, 600L, 20, 30);
        DrivingFeature feature = createFeature(
                trip, 10.0, 600L, 40.0, 60.0,
                0, 0, 0, 0.0, 0.0,
                0.0, 20, 30, 40.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertFalse(result.isScorable());
        assertNull(result.getScore());
        assertEquals("HIGH_ANOMALY_RATE", result.getDataQualityStatus());
    }

    @Test
    void testInProgressTripReturnsUnavailableScore() {
        Trip trip = createCompletedTrip(10.0, 600L, 50, 0);
        trip.setStatus(TripStatus.IN_PROGRESS);

        DrivingFeature feature = createFeature(
                trip, 10.0, 600L, 40.0, 60.0,
                0, 0, 0, 0.0, 0.0,
                0.0, 50, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertFalse(result.isScorable());
        assertNull(result.getScore());
        assertEquals("IN_PROGRESS_TRIP", result.getDataQualityStatus());
    }

    @Test
    void testNullNormalizedRatesReturnsUnavailableScore() {
        Trip trip = createCompletedTrip(5.0, 300L, 30, 0);
        DrivingFeature feature = createFeature(
                trip, 5.0, 300L, 30.0, 40.0,
                0, 0, 0, null, null,
                0.0, 30, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult result = calculator.calculateScore(trip, feature);

        assertFalse(result.isScorable());
        assertNull(result.getScore());
        assertEquals("INSUFFICIENT_DATA", result.getDataQualityStatus());
    }

    @Test
    void testDeterministicCalculation() {
        Trip trip = createCompletedTrip(25.0, 2000L, 120, 0);
        DrivingFeature feature = createFeature(
                trip, 25.0, 2000L, 50.0, 80.0,
                2, 1, 1, 8.0, 4.0,
                0.2, 120, 0, 100.0
        );

        RiskScoringCalculator.ScoringResult run1 = calculator.calculateScore(trip, feature);
        RiskScoringCalculator.ScoringResult run2 = calculator.calculateScore(trip, feature);

        assertEquals(run1.getScore(), run2.getScore());
        assertEquals(run1.getTotalPenalties(), run2.getTotalPenalties());
        assertEquals(run1.getScoreCategory(), run2.getScoreCategory());
        assertEquals(run1.getSummaryExplanation(), run2.getSummaryExplanation());
    }
}
