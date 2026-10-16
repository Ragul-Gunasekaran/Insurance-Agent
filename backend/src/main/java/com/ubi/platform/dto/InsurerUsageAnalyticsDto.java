package com.ubi.platform.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Observed fleet mileage and coverage analytics (Step 7: Phase 3.D).
 */
public class InsurerUsageAnalyticsDto {

    private Double totalObservedDistanceKm = 0.0;
    private Long totalCompletedTrips = 0L;
    private Long vehiclesWithSufficientUsage = 0L;
    private Long vehiclesWithInsufficientUsage = 0L;
    private List<VehicleMileageSummaryDto> vehicleMileageSummaries = new ArrayList<>();
    private String disclaimer = "Observed distance is derived solely from stitched trips. Annualized projections are separate illustrative estimates.";

    public InsurerUsageAnalyticsDto() {
    }

    public Double getTotalObservedDistanceKm() {
        return totalObservedDistanceKm;
    }

    public void setTotalObservedDistanceKm(Double totalObservedDistanceKm) {
        this.totalObservedDistanceKm = totalObservedDistanceKm;
    }

    public Long getTotalCompletedTrips() {
        return totalCompletedTrips;
    }

    public void setTotalCompletedTrips(Long totalCompletedTrips) {
        this.totalCompletedTrips = totalCompletedTrips;
    }

    public Long getVehiclesWithSufficientUsage() {
        return vehiclesWithSufficientUsage;
    }

    public void setVehiclesWithSufficientUsage(Long vehiclesWithSufficientUsage) {
        this.vehiclesWithSufficientUsage = vehiclesWithSufficientUsage;
    }

    public Long getVehiclesWithInsufficientUsage() {
        return vehiclesWithInsufficientUsage;
    }

    public void setVehiclesWithInsufficientUsage(Long vehiclesWithInsufficientUsage) {
        this.vehiclesWithInsufficientUsage = vehiclesWithInsufficientUsage;
    }

    public List<VehicleMileageSummaryDto> getVehicleMileageSummaries() {
        return vehicleMileageSummaries;
    }

    public void setVehicleMileageSummaries(List<VehicleMileageSummaryDto> vehicleMileageSummaries) {
        this.vehicleMileageSummaries = vehicleMileageSummaries;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
