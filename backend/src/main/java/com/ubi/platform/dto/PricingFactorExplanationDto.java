package com.ubi.platform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO representing an explainable actuarial factor contribution to the premium quote (Step 6).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PricingFactorExplanationDto {

    private String factorName;
    private String description;
    private String appliedValue;
    private String status;
    private String explanation;

    public PricingFactorExplanationDto() {
    }

    public PricingFactorExplanationDto(String factorName, String description, String appliedValue,
                                       String status, String explanation) {
        this.factorName = factorName;
        this.description = description;
        this.appliedValue = appliedValue;
        this.status = status;
        this.explanation = explanation;
    }

    // Getters and Setters

    public String getFactorName() {
        return factorName;
    }

    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppliedValue() {
        return appliedValue;
    }

    public void setAppliedValue(String appliedValue) {
        this.appliedValue = appliedValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
