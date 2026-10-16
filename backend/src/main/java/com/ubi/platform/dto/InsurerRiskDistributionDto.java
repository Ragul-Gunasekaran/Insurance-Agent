package com.ubi.platform.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Breakdown of risk categories across trips and vehicles (Step 7: Phase 3.B).
 */
public class InsurerRiskDistributionDto {

    private List<CategoryCountDto> tripCategories = new ArrayList<>();
    private List<CategoryCountDto> vehicleCategories = new ArrayList<>();
    private Long totalScoredTrips = 0L;
    private Long totalUnratedTrips = 0L;
    private Long totalScoredVehicles = 0L;
    private String disclaimer = "Prototype score distribution based on observed trips. Excludes trips with insufficient data.";

    public InsurerRiskDistributionDto() {
    }

    public List<CategoryCountDto> getTripCategories() {
        return tripCategories;
    }

    public void setTripCategories(List<CategoryCountDto> tripCategories) {
        this.tripCategories = tripCategories;
    }

    public List<CategoryCountDto> getVehicleCategories() {
        return vehicleCategories;
    }

    public void setVehicleCategories(List<CategoryCountDto> vehicleCategories) {
        this.vehicleCategories = vehicleCategories;
    }

    public Long getTotalScoredTrips() {
        return totalScoredTrips;
    }

    public void setTotalScoredTrips(Long totalScoredTrips) {
        this.totalScoredTrips = totalScoredTrips;
    }

    public Long getTotalUnratedTrips() {
        return totalUnratedTrips;
    }

    public void setTotalUnratedTrips(Long totalUnratedTrips) {
        this.totalUnratedTrips = totalUnratedTrips;
    }

    public Long getTotalScoredVehicles() {
        return totalScoredVehicles;
    }

    public void setTotalScoredVehicles(Long totalScoredVehicles) {
        this.totalScoredVehicles = totalScoredVehicles;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
