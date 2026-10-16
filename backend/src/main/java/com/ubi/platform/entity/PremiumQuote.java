package com.ubi.platform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA Entity representing a persisted actuarial premium quote snapshot (Step 6).
 * Preserves the exact formula version, input metrics, multipliers, and factor explanations at calculation time.
 * Demonstrates illustrative UBI pricing in INR (₹) without real policy purchase or payment processing.
 */
@Entity
@Table(
    name = "premium_quotes",
    indexes = {
        @Index(name = "idx_quotes_vehicle_date", columnList = "vehicle_id, calculated_at"),
        @Index(name = "idx_quotes_reference", columnList = "quote_reference", unique = true)
    }
)
public class PremiumQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quote_reference", nullable = false, unique = true, length = 64)
    private String quoteReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "status", nullable = false, length = 32)
    private String status; // READY, INSUFFICIENT_RISK_DATA, INSUFFICIENT_USAGE_DATA, INSUFFICIENT_DATA

    @Column(name = "is_eligible", nullable = false)
    private Boolean isEligible = true;

    @Column(name = "base_annual_premium_inr", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAnnualPremiumInr;

    @Column(name = "safety_score")
    private Double safetyScore;

    @Column(name = "risk_category", length = 32)
    private String riskCategory;

    @Column(name = "risk_multiplier", precision = 6, scale = 4)
    private BigDecimal riskMultiplier;

    @Column(name = "observed_distance_km")
    private Double observedDistanceKm;

    @Column(name = "observation_days")
    private Integer observationDays;

    @Column(name = "projected_annual_distance_km")
    private Double projectedAnnualDistanceKm;

    @Column(name = "annual_distance_source", length = 32)
    private String annualDistanceSource; // TELEMETRY_PROJECTED, USER_SPECIFIED, DEFAULT_ASSUMPTION, NONE

    @Column(name = "usage_multiplier", precision = 6, scale = 4)
    private BigDecimal usageMultiplier;

    @Column(name = "raw_annual_premium_inr", precision = 12, scale = 2)
    private BigDecimal rawAnnualPremiumInr;

    @Column(name = "final_annual_premium_inr", precision = 12, scale = 2)
    private BigDecimal finalAnnualPremiumInr;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency = "INR";

    @Column(name = "pricing_version", nullable = false, length = 32)
    private String pricingVersion = "pricing-v1";

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "excluded_trips_count")
    private Integer excludedTripsCount = 0;

    @Column(name = "summary_explanation", length = 1000)
    private String summaryExplanation;

    @Column(name = "factor_explanations_json", columnDefinition = "TEXT")
    private String factorExplanationsJson;

    public PremiumQuote() {
    }

    public PremiumQuote(String quoteReference, Vehicle vehicle, String status, Boolean isEligible,
                        BigDecimal baseAnnualPremiumInr, Double safetyScore, String riskCategory,
                        BigDecimal riskMultiplier, Double observedDistanceKm, Integer observationDays,
                        Double projectedAnnualDistanceKm, String annualDistanceSource,
                        BigDecimal usageMultiplier, BigDecimal rawAnnualPremiumInr,
                        BigDecimal finalAnnualPremiumInr, String currency, String pricingVersion,
                        Integer excludedTripsCount, String summaryExplanation,
                        String factorExplanationsJson) {
        this.quoteReference = quoteReference;
        this.vehicle = vehicle;
        this.status = status;
        this.isEligible = isEligible != null ? isEligible : true;
        this.baseAnnualPremiumInr = baseAnnualPremiumInr;
        this.safetyScore = safetyScore;
        this.riskCategory = riskCategory;
        this.riskMultiplier = riskMultiplier;
        this.observedDistanceKm = observedDistanceKm;
        this.observationDays = observationDays;
        this.projectedAnnualDistanceKm = projectedAnnualDistanceKm;
        this.annualDistanceSource = annualDistanceSource;
        this.usageMultiplier = usageMultiplier;
        this.rawAnnualPremiumInr = rawAnnualPremiumInr;
        this.finalAnnualPremiumInr = finalAnnualPremiumInr;
        this.currency = currency != null ? currency : "INR";
        this.pricingVersion = pricingVersion != null ? pricingVersion : "pricing-v1";
        this.calculatedAt = Instant.now();
        this.excludedTripsCount = excludedTripsCount != null ? excludedTripsCount : 0;
        this.summaryExplanation = summaryExplanation;
        this.factorExplanationsJson = factorExplanationsJson;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuoteReference() {
        return quoteReference;
    }

    public void setQuoteReference(String quoteReference) {
        this.quoteReference = quoteReference;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsEligible() {
        return isEligible;
    }

    public void setIsEligible(Boolean isEligible) {
        this.isEligible = isEligible;
    }

    public BigDecimal getBaseAnnualPremiumInr() {
        return baseAnnualPremiumInr;
    }

    public void setBaseAnnualPremiumInr(BigDecimal baseAnnualPremiumInr) {
        this.baseAnnualPremiumInr = baseAnnualPremiumInr;
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

    public BigDecimal getRiskMultiplier() {
        return riskMultiplier;
    }

    public void setRiskMultiplier(BigDecimal riskMultiplier) {
        this.riskMultiplier = riskMultiplier;
    }

    public Double getObservedDistanceKm() {
        return observedDistanceKm;
    }

    public void setObservedDistanceKm(Double observedDistanceKm) {
        this.observedDistanceKm = observedDistanceKm;
    }

    public Integer getObservationDays() {
        return observationDays;
    }

    public void setObservationDays(Integer observationDays) {
        this.observationDays = observationDays;
    }

    public Double getProjectedAnnualDistanceKm() {
        return projectedAnnualDistanceKm;
    }

    public void setProjectedAnnualDistanceKm(Double projectedAnnualDistanceKm) {
        this.projectedAnnualDistanceKm = projectedAnnualDistanceKm;
    }

    public String getAnnualDistanceSource() {
        return annualDistanceSource;
    }

    public void setAnnualDistanceSource(String annualDistanceSource) {
        this.annualDistanceSource = annualDistanceSource;
    }

    public BigDecimal getUsageMultiplier() {
        return usageMultiplier;
    }

    public void setUsageMultiplier(BigDecimal usageMultiplier) {
        this.usageMultiplier = usageMultiplier;
    }

    public BigDecimal getRawAnnualPremiumInr() {
        return rawAnnualPremiumInr;
    }

    public void setRawAnnualPremiumInr(BigDecimal rawAnnualPremiumInr) {
        this.rawAnnualPremiumInr = rawAnnualPremiumInr;
    }

    public BigDecimal getFinalAnnualPremiumInr() {
        return finalAnnualPremiumInr;
    }

    public void setFinalAnnualPremiumInr(BigDecimal finalAnnualPremiumInr) {
        this.finalAnnualPremiumInr = finalAnnualPremiumInr;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPricingVersion() {
        return pricingVersion;
    }

    public void setPricingVersion(String pricingVersion) {
        this.pricingVersion = pricingVersion;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Integer getExcludedTripsCount() {
        return excludedTripsCount;
    }

    public void setExcludedTripsCount(Integer excludedTripsCount) {
        this.excludedTripsCount = excludedTripsCount;
    }

    public String getSummaryExplanation() {
        return summaryExplanation;
    }

    public void setSummaryExplanation(String summaryExplanation) {
        this.summaryExplanation = summaryExplanation;
    }

    public String getFactorExplanationsJson() {
        return factorExplanationsJson;
    }

    public void setFactorExplanationsJson(String factorExplanationsJson) {
        this.factorExplanationsJson = factorExplanationsJson;
    }
}
