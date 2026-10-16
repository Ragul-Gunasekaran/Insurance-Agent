package com.ubi.platform.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Summary DTO for customer trip table entries with associated driving features and risk scores (Step 7: Phase 2.D).
 */
public class CustomerTripSummaryDto {

    private Long tripId;
    private Instant startTime;
    private Instant endTime;
    private Double distanceKm;
    private Long durationSeconds;
    private String status; // COMPLETED, etc.

    // Data quality
    private String dataQualityStatus;
    private Double dataQualityScore;

    // Safety score
    private Double safetyScore;
    private String riskCategory; // EXCELLENT, GOOD, MODERATE, HIGH_RISK, UNRATED
    private Boolean isScorable;
    private String unscorableReason;

    // Behavioral metrics
    private Integer harshBrakingCount;
    private Integer rapidAccelerationCount;
    private Integer speedingObservationsCount;
    private Long nightDrivingMinutes;
    private Double harshBrakeRatePer100Km;
    private Double rapidAccelRatePer100Km;
    private Double speedingPct;

    // Factor explanations
    private List<RiskFactorExplanationDto> factorExplanations = new ArrayList<>();

    public CustomerTripSummaryDto() {
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDataQualityStatus() {
        return dataQualityStatus;
    }

    public void setDataQualityStatus(String dataQualityStatus) {
        this.dataQualityStatus = dataQualityStatus;
    }

    public Double getDataQualityScore() {
        return dataQualityScore;
    }

    public void setDataQualityScore(Double dataQualityScore) {
        this.dataQualityScore = dataQualityScore;
    }

    public Double getSafetyScore() {
        return safetyScore;
    }

    public void setSafetyScore(Double safetyScore) {
        this.safetyScore = safetyScore;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public Boolean getIsScorable() {
        return isScorable;
    }

    public void setIsScorable(Boolean isScorable) {
        this.isScorable = isScorable;
    }

    public String getUnscorableReason() {
        return unscorableReason;
    }

    public void setUnscorableReason(String unscorableReason) {
        this.unscorableReason = unscorableReason;
    }

    public Integer getHarshBrakingCount() {
        return harshBrakingCount;
    }

    public void setHarshBrakingCount(Integer harshBrakingCount) {
        this.harshBrakingCount = harshBrakingCount;
    }

    public Integer getRapidAccelerationCount() {
        return rapidAccelerationCount;
    }

    public void setRapidAccelerationCount(Integer rapidAccelerationCount) {
        this.rapidAccelerationCount = rapidAccelerationCount;
    }

    public Integer getSpeedingObservationsCount() {
        return speedingObservationsCount;
    }

    public void setSpeedingObservationsCount(Integer speedingObservationsCount) {
        this.speedingObservationsCount = speedingObservationsCount;
    }

    public Long getNightDrivingMinutes() {
        return nightDrivingMinutes;
    }

    public void setNightDrivingMinutes(Long nightDrivingMinutes) {
        this.nightDrivingMinutes = nightDrivingMinutes;
    }

    public Double getHarshBrakeRatePer100Km() {
        return harshBrakeRatePer100Km;
    }

    public void setHarshBrakeRatePer100Km(Double harshBrakeRatePer100Km) {
        this.harshBrakeRatePer100Km = harshBrakeRatePer100Km;
    }

    public Double getRapidAccelRatePer100Km() {
        return rapidAccelRatePer100Km;
    }

    public void setRapidAccelRatePer100Km(Double rapidAccelRatePer100Km) {
        this.rapidAccelRatePer100Km = rapidAccelRatePer100Km;
    }

    public Double getSpeedingPct() {
        return speedingPct;
    }

    public void setSpeedingPct(Double speedingPct) {
        this.speedingPct = speedingPct;
    }

    public List<RiskFactorExplanationDto> getFactorExplanations() {
        return factorExplanations;
    }

    public void setFactorExplanations(List<RiskFactorExplanationDto> factorExplanations) {
        this.factorExplanations = factorExplanations;
    }
}
