package com.ubi.platform.service;

import com.ubi.platform.dto.TripResponseDto;
import com.ubi.platform.entity.*;
import com.ubi.platform.repository.TelematicsRepository;
import com.ubi.platform.repository.TripRepository;
import com.ubi.platform.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripStitchingServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TelematicsRepository telematicsRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Spy
    private GeoDistanceCalculator geoDistanceCalculator = new GeoDistanceCalculator();

    @InjectMocks
    private TripStitchingService tripStitchingService;

    private Vehicle vehicle1;
    private Vehicle vehicle2;

    @BeforeEach
    void setUp() {
        vehicle1 = new Vehicle("MH02AB1234", "Honda", "City", 2022, 1498, FuelType.PETROL, VehicleType.CAR);
        vehicle1.setId(1L);

        vehicle2 = new Vehicle("KA01CD5678", "Toyota", "Innova", 2021, 2393, FuelType.DIESEL, VehicleType.SUV);
        vehicle2.setId(2L);

        tripStitchingService.setInactivityThresholdMinutes(10);
    }

    @Test
    @DisplayName("Should stitch one continuous trip when intervals are within inactivity threshold")
    void testOneContinuousTrip() {
        Instant start = Instant.now().minus(20, ChronoUnit.MINUTES);
        List<TelematicsRecord> records = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            records.add(new TelematicsRecord(
                    vehicle1, start.plus(i * 10, ChronoUnit.SECONDS),
                    12.9716 + (i * 0.001), 77.5946 + (i * 0.001),
                    45.0, 0.05, 90.0, 1800, 80.0,
                    TelematicsValidationStatus.VALID, "Clean"
            ));
        }

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle1));
        when(telematicsRepository.findByVehicleIdOrderByTimestampAsc(1L)).thenReturn(records);
        when(tripRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<TripResponseDto> trips = tripStitchingService.stitchTripsForVehicle(1L);

        assertThat(trips).hasSize(1);
        assertThat(trips.get(0).getValidRecordCount()).isEqualTo(5);
        assertThat(trips.get(0).getDurationSeconds()).isEqualTo(40L);
        assertThat(trips.get(0).getDistanceMeters()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Should segment into two trips when consecutive records exceed inactivity threshold")
    void testTwoTripsSeparatedByInactivityThreshold() {
        Instant trip1Start = Instant.now().minus(40, ChronoUnit.MINUTES);
        Instant trip2Start = trip1Start.plus(25, ChronoUnit.MINUTES); // 25-minute gap (> 10m threshold)

        List<TelematicsRecord> records = new ArrayList<>();

        // Trip 1 (3 pings)
        for (int i = 0; i < 3; i++) {
            records.add(new TelematicsRecord(
                    vehicle1, trip1Start.plus(i * 10, ChronoUnit.SECONDS),
                    12.9716 + (i * 0.001), 77.5946 + (i * 0.001),
                    40.0, 0.05, 90.0, 1500, 80.0,
                    TelematicsValidationStatus.VALID, "Clean"
            ));
        }

        // Trip 2 (3 pings)
        for (int i = 0; i < 3; i++) {
            records.add(new TelematicsRecord(
                    vehicle1, trip2Start.plus(i * 10, ChronoUnit.SECONDS),
                    12.9800 + (i * 0.001), 77.6000 + (i * 0.001),
                    50.0, 0.08, 90.0, 1900, 78.0,
                    TelematicsValidationStatus.VALID, "Clean"
            ));
        }

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle1));
        when(telematicsRepository.findByVehicleIdOrderByTimestampAsc(1L)).thenReturn(records);
        when(tripRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<TripResponseDto> trips = tripStitchingService.stitchTripsForVehicle(1L);

        assertThat(trips).hasSize(2);
        assertThat(trips.get(0).getValidRecordCount()).isEqualTo(3);
        assertThat(trips.get(1).getValidRecordCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should maintain separate trip boundaries for distinct vehicles")
    void testSeparateVehicles() {
        Instant t = Instant.now().minus(15, ChronoUnit.MINUTES);
        List<TelematicsRecord> v1Records = List.of(
                new TelematicsRecord(vehicle1, t, 12.9716, 77.5946, 30.0, 0.0, 0.0, 1200, 80.0, TelematicsValidationStatus.VALID, "Clean"),
                new TelematicsRecord(vehicle1, t.plusSeconds(30), 12.9720, 77.5950, 35.0, 0.0, 0.0, 1300, 79.9, TelematicsValidationStatus.VALID, "Clean")
        );

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle1));
        when(telematicsRepository.findByVehicleIdOrderByTimestampAsc(1L)).thenReturn(v1Records);
        when(tripRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<TripResponseDto> v1Trips = tripStitchingService.stitchTripsForVehicle(1L);

        assertThat(v1Trips).hasSize(1);
        assertThat(v1Trips.get(0).getVehicleId()).isEqualTo(1L);
        verify(telematicsRepository, never()).findByVehicleIdOrderByTimestampAsc(2L);
    }

    @Test
    @DisplayName("Should sort out-of-order telemetry records chronologically")
    void testOutOfOrderRecords() {
        Instant base = Instant.now().minus(20, ChronoUnit.MINUTES);
        TelematicsRecord r1 = new TelematicsRecord(vehicle1, base.plusSeconds(30), 12.9730, 77.5960, 45.0, 0.0, 0.0, 1600, 80.0, TelematicsValidationStatus.VALID, "Clean");
        TelematicsRecord r2 = new TelematicsRecord(vehicle1, base.plusSeconds(10), 12.9716, 77.5946, 30.0, 0.0, 0.0, 1400, 80.0, TelematicsValidationStatus.VALID, "Clean");
        TelematicsRecord r3 = new TelematicsRecord(vehicle1, base.plusSeconds(20), 12.9720, 77.5950, 40.0, 0.0, 0.0, 1500, 80.0, TelematicsValidationStatus.VALID, "Clean");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle1));
        when(telematicsRepository.findByVehicleIdOrderByTimestampAsc(1L)).thenReturn(List.of(r1, r2, r3));
        when(tripRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<TripResponseDto> trips = tripStitchingService.stitchTripsForVehicle(1L);

        assertThat(trips).hasSize(1);
        assertThat(trips.get(0).getStartTime()).isEqualTo(base.plusSeconds(10));
        assertThat(trips.get(0).getDurationSeconds()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Should exclude invalid telemetry records from distance but track anomalous count")
    void testInvalidTelemetryExclusion() {
        Instant base = Instant.now().minus(20, ChronoUnit.MINUTES);
        TelematicsRecord valid1 = new TelematicsRecord(vehicle1, base, 12.9716, 77.5946, 30.0, 0.0, 0.0, 1200, 80.0, TelematicsValidationStatus.VALID, "Clean");
        TelematicsRecord invalid = new TelematicsRecord(vehicle1, base.plusSeconds(10), 120.0, 200.0, 300.0, 0.0, 0.0, 1200, 80.0, TelematicsValidationStatus.GPS_OUT_OF_BOUNDS, "Invalid GPS");
        TelematicsRecord valid2 = new TelematicsRecord(vehicle1, base.plusSeconds(20), 12.9720, 77.5950, 35.0, 0.0, 0.0, 1300, 79.9, TelematicsValidationStatus.VALID, "Clean");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle1));
        when(telematicsRepository.findByVehicleIdOrderByTimestampAsc(1L)).thenReturn(List.of(valid1, invalid, valid2));
        when(tripRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<TripResponseDto> trips = tripStitchingService.stitchTripsForVehicle(1L);

        assertThat(trips).hasSize(1);
        assertThat(trips.get(0).getValidRecordCount()).isEqualTo(2);
        assertThat(trips.get(0).getAnomalousRecordCount()).isEqualTo(1);
        // Distance calculated only between valid1 and valid2
        assertThat(trips.get(0).getDistanceMeters()).isLessThan(100.0);
    }

    @Test
    @DisplayName("Should detect ongoing active trip when latest ping was within inactivity threshold")
    void testOngoingActiveTrip() {
        // Ping logged 2 minutes ago (< 10 minutes threshold)
        Instant recentTime = Instant.now().minus(2, ChronoUnit.MINUTES);
        List<TelematicsRecord> records = List.of(
                new TelematicsRecord(vehicle1, recentTime.minusSeconds(60), 12.9716, 77.5946, 40.0, 0.0, 0.0, 1500, 80.0, TelematicsValidationStatus.VALID, "Clean"),
                new TelematicsRecord(vehicle1, recentTime, 12.9725, 77.5955, 45.0, 0.0, 0.0, 1600, 80.0, TelematicsValidationStatus.VALID, "Clean")
        );

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle1));
        when(telematicsRepository.findByVehicleIdOrderByTimestampAsc(1L)).thenReturn(records);
        when(tripRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<TripResponseDto> trips = tripStitchingService.stitchTripsForVehicle(1L);

        assertThat(trips).hasSize(1);
        assertThat(trips.get(0).getStatus()).isEqualTo(TripStatus.IN_PROGRESS);
        assertThat(trips.get(0).getEndTime()).isNull();
    }

    @Test
    @DisplayName("Should reprocess telemetry idempotently without duplicate trips")
    void testReprocessingIdempotency() {
        Instant start = Instant.now().minus(30, ChronoUnit.MINUTES);
        List<TelematicsRecord> records = List.of(
                new TelematicsRecord(vehicle1, start, 12.9716, 77.5946, 40.0, 0.0, 0.0, 1500, 80.0, TelematicsValidationStatus.VALID, "Clean"),
                new TelematicsRecord(vehicle1, start.plusSeconds(30), 12.9725, 77.5955, 45.0, 0.0, 0.0, 1600, 80.0, TelematicsValidationStatus.VALID, "Clean")
        );

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle1));
        when(telematicsRepository.findByVehicleIdOrderByTimestampAsc(1L)).thenReturn(records);
        when(tripRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        // First stitch run
        List<TripResponseDto> run1 = tripStitchingService.stitchTripsForVehicle(1L);
        // Second stitch run
        List<TripResponseDto> run2 = tripStitchingService.stitchTripsForVehicle(1L);

        assertThat(run1).hasSize(1);
        assertThat(run2).hasSize(1);
        verify(tripRepository, times(2)).deleteByVehicleId(1L);
    }
}
