package com.ubi.platform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request payload for calculating an actuarial premium quote (Step 6).
 * Supports an optional user-supplied annual distance assumption.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PricingCalculationRequestDto {

    private Double annualDistanceKmAssumption;

    public PricingCalculationRequestDto() {
    }

    public PricingCalculationRequestDto(Double annualDistanceKmAssumption) {
        this.annualDistanceKmAssumption = annualDistanceKmAssumption;
    }

    public Double getAnnualDistanceKmAssumption() {
        return annualDistanceKmAssumption;
    }

    public void setAnnualDistanceKmAssumption(Double annualDistanceKmAssumption) {
        this.annualDistanceKmAssumption = annualDistanceKmAssumption;
    }
}
