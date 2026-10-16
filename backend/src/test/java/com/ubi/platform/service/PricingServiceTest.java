package com.ubi.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.PremiumQuoteResponseDto;
import com.ubi.platform.dto.PricingCalculationRequestDto;
import com.ubi.platform.dto.PricingConfigurationResponseDto;
import com.ubi.platform.entity.*;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.PremiumQuoteRepository;
import com.ubi.platform.repository.RiskScoreRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PremiumQuoteRepository quoteRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RiskScoreRepository riskScoreRepository;

    @Spy
    private PricingCalculator pricingCalculator = new PricingCalculator();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PricingService pricingService;

    private Vehicle vehicle;
    private Trip trip;
    private RiskScore riskScore;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle("MH02AB1234", "Honda", "City", 2022, 1498, FuelType.PETROL, VehicleType.CAR);
        vehicle.setId(1L);

        trip = new Trip(
                vehicle,
                Instant.parse("2026-10-01T08:00:00Z"),
                Instant.parse("2026-10-01T09:00:00Z"),
                18.5204, 73.8567,
                18.5500, 73.8800,
                3600L,
                40000.0,
                120,
                0,
                TripStatus.COMPLETED
        );
        trip.setId(10L);
        trip.setFeaturesCalculated(true);
        trip.setScoreCalculated(true);

        riskScore = new RiskScore();
        riskScore.setId(100L);
        riskScore.setTrip(trip);
        riskScore.setVehicle(vehicle);
        riskScore.setScore(90.0);
        riskScore.setScoreCategory("EXCELLENT");
        riskScore.setIsScorable(true);
        riskScore.setDataQualityStatus("SUFFICIENT");
        riskScore.setCalculatedAt(Instant.now());
    }

    @Test
    @DisplayName("Successfully calculates quote with user distance assumption")
    void testCalculateQuoteForVehicleSuccess() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(tripRepository.findByVehicleIdOrderByStartTimeAsc(1L)).thenReturn(List.of(trip));
        when(riskScoreRepository.findByTripId(10L)).thenReturn(Optional.of(riskScore));
        when(quoteRepository.save(any(PremiumQuote.class))).thenAnswer(inv -> {
            PremiumQuote q = inv.getArgument(0);
            q.setId(500L);
            return q;
        });

        PricingCalculationRequestDto req = new PricingCalculationRequestDto(10000.0);
        PremiumQuoteResponseDto quote = pricingService.calculateQuoteForVehicle(1L, req);

        assertNotNull(quote);
        assertEquals("READY", quote.getStatus());
        assertTrue(quote.getIsEligible());
        assertEquals(90.0, quote.getSafetyScore());
        assertEquals("EXCELLENT", quote.getRiskCategory());
        assertEquals(new BigDecimal("0.9000"), quote.getRiskMultiplier());
        assertEquals(new BigDecimal("1.0000"), quote.getUsageMultiplier());
        // 12000 * 0.90 * 1.00 = 10800.00
        assertEquals(new BigDecimal("10800.00"), quote.getFinalAnnualPremiumInr());
        assertEquals("USER_SPECIFIED", quote.getAnnualDistanceSource());

        verify(quoteRepository).save(any(PremiumQuote.class));
    }

    @Test
    @DisplayName("Excludes in-progress trips from scoring eligibility")
    void testCalculateQuoteExcludesInProgressTrips() {
        trip.setStatus(TripStatus.IN_PROGRESS);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(tripRepository.findByVehicleIdOrderByStartTimeAsc(1L)).thenReturn(List.of(trip));
        when(quoteRepository.save(any(PremiumQuote.class))).thenAnswer(inv -> inv.getArgument(0));

        PricingCalculationRequestDto req = new PricingCalculationRequestDto(10000.0);
        PremiumQuoteResponseDto quote = pricingService.calculateQuoteForVehicle(1L, req);

        assertNotNull(quote);
        assertEquals("INSUFFICIENT_RISK_DATA", quote.getStatus());
        assertFalse(quote.getIsEligible());
        assertNull(quote.getFinalAnnualPremiumInr());
        assertEquals(1, quote.getExcludedTripsCount());
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when vehicle not found")
    void testCalculateQuoteThrowsWhenVehicleNotFound() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pricingService.calculateQuoteForVehicle(999L, null));
    }

    @Test
    @DisplayName("Returns pricing configuration successfully")
    void testGetPricingConfiguration() {
        PricingConfigurationResponseDto config = pricingService.getPricingConfiguration();
        assertNotNull(config);
        assertEquals(new BigDecimal("12000.00"), config.getBaseAnnualPremiumInr());
        assertEquals("INR", config.getCurrency());
        assertNotNull(config.getRiskMultipliers());
        assertEquals(new BigDecimal("0.9000"), config.getRiskMultipliers().get("EXCELLENT"));
    }
}
