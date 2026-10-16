package com.ubi.platform.dto;

import java.time.Instant;

/**
 * Aggregated metrics for Insurer Fleet Overview (Step 7: Phase 3.A).
 */
public class InsurerFleetOverviewDto {

    private Long totalRegisteredVehicles = 0L;
    private Long vehiclesWithTelematics = 0L;
    private Long vehiclesWithTrips = 0L;
    private Long completedTripsCount = 0L;
    private Double totalObservedDistanceKm = 0.0;
    private Long scorableTripsCount = 0L;
    private Long unratedTripsCount = 0L;
    private Long vehiclesWithEligibleQuotes = 0L;
    private Double averageSafetyScore;
    private Instant auditTimestamp;
    private String disclaimer = "Simulated Insurer Demonstration — Public Prototype View. No active policy underwriting or regulatory approval.";

    public InsurerFleetOverviewDto() {
        this.auditTimestamp = Instant.now();
    }

    public Long getTotalRegisteredVehicles() {
        return totalRegisteredVehicles;
    }

    public void setTotalRegisteredVehicles(Long totalRegisteredVehicles) {
        this.totalRegisteredVehicles = totalRegisteredVehicles;
    }

    public Long getVehiclesWithTelematics() {
        return vehiclesWithTelematics;
    }

    public void setVehiclesWithTelematics(Long vehiclesWithTelematics) {
        this.vehiclesWithTelematics = vehiclesWithTelematics;
    }

    public Long getVehiclesWithTrips() {
        return vehiclesWithTrips;
    }

    public void setVehiclesWithTrips(Long vehiclesWithTrips) {
        this.vehiclesWithTrips = vehiclesWithTrips;
    }

    public Long getCompletedTripsCount() {
        return completedTripsCount;
    }

    public void setCompletedTripsCount(Long completedTripsCount) {
        this.completedTripsCount = completedTripsCount;
    }

    public Double getTotalObservedDistanceKm() {
        return totalObservedDistanceKm;
    }

    public void setTotalObservedDistanceKm(Double totalObservedDistanceKm) {
        this.totalObservedDistanceKm = totalObservedDistanceKm;
    }

    public Long getScorableTripsCount() {
        return scorableTripsCount;
    }

    public void setScorableTripsCount(Long scorableTripsCount) {
        this.scorableTripsCount = scorableTripsCount;
    }

    public Long getUnratedTripsCount() {
        return unratedTripsCount;
    }

    public void setUnratedTripsCount(Long unratedTripsCount) {
        this.unratedTripsCount = unratedTripsCount;
    }

    public Long getVehiclesWithEligibleQuotes() {
        return vehiclesWithEligibleQuotes;
    }

    public void setVehiclesWithEligibleQuotes(Long vehiclesWithEligibleQuotes) {
        this.vehiclesWithEligibleQuotes = vehiclesWithEligibleQuotes;
    }

    public Double getAverageSafetyScore() {
        return averageSafetyScore;
    }

    public void setAverageSafetyScore(Double averageSafetyScore) {
        this.averageSafetyScore = averageSafetyScore;
    }

    public Instant getAuditTimestamp() {
        return auditTimestamp;
    }

    public void setAuditTimestamp(Instant auditTimestamp) {
        this.auditTimestamp = auditTimestamp;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
