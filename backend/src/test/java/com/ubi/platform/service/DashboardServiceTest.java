package com.ubi.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.*;
import com.ubi.platform.entity.*;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private DrivingFeatureRepository drivingFeatureRepository;

    @Mock
    private RiskScoreRepository riskScoreRepository;

    @Mock
    private PremiumQuoteRepository premiumQuoteRepository;

    @Mock
    private TelematicsRepository telematicsRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private DashboardService dashboardService;

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle("MH12DE1234", "Tata", "Nexon", 2023, 1199, FuelType.PETROL, VehicleType.CAR);
        vehicle.setId(101L);
        vehicle.setVerificationStatus(VerificationStatus.VERIFIED);
    }

    @Test
    @DisplayName("Empty Fleet: Overview returns safe zeros and null average score")
    void testEmptyFleet_overviewReturnsZerosAndNullAvg() {
        when(vehicleRepository.count()).thenReturn(0L);
        when(telematicsRepository.findDistinctVehicleIdsWithTelematics()).thenReturn(Collections.emptyList());
        when(tripRepository.findDistinctVehicleIdsWithTrips()).thenReturn(Collections.emptyList());
        when(tripRepository.countByStatus(TripStatus.COMPLETED)).thenReturn(0L);
        when(tripRepository.sumDistanceByStatus(TripStatus.COMPLETED)).thenReturn(0.0);
        when(riskScoreRepository.countByIsScorableTrue()).thenReturn(0L);
        when(riskScoreRepository.countByIsScorableFalse()).thenReturn(0L);
        when(premiumQuoteRepository.findDistinctVehicleIdsWithEligibleQuotes()).thenReturn(Collections.emptyList());
        when(riskScoreRepository.findAverageSafetyScore()).thenReturn(null);

        InsurerFleetOverviewDto overview = dashboardService.getInsurerFleetOverview();

        assertNotNull(overview);
        assertEquals(0L, overview.getTotalRegisteredVehicles());
        assertEquals(0L, overview.getVehiclesWithTelematics());
        assertEquals(0L, overview.getCompletedTripsCount());
        assertEquals(0.0, overview.getTotalObservedDistanceKm());
        assertNull(overview.getAverageSafetyScore());
        assertTrue(overview.getDisclaimer().contains("Prototype"));
    }

    @Test
    @DisplayName("Empty Fleet: Pricing analytics handles empty database safely")
    void testEmptyFleet_pricingReturnsZeroQuotesAndNullAverages() {
        when(premiumQuoteRepository.countByIsEligibleTrue()).thenReturn(0L);
        when(premiumQuoteRepository.countByIsEligibleFalse()).thenReturn(0L);
        when(premiumQuoteRepository.findAllEligiblePremiumsSorted()).thenReturn(Collections.emptyList());
        when(premiumQuoteRepository.countEligibleQuotesGroupByRiskCategory()).thenReturn(Collections.emptyList());
        when(premiumQuoteRepository.countQuotesGroupByPricingVersion()).thenReturn(Collections.emptyList());

        InsurerPricingAnalyticsDto pricing = dashboardService.getInsurerPricingAnalytics();

        assertNotNull(pricing);
        assertEquals(0L, pricing.getTotalQuotes());
        assertEquals(0L, pricing.getEligibleQuotesCount());
        assertNull(pricing.getAverageEstimatedPremiumInr());
        assertNull(pricing.getMedianEstimatedPremiumInr());
        assertNull(pricing.getMinEstimatedPremiumInr());
        assertNull(pricing.getMaxEstimatedPremiumInr());
        assertTrue(pricing.getQuotedRiskDistribution().isEmpty());
    }

    @Test
    @DisplayName("Vehicle Without Trips: Returns NOT_AVAILABLE score, zero distance, and safe defaults")
    void testVehicleWithoutTrips_returnsNotAvailableScoreAndZeroDistance() {
        when(vehicleRepository.findById(101L)).thenReturn(Optional.of(vehicle));
        when(tripRepository.countByVehicleIdAndStatus(101L, TripStatus.COMPLETED)).thenReturn(0L);
        when(tripRepository.sumDistanceByVehicleIdAndStatus(101L, TripStatus.COMPLETED)).thenReturn(0.0);
        when(riskScoreRepository.findByVehicleIdOrderByCalculatedAtDesc(101L)).thenReturn(Collections.emptyList());
        when(drivingFeatureRepository.findVehicleFeatureSums(101L)).thenReturn(Collections.emptyList());
        when(premiumQuoteRepository.findTopByVehicleIdOrderByCalculatedAtDesc(101L)).thenReturn(Optional.empty());

        CustomerScorecardSummaryDto scorecard = dashboardService.getCustomerScorecard(101L);

        assertNotNull(scorecard);
        assertEquals("MH12DE1234", scorecard.getVehicleNumber());
        assertEquals(0L, scorecard.getCompletedTripsCount());
        assertEquals(0.0, scorecard.getTotalObservedDistanceKm());
        assertNull(scorecard.getLatestSafetyScore(), "Safety score must be null when no scores exist");
        assertEquals("NOT_AVAILABLE", scorecard.getLatestRiskCategory());
        assertEquals(0.0, scorecard.getHarshBrakeRatePer100Km());
        assertNull(scorecard.getLatestEstimatedAnnualPremiumInr());
    }

    @Test
    @DisplayName("Trip Without Risk Score: Trip summary marks isScorable=false, category UNRATED, null safetyScore")
    void testTripWithoutRiskScore_customerTripsShowsUnratedAndNullScore() {
        when(vehicleRepository.existsById(101L)).thenReturn(true);

        Trip trip = new Trip(vehicle, Instant.parse("2026-10-15T10:00:00Z"), Instant.parse("2026-10-15T10:30:00Z"),
                18.5204, 73.8567, 18.5500, 73.8800, 1800L, 15000.0, 50, 0, TripStatus.COMPLETED);
        trip.setId(201L);

        Page<Trip> tripPage = new PageImpl<>(List.of(trip), PageRequest.of(0, 10), 1);
        when(tripRepository.findByVehicleIdOrderByStartTimeDesc(eq(101L), any())).thenReturn(tripPage);
        when(drivingFeatureRepository.findByTripId(201L)).thenReturn(Optional.empty());
        when(riskScoreRepository.findByTripId(201L)).thenReturn(Optional.empty());

        PageResponse<CustomerTripSummaryDto> response = dashboardService.getCustomerTrips(101L, PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        CustomerTripSummaryDto item = response.getContent().get(0);
        assertEquals(201L, item.getTripId());
        assertFalse(item.getIsScorable());
        assertEquals("UNRATED", item.getRiskCategory());
        assertNull(item.getSafetyScore(), "Safety score must be null for unscored trips");
    }

    @Test
    @DisplayName("UNRATED Score: Does NOT return zero or 100, sets riskCategory=UNRATED and unratedReason")
    void testUnratedRiskScore_doesNotReturnZeroOr100_returnsNullScoreAndUnratedCategory() {
        when(vehicleRepository.findById(101L)).thenReturn(Optional.of(vehicle));
        when(tripRepository.countByVehicleIdAndStatus(101L, TripStatus.COMPLETED)).thenReturn(1L);
        when(tripRepository.sumDistanceByVehicleIdAndStatus(101L, TripStatus.COMPLETED)).thenReturn(0.8);

        Trip trip = new Trip(vehicle, Instant.now().minusSeconds(3600), Instant.now(),
                18.5204, 73.8567, 18.5500, 73.8800, 120L, 800.0, 10, 0, TripStatus.COMPLETED);
        trip.setId(301L);

        RiskScore unratedScore = new RiskScore(trip, vehicle, null, "UNRATED",
                "v1.0", "POOR", false,
                0.0, 0.0, 0.0, 0.0, 0.0,
                "Trip distance below minimum 1.0 km required for reliable behavioral scoring.", "[]");

        when(riskScoreRepository.findByVehicleIdOrderByCalculatedAtDesc(101L)).thenReturn(List.of(unratedScore));
        when(drivingFeatureRepository.findVehicleFeatureSums(101L)).thenReturn(Collections.emptyList());
        when(premiumQuoteRepository.findTopByVehicleIdOrderByCalculatedAtDesc(101L)).thenReturn(Optional.empty());

        CustomerScorecardSummaryDto scorecard = dashboardService.getCustomerScorecard(101L);

        assertNotNull(scorecard);
        assertNull(scorecard.getLatestSafetyScore(), "Must NOT return 0 or 100 for an unrated trip");
        assertEquals("UNRATED", scorecard.getLatestRiskCategory());
        assertNotNull(scorecard.getUnratedReason());
        assertTrue(scorecard.getUnratedReason().contains("minimum 1.0 km"));
    }

    @Test
    @DisplayName("Risk Distribution: Calculates exact counts and percentages across categories and unrated trips")
    void testCorrectRiskCategoryCountsAndPercentages_forFleet() {
        // Mock trips: 5 EXCELLENT, 3 GOOD, 2 MODERATE, 0 HIGH_RISK, 2 UNRATED -> total 12
        List<Object[]> tripGroupMock = new ArrayList<>();
        tripGroupMock.add(new Object[]{"EXCELLENT", 5L});
        tripGroupMock.add(new Object[]{"GOOD", 3L});
        tripGroupMock.add(new Object[]{"MODERATE", 2L});
        when(riskScoreRepository.countGroupByScoreCategory()).thenReturn(tripGroupMock);
        when(riskScoreRepository.countByIsScorableFalse()).thenReturn(2L);
        when(vehicleRepository.findAll()).thenReturn(Collections.emptyList());

        InsurerRiskDistributionDto dist = dashboardService.getInsurerRiskDistribution();

        assertNotNull(dist);
        assertEquals(10L, dist.getTotalScoredTrips());
        assertEquals(2L, dist.getTotalUnratedTrips());

        List<CategoryCountDto> tripCats = dist.getTripCategories();
        assertEquals(5, tripCats.size()); // EXCELLENT, GOOD, MODERATE, HIGH_RISK, UNRATED

        CategoryCountDto exc = tripCats.stream().filter(c -> c.getCategory().equals("EXCELLENT")).findFirst().orElseThrow();
        assertEquals(5L, exc.getCount());
        // 5 / 12 * 100 = 41.7%
        assertEquals(41.7, exc.getPercentage());

        CategoryCountDto unrated = tripCats.stream().filter(c -> c.getCategory().equals("UNRATED")).findFirst().orElseThrow();
        assertEquals(2L, unrated.getCount());
        // 2 / 12 * 100 = 16.7%
        assertEquals(16.7, unrated.getPercentage());
    }

    @Test
    @DisplayName("Median and Average Calculation: Accurately computes odd and even quote distributions")
    void testMedianAndAverageCalculation_withOddAndEvenEligibleQuotes() {
        // Odd count test: [10000, 12000, 18000] -> avg = 13333.33, median = 12000.00
        when(premiumQuoteRepository.countByIsEligibleTrue()).thenReturn(3L);
        when(premiumQuoteRepository.countByIsEligibleFalse()).thenReturn(1L);
        when(premiumQuoteRepository.findAllEligiblePremiumsSorted()).thenReturn(List.of(
                new BigDecimal("10000.00"),
                new BigDecimal("12000.00"),
                new BigDecimal("18000.00")
        ));
        when(premiumQuoteRepository.countEligibleQuotesGroupByRiskCategory()).thenReturn(Collections.emptyList());
        when(premiumQuoteRepository.countQuotesGroupByPricingVersion()).thenReturn(Collections.emptyList());

        InsurerPricingAnalyticsDto oddPricing = dashboardService.getInsurerPricingAnalytics();
        assertEquals(new BigDecimal("12000.00"), oddPricing.getMedianEstimatedPremiumInr());
        assertEquals(new BigDecimal("13333.33"), oddPricing.getAverageEstimatedPremiumInr());
        assertEquals(new BigDecimal("10000.00"), oddPricing.getMinEstimatedPremiumInr());
        assertEquals(new BigDecimal("18000.00"), oddPricing.getMaxEstimatedPremiumInr());

        // Even count test: [10000, 12000, 14000, 20000] -> avg = 14000.00, median = (12000+14000)/2 = 13000.00
        when(premiumQuoteRepository.findAllEligiblePremiumsSorted()).thenReturn(List.of(
                new BigDecimal("10000.00"),
                new BigDecimal("12000.00"),
                new BigDecimal("14000.00"),
                new BigDecimal("20000.00")
        ));
        InsurerPricingAnalyticsDto evenPricing = dashboardService.getInsurerPricingAnalytics();
        assertEquals(new BigDecimal("13000.00"), evenPricing.getMedianEstimatedPremiumInr());
        assertEquals(new BigDecimal("14000.00"), evenPricing.getAverageEstimatedPremiumInr());
    }

    @Test
    @DisplayName("Distance-Weighted Rates: Correctly weights harsh brake, accel, speeding and anomaly rates")
    void testDistanceWeightedBehaviorRates() {
        // [sumHarshBraking, sumRapidAccel, sumSpeeding, sumNightMinutes, sumValidRecords, sumAnomalousRecords, sumDistanceKm, sumDurationSec, countFeatures]
        Object[] row = new Object[]{4L, 2L, 8L, 30L, 950L, 50L, 200.0, 7200L, 5L};
        List<Object[]> sumsList = new ArrayList<>();
        sumsList.add(row);
        when(drivingFeatureRepository.findFleetFeatureSums()).thenReturn(sumsList);

        InsurerDrivingBehaviorDto behavior = dashboardService.getInsurerDrivingBehavior();

        assertNotNull(behavior);
        assertEquals(4L, behavior.getTotalHarshBrakingEvents());
        assertEquals(2L, behavior.getTotalRapidAccelerationEvents());
        assertEquals(8L, behavior.getTotalSpeedingObservations());
        assertEquals(200.0, behavior.getTotalDistanceEvaluatedKm());
        // 4 / 200 * 100 = 2.0 per 100km
        assertEquals(2.0, behavior.getFleetHarshBrakeRatePer100Km());
        // 2 / 200 * 100 = 1.0 per 100km
        assertEquals(1.0, behavior.getFleetRapidAccelRatePer100Km());
        // 8 / 200 * 100 = 4.0 per 100km
        assertEquals(4.0, behavior.getFleetSpeedingObsPer100Km());
        // Anomaly pct = 50 / 1000 * 100 = 5.0%
        assertEquals(5.0, behavior.getFleetAnomalyRatePct());
    }

    @Test
    @DisplayName("ResourceNotFoundException when vehicle not found")
    void testVehicleNotFound_throwsException() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> dashboardService.getCustomerScorecard(999L));
    }
}
