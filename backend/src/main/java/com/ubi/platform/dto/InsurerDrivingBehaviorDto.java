package com.ubi.platform.dto;

/**
 * Fleet-level aggregated driving behavior metrics with weighted rates (Step 7: Phase 3.C).
 */
public class InsurerDrivingBehaviorDto {

    private Long totalHarshBrakingEvents = 0L;
    private Long totalRapidAccelerationEvents = 0L;
    private Long totalSpeedingObservations = 0L;

    // Rates per 100 km (weighted by fleet distance)
    private Double fleetHarshBrakeRatePer100Km = 0.0;
    private Double fleetRapidAccelRatePer100Km = 0.0;
    private Double fleetSpeedingObsPer100Km = 0.0;

    // Night driving
    private Long totalNightDrivingMinutes = 0L;
    private Double fleetNightDrivingRatio = 0.0;

    // Telematics data quality
    private Long totalValidRecords = 0L;
    private Long totalAnomalousRecords = 0L;
    private Double fleetAnomalyRatePct = 0.0;

    // Evaluated bases
    private Double totalDistanceEvaluatedKm = 0.0;
    private Long totalTripsEvaluated = 0L;

    private String disclaimer = "Rates calculated across all validated trips using distance-weighted aggregation. Individual driving patterns vary.";

    public InsurerDrivingBehaviorDto() {
    }

    public Long getTotalHarshBrakingEvents() {
        return totalHarshBrakingEvents;
    }

    public void setTotalHarshBrakingEvents(Long totalHarshBrakingEvents) {
        this.totalHarshBrakingEvents = totalHarshBrakingEvents;
    }

    public Long getTotalRapidAccelerationEvents() {
        return totalRapidAccelerationEvents;
    }

    public void setTotalRapidAccelerationEvents(Long totalRapidAccelerationEvents) {
        this.totalRapidAccelerationEvents = totalRapidAccelerationEvents;
    }

    public Long getTotalSpeedingObservations() {
        return totalSpeedingObservations;
    }

    public void setTotalSpeedingObservations(Long totalSpeedingObservations) {
        this.totalSpeedingObservations = totalSpeedingObservations;
    }

    public Double getFleetHarshBrakeRatePer100Km() {
        return fleetHarshBrakeRatePer100Km;
    }

    public void setFleetHarshBrakeRatePer100Km(Double fleetHarshBrakeRatePer100Km) {
        this.fleetHarshBrakeRatePer100Km = fleetHarshBrakeRatePer100Km;
    }

    public Double getFleetRapidAccelRatePer100Km() {
        return fleetRapidAccelRatePer100Km;
    }

    public void setFleetRapidAccelRatePer100Km(Double fleetRapidAccelRatePer100Km) {
        this.fleetRapidAccelRatePer100Km = fleetRapidAccelRatePer100Km;
    }

    public Double getFleetSpeedingObsPer100Km() {
        return fleetSpeedingObsPer100Km;
    }

    public void setFleetSpeedingObsPer100Km(Double fleetSpeedingObsPer100Km) {
        this.fleetSpeedingObsPer100Km = fleetSpeedingObsPer100Km;
    }

    public Long getTotalNightDrivingMinutes() {
        return totalNightDrivingMinutes;
    }

    public void setTotalNightDrivingMinutes(Long totalNightDrivingMinutes) {
        this.totalNightDrivingMinutes = totalNightDrivingMinutes;
    }

    public Double getFleetNightDrivingRatio() {
        return fleetNightDrivingRatio;
    }

    public void setFleetNightDrivingRatio(Double fleetNightDrivingRatio) {
        this.fleetNightDrivingRatio = fleetNightDrivingRatio;
    }

    public Long getTotalValidRecords() {
        return totalValidRecords;
    }

    public void setTotalValidRecords(Long totalValidRecords) {
        this.totalValidRecords = totalValidRecords;
    }

    public Long getTotalAnomalousRecords() {
        return totalAnomalousRecords;
    }

    public void setTotalAnomalousRecords(Long totalAnomalousRecords) {
        this.totalAnomalousRecords = totalAnomalousRecords;
    }

    public Double getFleetAnomalyRatePct() {
        return fleetAnomalyRatePct;
    }

    public void setFleetAnomalyRatePct(Double fleetAnomalyRatePct) {
        this.fleetAnomalyRatePct = fleetAnomalyRatePct;
    }

    public Double getTotalDistanceEvaluatedKm() {
        return totalDistanceEvaluatedKm;
    }

    public void setTotalDistanceEvaluatedKm(Double totalDistanceEvaluatedKm) {
        this.totalDistanceEvaluatedKm = totalDistanceEvaluatedKm;
    }

    public Long getTotalTripsEvaluated() {
        return totalTripsEvaluated;
    }

    public void setTotalTripsEvaluated(Long totalTripsEvaluated) {
        this.totalTripsEvaluated = totalTripsEvaluated;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
