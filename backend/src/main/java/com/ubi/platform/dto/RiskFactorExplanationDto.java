package com.ubi.platform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing an explainable driving behavior factor contribution to the risk safety score (Step 5).
 * Transparently conveys observed metrics, applied deductions, caps, and data-quality exclusions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskFactorExplanationDto {

    private String factorName;
    private Double observedValue;
    private String unit;
    private Double penaltyApplied;
    private Double maxPenaltyCap;
    private Boolean included;
    private String explanation;

    public RiskFactorExplanationDto() {
    }

    public RiskFactorExplanationDto(String factorName, Double observedValue, String unit,
                                   Double penaltyApplied, Double maxPenaltyCap,
                                   Boolean included, String explanation) {
        this.factorName = factorName;
        this.observedValue = observedValue;
        this.unit = unit;
        this.penaltyApplied = penaltyApplied;
        this.maxPenaltyCap = maxPenaltyCap;
        this.included = included;
        this.explanation = explanation;
    }

    // Static factory for excluded factor due to data quality / insufficient data
    public static RiskFactorExplanationDto excluded(String factorName, String reason) {
        return new RiskFactorExplanationDto(
                factorName,
                null,
                null,
                0.0,
                null,
                false,
                reason
        );
    }

    // Getters and Setters

    public String getFactorName() {
        return factorName;
    }

    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }

    public Double getObservedValue() {
        return observedValue;
    }

    public void setObservedValue(Double observedValue) {
        this.observedValue = observedValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getPenaltyApplied() {
        return penaltyApplied;
    }

    public void setPenaltyApplied(Double penaltyApplied) {
        this.penaltyApplied = penaltyApplied;
    }

    public Double getMaxPenaltyCap() {
        return maxPenaltyCap;
    }

    public void setMaxPenaltyCap(Double maxPenaltyCap) {
        this.maxPenaltyCap = maxPenaltyCap;
    }

    public Boolean getIncluded() {
        return included;
    }

    public void setIncluded(Boolean included) {
        this.included = included;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
