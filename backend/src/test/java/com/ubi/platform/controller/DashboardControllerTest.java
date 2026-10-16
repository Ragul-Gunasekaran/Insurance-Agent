package com.ubi.platform.controller;

import com.ubi.platform.dto.*;
import com.ubi.platform.exception.GlobalExceptionHandler;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("GET /api/v1/dashboard/customer/vehicles/{id} - Returns customer scorecard")
    void testGetCustomerScorecard_returnsOk() throws Exception {
        CustomerScorecardSummaryDto dto = new CustomerScorecardSummaryDto();
        dto.setVehicleId(1L);
        dto.setVehicleNumber("MH02AB1234");
        dto.setMake("Honda");
        dto.setModel("City");
        dto.setCompletedTripsCount(5L);
        dto.setTotalObservedDistanceKm(120.5);
        dto.setLatestSafetyScore(86.5);
        dto.setLatestRiskCategory("EXCELLENT");

        when(dashboardService.getCustomerScorecard(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/dashboard/customer/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicleNumber").value("MH02AB1234"))
                .andExpect(jsonPath("$.data.latestSafetyScore").value(86.5))
                .andExpect(jsonPath("$.data.latestRiskCategory").value("EXCELLENT"));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/customer/vehicles/{id}/trips - Returns paginated trips")
    void testGetCustomerTrips_returnsOk() throws Exception {
        CustomerTripSummaryDto tripDto = new CustomerTripSummaryDto();
        tripDto.setTripId(101L);
        tripDto.setDistanceKm(15.2);
        tripDto.setDurationSeconds(1200L);
        tripDto.setSafetyScore(84.0);
        tripDto.setRiskCategory("EXCELLENT");

        PageResponse<CustomerTripSummaryDto> pageResponse = new PageResponse<>(
                List.of(tripDto), 0, 10, 1L, 1, true, true
        );

        when(dashboardService.getCustomerTrips(eq(1L), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/dashboard/customer/vehicles/1/trips?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].tripId").value(101))
                .andExpect(jsonPath("$.data.content[0].safetyScore").value(84.0));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/insurer/overview - Returns fleet overview")
    void testGetInsurerFleetOverview_returnsOk() throws Exception {
        InsurerFleetOverviewDto dto = new InsurerFleetOverviewDto();
        dto.setTotalRegisteredVehicles(10L);
        dto.setVehiclesWithTelematics(8L);
        dto.setCompletedTripsCount(45L);
        dto.setTotalObservedDistanceKm(850.0);
        dto.setAverageSafetyScore(82.4);

        when(dashboardService.getInsurerFleetOverview()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/dashboard/insurer/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRegisteredVehicles").value(10))
                .andExpect(jsonPath("$.data.completedTripsCount").value(45))
                .andExpect(jsonPath("$.data.averageSafetyScore").value(82.4));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/insurer/risk-distribution - Returns category distributions")
    void testGetInsurerRiskDistribution_returnsOk() throws Exception {
        InsurerRiskDistributionDto dto = new InsurerRiskDistributionDto();
        dto.setTotalScoredTrips(40L);
        dto.setTotalUnratedTrips(5L);
        dto.setTripCategories(List.of(
                new CategoryCountDto("EXCELLENT", 20L, 44.4),
                new CategoryCountDto("UNRATED", 5L, 11.1)
        ));

        when(dashboardService.getInsurerRiskDistribution()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/dashboard/insurer/risk-distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalScoredTrips").value(40))
                .andExpect(jsonPath("$.data.tripCategories[0].category").value("EXCELLENT"));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/insurer/behavior - Returns aggregated driving metrics")
    void testGetInsurerDrivingBehavior_returnsOk() throws Exception {
        InsurerDrivingBehaviorDto dto = new InsurerDrivingBehaviorDto();
        dto.setTotalHarshBrakingEvents(12L);
        dto.setFleetHarshBrakeRatePer100Km(1.8);
        dto.setFleetRapidAccelRatePer100Km(1.2);

        when(dashboardService.getInsurerDrivingBehavior()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/dashboard/insurer/behavior"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalHarshBrakingEvents").value(12))
                .andExpect(jsonPath("$.data.fleetHarshBrakeRatePer100Km").value(1.8));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/insurer/usage - Returns mileage analytics")
    void testGetInsurerUsageAnalytics_returnsOk() throws Exception {
        InsurerUsageAnalyticsDto dto = new InsurerUsageAnalyticsDto();
        dto.setTotalObservedDistanceKm(1250.0);
        dto.setVehiclesWithSufficientUsage(6L);
        dto.setVehiclesWithInsufficientUsage(2L);

        when(dashboardService.getInsurerUsageAnalytics()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/dashboard/insurer/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalObservedDistanceKm").value(1250.0))
                .andExpect(jsonPath("$.data.vehiclesWithSufficientUsage").value(6));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/insurer/pricing - Returns quote pricing analytics")
    void testGetInsurerPricingAnalytics_returnsOk() throws Exception {
        InsurerPricingAnalyticsDto dto = new InsurerPricingAnalyticsDto();
        dto.setTotalQuotes(10L);
        dto.setEligibleQuotesCount(8L);
        dto.setAverageEstimatedPremiumInr(new BigDecimal("11500.00"));
        dto.setMedianEstimatedPremiumInr(new BigDecimal("11200.00"));

        when(dashboardService.getInsurerPricingAnalytics()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/dashboard/insurer/pricing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalQuotes").value(10))
                .andExpect(jsonPath("$.data.averageEstimatedPremiumInr").value(11500.00))
                .andExpect(jsonPath("$.data.medianEstimatedPremiumInr").value(11200.00));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/customer/vehicles/999 - Returns 404 when vehicle not found")
    void testGetCustomerScorecard_notFound() throws Exception {
        when(dashboardService.getCustomerScorecard(999L))
                .thenThrow(new ResourceNotFoundException("Vehicle not found with ID: 999"));

        mockMvc.perform(get("/api/v1/dashboard/customer/vehicles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Vehicle not found with ID: 999"));
    }
}
