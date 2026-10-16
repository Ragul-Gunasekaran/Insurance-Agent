package com.ubi.platform.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated summary for Customer Driver Scorecard (Step 7: Phase 2).
 */
public class CustomerScorecardSummaryDto {

    // Vehicle information
    private Long vehicleId;
    private String vehicleNumber;
    private String make;
    private String model;
    private Integer manufacturingYear;
    private String fuelType;
    private String verificationStatus; // e.g., "VERIFIED (Mock Verification)"
    private Boolean isMockVerification = true;

    // Usage & trip counts
    private Long completedTripsCount = 0L;
    private Double totalObservedDistanceKm = 0.0;

    // Latest risk score details
    private Double latestSafetyScore;
    private String latestRiskCategory; // EXCELLENT, GOOD, MODERATE, HIGH_RISK, UNRATED
    private String scoringVersion;
    private Instant scoreCalculatedAt;
    private String dataQualityStatus;
    private String unratedReason;
    private List<RiskFactorExplanationDto> scoreExplanations = new ArrayList<>();

    // Aggregated driving performance rates (distance-weighted)
    private Double harshBrakeRatePer100Km = 0.0;
    private Double rapidAccelRatePer100Km = 0.0;
    private Double speedingObsPer100Km = 0.0;
    private Double nightDrivingRatio = 0.0;
    private Long nightDrivingMinutes = 0L;

    // Latest illustrative quote details
    private String latestQuoteReference;
    private String latestQuoteStatus; // READY, INSUFFICIENT_DATA, etc.
    private Boolean isQuoteEligible;
    private BigDecimal latestEstimatedAnnualPremiumInr;
    private BigDecimal latestBasePremiumInr;
    private BigDecimal latestRiskMultiplier;
    private BigDecimal latestUsageMultiplier;
    private String latestAnnualDistanceSource;
    private String latestPricingVersion;
    private Instant latestQuoteCalculatedAt;
    private String latestQuoteSummary;

    // Score progression history (chronological)
    private List<ScoreHistoryPointDto> scoreHistory = new ArrayList<>();

    // Regulatory / Hackathon prototype disclaimer
    private String disclaimer = "Prototype score and illustrative quote. Not a binding insurance policy, regulatory filing, or guaranteed underwriting offer.";

    public CustomerScorecardSummaryDto() {
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getManufacturingYear() {
        return manufacturingYear;
    }

    public void setManufacturingYear(Integer manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Boolean getIsMockVerification() {
        return isMockVerification;
    }

    public void setIsMockVerification(Boolean isMockVerification) {
        this.isMockVerification = isMockVerification;
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

    public Double getLatestSafetyScore() {
        return latestSafetyScore;
    }

    public void setLatestSafetyScore(Double latestSafetyScore) {
        this.latestSafetyScore = latestSafetyScore;
    }

    public String getLatestRiskCategory() {
        return latestRiskCategory;
    }

    public void setLatestRiskCategory(String latestRiskCategory) {
        this.latestRiskCategory = latestRiskCategory;
    }

    public String getScoringVersion() {
        return scoringVersion;
    }

    public void setScoringVersion(String scoringVersion) {
        this.scoringVersion = scoringVersion;
    }

    public Instant getScoreCalculatedAt() {
        return scoreCalculatedAt;
    }

    public void setScoreCalculatedAt(Instant scoreCalculatedAt) {
        this.scoreCalculatedAt = scoreCalculatedAt;
    }

    public String getDataQualityStatus() {
        return dataQualityStatus;
    }

    public void setDataQualityStatus(String dataQualityStatus) {
        this.dataQualityStatus = dataQualityStatus;
    }

    public String getUnratedReason() {
        return unratedReason;
    }

    public void setUnratedReason(String unratedReason) {
        this.unratedReason = unratedReason;
    }

    public List<RiskFactorExplanationDto> getScoreExplanations() {
        return scoreExplanations;
    }

    public void setScoreExplanations(List<RiskFactorExplanationDto> scoreExplanations) {
        this.scoreExplanations = scoreExplanations;
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

    public Double getSpeedingObsPer100Km() {
        return speedingObsPer100Km;
    }

    public void setSpeedingObsPer100Km(Double speedingObsPer100Km) {
        this.speedingObsPer100Km = speedingObsPer100Km;
    }

    public Double getNightDrivingRatio() {
        return nightDrivingRatio;
    }

    public void setNightDrivingRatio(Double nightDrivingRatio) {
        this.nightDrivingRatio = nightDrivingRatio;
    }

    public Long getNightDrivingMinutes() {
        return nightDrivingMinutes;
    }

    public void setNightDrivingMinutes(Long nightDrivingMinutes) {
        this.nightDrivingMinutes = nightDrivingMinutes;
    }

    public String getLatestQuoteReference() {
        return latestQuoteReference;
    }

    public void setLatestQuoteReference(String latestQuoteReference) {
        this.latestQuoteReference = latestQuoteReference;
    }

    public String getLatestQuoteStatus() {
        return latestQuoteStatus;
    }

    public void setLatestQuoteStatus(String latestQuoteStatus) {
        this.latestQuoteStatus = latestQuoteStatus;
    }

    public Boolean getIsQuoteEligible() {
        return isQuoteEligible;
    }

    public void setIsQuoteEligible(Boolean isQuoteEligible) {
        this.isQuoteEligible = isQuoteEligible;
    }

    public BigDecimal getLatestEstimatedAnnualPremiumInr() {
        return latestEstimatedAnnualPremiumInr;
    }

    public void setLatestEstimatedAnnualPremiumInr(BigDecimal latestEstimatedAnnualPremiumInr) {
        this.latestEstimatedAnnualPremiumInr = latestEstimatedAnnualPremiumInr;
    }

    public BigDecimal getLatestBasePremiumInr() {
        return latestBasePremiumInr;
    }

    public void setLatestBasePremiumInr(BigDecimal latestBasePremiumInr) {
        this.latestBasePremiumInr = latestBasePremiumInr;
    }

    public BigDecimal getLatestRiskMultiplier() {
        return latestRiskMultiplier;
    }

    public void setLatestRiskMultiplier(BigDecimal latestRiskMultiplier) {
        this.latestRiskMultiplier = latestRiskMultiplier;
    }

    public BigDecimal getLatestUsageMultiplier() {
        return latestUsageMultiplier;
    }

    public void setLatestUsageMultiplier(BigDecimal latestUsageMultiplier) {
        this.latestUsageMultiplier = latestUsageMultiplier;
    }

    public String getLatestAnnualDistanceSource() {
        return latestAnnualDistanceSource;
    }

    public void setLatestAnnualDistanceSource(String latestAnnualDistanceSource) {
        this.latestAnnualDistanceSource = latestAnnualDistanceSource;
    }

    public String getLatestPricingVersion() {
        return latestPricingVersion;
    }

    public void setLatestPricingVersion(String latestPricingVersion) {
        this.latestPricingVersion = latestPricingVersion;
    }

    public Instant getLatestQuoteCalculatedAt() {
        return latestQuoteCalculatedAt;
    }

    public void setLatestQuoteCalculatedAt(Instant latestQuoteCalculatedAt) {
        this.latestQuoteCalculatedAt = latestQuoteCalculatedAt;
    }

    public String getLatestQuoteSummary() {
        return latestQuoteSummary;
    }

    public void setLatestQuoteSummary(String latestQuoteSummary) {
        this.latestQuoteSummary = latestQuoteSummary;
    }

    public List<ScoreHistoryPointDto> getScoreHistory() {
        return scoreHistory;
    }

    public void setScoreHistory(List<ScoreHistoryPointDto> scoreHistory) {
        this.scoreHistory = scoreHistory;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
