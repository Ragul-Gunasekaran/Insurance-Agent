package com.ubi.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.RiskScoreResponseDto;
import com.ubi.platform.entity.*;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.DrivingFeatureRepository;
import com.ubi.platform.repository.RiskScoreRepository;
import com.ubi.platform.repository.TripRepository;
import com.ubi.platform.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RiskScoringService verifying orchestration, safe recalculation, and exception handling (Step 5).
 */
@ExtendWith(MockitoExtension.class)
class RiskScoringServiceTest {

    @Mock
    private RiskScoreRepository riskScoreRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DrivingFeatureRepository featureRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Spy
    private RiskScoringCalculator scoringCalculator = new RiskScoringCalculator();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RiskScoringService riskScoringService;

    private Vehicle vehicle;
    private Trip trip;
    private DrivingFeature feature;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle("MH02AB1234", "Honda", "City", 2022, 1498, FuelType.PETROL, VehicleType.CAR);
        vehicle.setId(1L);

        trip = new Trip(
                vehicle,
                Instant.parse("2026-10-15T08:00:00Z"),
                Instant.parse("2026-10-15T08:30:00Z"),
                18.5204, 73.8567,
                18.5500, 73.8800,
                1800L,
                20000.0,
                100,
                0,
                TripStatus.COMPLETED
        );
        trip.setId(10L);

        feature = new DrivingFeature(
                trip,
                vehicle,
                "v1.0",
                20.0,
                1800L,
                45.0,
                70.0,
                1,
                1,
                0,
                5.0,
                5.0,
                0.0,
                0L,
                0.0,
                100,
                0,
                100.0,
                "EXCELLENT"
        );
        feature.setId(100L);
    }

    @Test
    void testCalculateScoreForTripSuccess() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(featureRepository.findByTripId(10L)).thenReturn(Optional.of(feature));
        when(riskScoreRepository.findByTripId(10L)).thenReturn(Optional.empty());
        when(riskScoreRepository.save(any(RiskScore.class))).thenAnswer(invocation -> {
            RiskScore saved = invocation.getArgument(0);
            saved.setId(500L);
            return saved;
        });

        RiskScoreResponseDto response = riskScoringService.calculateScoreForTrip(10L);

        assertNotNull(response);
        assertEquals(10L, response.getTripId());
        assertEquals(1L, response.getVehicleId());
        assertTrue(response.getIsScorable());
        assertEquals("SUFFICIENT", response.getDataQualityStatus());
        assertNotNull(response.getScore());
        assertTrue(response.getScore() > 0 && response.getScore() <= 100);
        assertTrue(trip.getScoreCalculated());

        verify(tripRepository).save(trip);
        verify(riskScoreRepository).save(any(RiskScore.class));
    }

    @Test
    void testSafeRecalculationUpdatesExistingScoreInPlace() {
        RiskScore existing = new RiskScore();
        existing.setId(500L);
        existing.setTrip(trip);
        existing.setVehicle(vehicle);
        existing.setScore(50.0);

        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(featureRepository.findByTripId(10L)).thenReturn(Optional.of(feature));
        when(riskScoreRepository.findByTripId(10L)).thenReturn(Optional.of(existing));
        when(riskScoreRepository.save(any(RiskScore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RiskScoreResponseDto response = riskScoringService.calculateScoreForTrip(10L);

        assertNotNull(response);
        assertEquals(500L, response.getId()); // Retains original database ID
        verify(riskScoreRepository, never()).delete(any());
        verify(riskScoreRepository).save(existing);
    }

    @Test
    void testCalculateScoreThrowsWhenTripNotFound() {
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> riskScoringService.calculateScoreForTrip(999L));
    }

    @Test
    void testCalculateScoreThrowsWhenFeaturesNotFound() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(featureRepository.findByTripId(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> riskScoringService.calculateScoreForTrip(10L));
    }

    @Test
    void testGetScoreForTripThrowsWhenNotCalculated() {
        when(riskScoreRepository.findByTripId(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> riskScoringService.getScoreForTrip(10L));
    }

    @Test
    void testCalculateScoresForVehicleBatch() {
        when(vehicleRepository.existsById(1L)).thenReturn(true);
        when(tripRepository.findByVehicleIdOrderByStartTimeAsc(1L)).thenReturn(List.of(trip));
        when(featureRepository.existsByTripId(10L)).thenReturn(true);
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(featureRepository.findByTripId(10L)).thenReturn(Optional.of(feature));
        when(riskScoreRepository.findByTripId(10L)).thenReturn(Optional.empty());
        when(riskScoreRepository.save(any(RiskScore.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<RiskScoreResponseDto> results = riskScoringService.calculateScoresForVehicle(1L);

        assertEquals(1, results.size());
        assertEquals(10L, results.get(0).getTripId());
    }
}
