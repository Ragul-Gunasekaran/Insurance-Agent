package com.ubi.platform.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Safe, non-sensitive configuration response DTO exposing prototype actuarial pricing parameters (Step 6).
 */
public class PricingConfigurationResponseDto {

    private BigDecimal baseAnnualPremiumInr;
    private BigDecimal minPremiumInr;
    private BigDecimal maxPremiumInr;
    private Map<String, BigDecimal> riskMultipliers;
    private Double baselineAnnualDistanceKm;
    private Integer minObservationDaysForProjection;
    private Double minObservedDistanceKmForProjection;
    private String pricingVersion;
    private String currency;
    private String prototypeDisclaimer;

    public PricingConfigurationResponseDto() {
    }

    public PricingConfigurationResponseDto(BigDecimal baseAnnualPremiumInr, BigDecimal minPremiumInr,
                                           BigDecimal maxPremiumInr, Map<String, BigDecimal> riskMultipliers,
                                           Double baselineAnnualDistanceKm, Integer minObservationDaysForProjection,
                                           Double minObservedDistanceKmForProjection, String pricingVersion,
                                           String currency, String prototypeDisclaimer) {
        this.baseAnnualPremiumInr = baseAnnualPremiumInr;
        this.minPremiumInr = minPremiumInr;
        this.maxPremiumInr = maxPremiumInr;
        this.riskMultipliers = riskMultipliers;
        this.baselineAnnualDistanceKm = baselineAnnualDistanceKm;
        this.minObservationDaysForProjection = minObservationDaysForProjection;
        this.minObservedDistanceKmForProjection = minObservedDistanceKmForProjection;
        this.pricingVersion = pricingVersion;
        this.currency = currency;
        this.prototypeDisclaimer = prototypeDisclaimer;
    }

    // Getters and Setters

    public BigDecimal getBaseAnnualPremiumInr() {
        return baseAnnualPremiumInr;
    }

    public void setBaseAnnualPremiumInr(BigDecimal baseAnnualPremiumInr) {
        this.baseAnnualPremiumInr = baseAnnualPremiumInr;
    }

    public BigDecimal getMinPremiumInr() {
        return minPremiumInr;
    }

    public void setMinPremiumInr(BigDecimal minPremiumInr) {
        this.minPremiumInr = minPremiumInr;
    }

    public BigDecimal getMaxPremiumInr() {
        return maxPremiumInr;
    }

    public void setMaxPremiumInr(BigDecimal maxPremiumInr) {
        this.maxPremiumInr = maxPremiumInr;
    }

    public Map<String, BigDecimal> getRiskMultipliers() {
        return riskMultipliers;
    }

    public void setRiskMultipliers(Map<String, BigDecimal> riskMultipliers) {
        this.riskMultipliers = riskMultipliers;
    }

    public Double getBaselineAnnualDistanceKm() {
        return baselineAnnualDistanceKm;
    }

    public void setBaselineAnnualDistanceKm(Double baselineAnnualDistanceKm) {
        this.baselineAnnualDistanceKm = baselineAnnualDistanceKm;
    }

    public Integer getMinObservationDaysForProjection() {
        return minObservationDaysForProjection;
    }

    public void setMinObservationDaysForProjection(Integer minObservationDaysForProjection) {
        this.minObservationDaysForProjection = minObservationDaysForProjection;
    }

    public Double getMinObservedDistanceKmForProjection() {
        return minObservedDistanceKmForProjection;
    }

    public void setMinObservedDistanceKmForProjection(Double minObservedDistanceKmForProjection) {
        this.minObservedDistanceKmForProjection = minObservedDistanceKmForProjection;
    }

    public String getPricingVersion() {
        return pricingVersion;
    }

    public void setPricingVersion(String pricingVersion) {
        this.pricingVersion = pricingVersion;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPrototypeDisclaimer() {
        return prototypeDisclaimer;
    }

    public void setPrototypeDisclaimer(String prototypeDisclaimer) {
        this.prototypeDisclaimer = prototypeDisclaimer;
    }
}
