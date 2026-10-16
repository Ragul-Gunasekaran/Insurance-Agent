package com.ubi.platform.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.entity.PremiumQuote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * REST response DTO for actuarial premium quotes and explainability breakdowns (Step 6).
 */
public class PremiumQuoteResponseDto {

    private Long id;
    private String quoteReference;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private String vehicleMake;
    private String vehicleModel;
    private String status;
    private Boolean isEligible;
    private BigDecimal baseAnnualPremiumInr;
    private Double safetyScore;
    private String riskCategory;
    private BigDecimal riskMultiplier;
    private Double observedDistanceKm;
    private Integer observationDays;
    private Double projectedAnnualDistanceKm;
    private String annualDistanceSource;
    private BigDecimal usageMultiplier;
    private BigDecimal rawAnnualPremiumInr;
    private BigDecimal finalAnnualPremiumInr;
    private String currency;
    private String pricingVersion;
    private Instant calculatedAt;
    private Integer excludedTripsCount;
    private String summaryExplanation;
    private List<PricingFactorExplanationDto> factorExplanations = new ArrayList<>();

    public PremiumQuoteResponseDto() {
    }

    public static PremiumQuoteResponseDto fromEntity(PremiumQuote entity, ObjectMapper objectMapper) {
        if (entity == null) {
            return null;
        }

        PremiumQuoteResponseDto dto = new PremiumQuoteResponseDto();
        dto.setId(entity.getId());
        dto.setQuoteReference(entity.getQuoteReference());
        if (entity.getVehicle() != null) {
            dto.setVehicleId(entity.getVehicle().getId());
            dto.setVehicleRegistrationNumber(entity.getVehicle().getVehicleNumber());
            dto.setVehicleMake(entity.getVehicle().getMake());
            dto.setVehicleModel(entity.getVehicle().getModel());
        }
        dto.setStatus(entity.getStatus());
        dto.setIsEligible(entity.getIsEligible());
        dto.setBaseAnnualPremiumInr(entity.getBaseAnnualPremiumInr());
        dto.setSafetyScore(entity.getSafetyScore());
        dto.setRiskCategory(entity.getRiskCategory());
        dto.setRiskMultiplier(entity.getRiskMultiplier());
        dto.setObservedDistanceKm(entity.getObservedDistanceKm());
        dto.setObservationDays(entity.getObservationDays());
        dto.setProjectedAnnualDistanceKm(entity.getProjectedAnnualDistanceKm());
        dto.setAnnualDistanceSource(entity.getAnnualDistanceSource());
        dto.setUsageMultiplier(entity.getUsageMultiplier());
        dto.setRawAnnualPremiumInr(entity.getRawAnnualPremiumInr());
        dto.setFinalAnnualPremiumInr(entity.getFinalAnnualPremiumInr());
        dto.setCurrency(entity.getCurrency());
        dto.setPricingVersion(entity.getPricingVersion());
        dto.setCalculatedAt(entity.getCalculatedAt());
        dto.setExcludedTripsCount(entity.getExcludedTripsCount());
        dto.setSummaryExplanation(entity.getSummaryExplanation());

        if (entity.getFactorExplanationsJson() != null && objectMapper != null) {
            try {
                List<PricingFactorExplanationDto> explanations = objectMapper.readValue(
                        entity.getFactorExplanationsJson(),
                        new TypeReference<List<PricingFactorExplanationDto>>() {}
                );
                dto.setFactorExplanations(explanations);
            } catch (Exception e) {
                dto.setFactorExplanations(new ArrayList<>());
            }
        }

        return dto;
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

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
    }

    public String getVehicleMake() {
        return vehicleMake;
    }

    public void setVehicleMake(String vehicleMake) {
        this.vehicleMake = vehicleMake;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
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

    public List<PricingFactorExplanationDto> getFactorExplanations() {
        return factorExplanations;
    }

    public void setFactorExplanations(List<PricingFactorExplanationDto> factorExplanations) {
        this.factorExplanations = factorExplanations;
    }
}
