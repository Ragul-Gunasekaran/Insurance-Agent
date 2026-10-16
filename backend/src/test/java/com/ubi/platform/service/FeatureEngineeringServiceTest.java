package com.ubi.platform.service;

import com.ubi.platform.dto.DrivingFeatureResponseDto;
import com.ubi.platform.entity.*;
import com.ubi.platform.repository.DrivingFeatureRepository;
import com.ubi.platform.repository.TelematicsRepository;
import com.ubi.platform.repository.TripRepository;
import com.ubi.platform.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureEngineeringServiceTest {

    @Mock
    private DrivingFeatureRepository featureRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TelematicsRepository telematicsRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private FeatureEngineeringService featureEngineeringService;

    private Vehicle vehicle;
    private Trip trip;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle("MH02AB1234", "Honda", "City", 2022, 1498, FuelType.PETROL, VehicleType.CAR);
        vehicle.setId(10L);

        Instant start = Instant.parse("2026-10-15T08:00:00Z");
        Instant end = Instant.parse("2026-10-15T08:30:00Z");

        trip = new Trip(
                vehicle, start, end,
                12.9716, 77.5946, 12.9800, 77.6000,
                1800L, 12500.0, 10, 0, TripStatus.COMPLETED
        );
        trip.setId(100L);

        featureEngineeringService.setHarshBrakingThresholdG(-0.35);
        featureEngineeringService.setRapidAccelThresholdG(0.30);
        featureEngineeringService.setSpeedingThresholdKmh(120.0);
        featureEngineeringService.setAppTimezone("Asia/Kolkata");
        featureEngineeringService.setNightStartHour(22);
        featureEngineeringService.setNightEndHour(5);
    }

    @Test
    @DisplayName("Should correctly calculate speed aggregates, distance, and duration")
    void testBasicFeatureCalculations() {
        Instant t0 = trip.getStartTime();
        List<TelematicsRecord> records = List.of(
                new TelematicsRecord(vehicle, t0, 12.9716, 77.5946, 40.0, 0.05, 0.0, 1500, 80.0, TelematicsValidationStatus.VALID, "Clean"),
                new TelematicsRecord(vehicle, t0.plusSeconds(10), 12.9720, 77.5950, 60.0, 0.10, 0.0, 1800, 80.0, TelematicsValidationStatus.VALID, "Clean"),
                new TelematicsRecord(vehicle, t0.plusSeconds(20), 12.9730, 77.5960, 80.0, -0.05, 0.0, 2200, 80.0, TelematicsValidationStatus.VALID, "Clean")
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(telematicsRepository.findByTripIdOrderByTimestampAsc(100L)).thenReturn(records);
        when(featureRepository.findByTripId(100L)).thenReturn(Optional.empty());
        when(featureRepository.save(any(DrivingFeature.class))).thenAnswer(inv -> inv.getArgument(0));

        DrivingFeatureResponseDto dto = featureEngineeringService.calculateFeaturesForTrip(100L);

        assertThat(dto.getTripId()).isEqualTo(100L);
        assertThat(dto.getMaxSpeedKmh()).isEqualTo(80.0);
        assertThat(dto.getAverageSpeedKmh()).isEqualTo(60.0);
        assertThat(dto.getValidRecordCount()).isEqualTo(3);
        assertThat(dto.getDataQualityStatus()).isEqualTo("EXCELLENT");
    }

    @Test
    @DisplayName("Should debounce sustained harsh braking to prevent double counting consecutive pings")
    void testHarshBrakingDebounce() {
        Instant t0 = trip.getStartTime();
        // 3 consecutive pings with deceleration <= -0.35G (representing a single 3-second hard stop)
        List<TelematicsRecord> records = List.of(
                new TelematicsRecord(vehicle, t0, 12.9716, 77.5946, 50.0, 0.0, 0.0, 1500, 80.0, TelematicsValidationStatus.VALID, "Clean"),
                new TelematicsRecord(vehicle, t0.plusSeconds(5), 12.9718, 77.5948, 40.0, -0.42, 0.0, 1400, 80.0, TelematicsValidationStatus.VALID, "Harsh 1"),
                new TelematicsRecord(vehicle, t0.plusSeconds(10), 12.9720, 77.5950, 25.0, -0.48, 0.0, 1200, 80.0, TelematicsValidationStatus.VALID, "Harsh 2"),
                new TelematicsRecord(vehicle, t0.plusSeconds(15), 12.9722, 77.5952, 10.0, -0.38, 0.0, 1000, 80.0, TelematicsValidationStatus.VALID, "Harsh 3"),
                new TelematicsRecord(vehicle, t0.plusSeconds(20), 12.9724, 77.5954, 5.0, -0.10, 0.0, 900, 80.0, TelematicsValidationStatus.VALID, "Recovered")
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(telematicsRepository.findByTripIdOrderByTimestampAsc(100L)).thenReturn(records);
        when(featureRepository.findByTripId(100L)).thenReturn(Optional.empty());
        when(featureRepository.save(any(DrivingFeature.class))).thenAnswer(inv -> inv.getArgument(0));

        DrivingFeatureResponseDto dto = featureEngineeringService.calculateFeaturesForTrip(100L);

        // Exactly 1 distinct harsh braking event should be counted, NOT 3!
        assertThat(dto.getHarshBrakingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should debounce rapid acceleration events to prevent double counting")
    void testRapidAccelerationDebounce() {
        Instant t0 = trip.getStartTime();
        // 2 consecutive pings with accel >= +0.30G
        List<TelematicsRecord> records = List.of(
                new TelematicsRecord(vehicle, t0, 12.9716, 77.5946, 10.0, 0.35, 0.0, 2500, 80.0, TelematicsValidationStatus.VALID, "Burst 1"),
                new TelematicsRecord(vehicle, t0.plusSeconds(5), 12.9720, 77.5950, 30.0, 0.40, 0.0, 3200, 80.0, TelematicsValidationStatus.VALID, "Burst 2"),
                new TelematicsRecord(vehicle, t0.plusSeconds(10), 12.9725, 77.5955, 45.0, 0.05, 0.0, 2000, 80.0, TelematicsValidationStatus.VALID, "Cruising")
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(telematicsRepository.findByTripIdOrderByTimestampAsc(100L)).thenReturn(records);
        when(featureRepository.findByTripId(100L)).thenReturn(Optional.empty());
        when(featureRepository.save(any(DrivingFeature.class))).thenAnswer(inv -> inv.getArgument(0));

        DrivingFeatureResponseDto dto = featureEngineeringService.calculateFeaturesForTrip(100L);

        // Exactly 1 rapid acceleration burst
        assertThat(dto.getRapidAccelerationCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should calculate night driving exposure using local timezone (22:00 to 05:00)")
    void testNightDrivingExposure() {
        // 23:00 IST is 17:30 UTC
        Instant nightTimeUtc = Instant.parse("2026-10-15T17:30:00Z");

        List<TelematicsRecord> records = List.of(
                new TelematicsRecord(vehicle, nightTimeUtc, 12.9716, 77.5946, 40.0, 0.0, 0.0, 1500, 80.0, TelematicsValidationStatus.VALID, "Clean"),
                new TelematicsRecord(vehicle, nightTimeUtc.plusSeconds(180), 12.9730, 77.5960, 45.0, 0.0, 0.0, 1600, 80.0, TelematicsValidationStatus.VALID, "Clean")
        );

        trip.setDurationSeconds(180L);
        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(telematicsRepository.findByTripIdOrderByTimestampAsc(100L)).thenReturn(records);
        when(featureRepository.findByTripId(100L)).thenReturn(Optional.empty());
        when(featureRepository.save(any(DrivingFeature.class))).thenAnswer(inv -> inv.getArgument(0));

        DrivingFeatureResponseDto dto = featureEngineeringService.calculateFeaturesForTrip(100L);

        // 180 seconds = 3 minutes of night driving
        assertThat(dto.getNightDrivingMinutes()).isEqualTo(3L);
        assertThat(dto.getNightDrivingRatio()).isCloseTo(1.0, within(0.01));
    }

    @Test
    @DisplayName("Should return null for normalized event rate per 100km when distance is zero or negligible")
    void testZeroDistanceSafeDivision() {
        trip.setDistanceKm(0.0); // Zero distance
        trip.setDistanceMeters(0.0);

        List<TelematicsRecord> records = List.of(
                new TelematicsRecord(vehicle, trip.getStartTime(), 12.9716, 77.5946, 0.0, -0.40, 0.0, 900, 80.0, TelematicsValidationStatus.VALID, "Clean")
        );

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(telematicsRepository.findByTripIdOrderByTimestampAsc(100L)).thenReturn(records);
        when(featureRepository.findByTripId(100L)).thenReturn(Optional.empty());
        when(featureRepository.save(any(DrivingFeature.class))).thenAnswer(inv -> inv.getArgument(0));

        DrivingFeatureResponseDto dto = featureEngineeringService.calculateFeaturesForTrip(100L);

        // Normalized rate must be null rather than ArithmeticException / NaN / Infinity!
        assertThat(dto.getHarshBrakeRatePer100Km()).isNull();
        assertThat(dto.getRapidAccelRatePer100Km()).isNull();
    }

    @Test
    @DisplayName("Should safely update existing feature record on recalculation")
    void testSafeRecalculation() {
        DrivingFeature existing = new DrivingFeature();
        existing.setId(555L);
        existing.setTrip(trip);
        existing.setVehicle(vehicle);

        when(tripRepository.findById(100L)).thenReturn(Optional.of(trip));
        when(telematicsRepository.findByTripIdOrderByTimestampAsc(100L)).thenReturn(List.of());
        when(featureRepository.findByTripId(100L)).thenReturn(Optional.of(existing));
        when(featureRepository.save(any(DrivingFeature.class))).thenAnswer(inv -> inv.getArgument(0));

        DrivingFeatureResponseDto dto = featureEngineeringService.calculateFeaturesForTrip(100L);

        assertThat(dto.getId()).isEqualTo(555L);
        verify(featureRepository, times(1)).save(existing);
    }

    private static org.assertj.core.data.Offset<Double> within(double value) {
        return org.assertj.core.data.Offset.offset(value);
    }
}
