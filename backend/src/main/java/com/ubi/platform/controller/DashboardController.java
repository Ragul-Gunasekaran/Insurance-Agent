package com.ubi.platform.controller;

import com.ubi.platform.dto.*;
import com.ubi.platform.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing Customer Driver Scorecards and Prototype Insurer Analytics (Step 7).
 * Follows hackathon prototype conventions, avoiding real underwriting claims or fabricated RBAC.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Customer Driver Scorecard summary for a specific vehicle.
     */
    @GetMapping("/customer/vehicles/{vehicleId}")
    public ResponseEntity<ApiResponse<CustomerScorecardSummaryDto>> getCustomerScorecard(
            @PathVariable Long vehicleId) {
        CustomerScorecardSummaryDto scorecard = dashboardService.getCustomerScorecard(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(scorecard));
    }

    /**
     * Paginated customer trip history with embedded driving features and risk scores.
     */
    @GetMapping("/customer/vehicles/{vehicleId}/trips")
    public ResponseEntity<ApiResponse<PageResponse<CustomerTripSummaryDto>>> getCustomerTrips(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        PageResponse<CustomerTripSummaryDto> trips = dashboardService.getCustomerTrips(vehicleId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(trips));
    }

    /**
     * Insurer Analytics: Fleet Overview macro metrics.
     */
    @GetMapping("/insurer/overview")
    public ResponseEntity<ApiResponse<InsurerFleetOverviewDto>> getInsurerFleetOverview() {
        InsurerFleetOverviewDto overview = dashboardService.getInsurerFleetOverview();
        return ResponseEntity.ok(ApiResponse.ok(overview));
    }

    /**
     * Insurer Analytics: Risk distribution across trips and vehicles.
     */
    @GetMapping("/insurer/risk-distribution")
    public ResponseEntity<ApiResponse<InsurerRiskDistributionDto>> getInsurerRiskDistribution() {
        InsurerRiskDistributionDto distribution = dashboardService.getInsurerRiskDistribution();
        return ResponseEntity.ok(ApiResponse.ok(distribution));
    }

    /**
     * Insurer Analytics: Fleet-wide driving behavior and weighted event rates.
     */
    @GetMapping("/insurer/behavior")
    public ResponseEntity<ApiResponse<InsurerDrivingBehaviorDto>> getInsurerDrivingBehavior() {
        InsurerDrivingBehaviorDto behavior = dashboardService.getInsurerDrivingBehavior();
        return ResponseEntity.ok(ApiResponse.ok(behavior));
    }

    /**
     * Insurer Analytics: Fleet mileage coverage and usage distributions.
     */
    @GetMapping("/insurer/usage")
    public ResponseEntity<ApiResponse<InsurerUsageAnalyticsDto>> getInsurerUsageAnalytics() {
        InsurerUsageAnalyticsDto usage = dashboardService.getInsurerUsageAnalytics();
        return ResponseEntity.ok(ApiResponse.ok(usage));
    }

    /**
     * Insurer Analytics: Actuarial premium quote analytics, average, and median.
     */
    @GetMapping("/insurer/pricing")
    public ResponseEntity<ApiResponse<InsurerPricingAnalyticsDto>> getInsurerPricingAnalytics() {
        InsurerPricingAnalyticsDto pricing = dashboardService.getInsurerPricingAnalytics();
        return ResponseEntity.ok(ApiResponse.ok(pricing));
    }
}
