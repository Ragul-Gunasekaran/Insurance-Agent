package com.ubi.platform.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated analytics for illustrative premium quotes (Step 7: Phase 3.E).
 */
public class InsurerPricingAnalyticsDto {

    private Long totalQuotes = 0L;
    private Long eligibleQuotesCount = 0L;
    private Long ineligibleQuotesCount = 0L;

    // Monetary figures in INR for eligible quotes
    private BigDecimal averageEstimatedPremiumInr;
    private BigDecimal medianEstimatedPremiumInr;
    private BigDecimal minEstimatedPremiumInr;
    private BigDecimal maxEstimatedPremiumInr;

    // Distributions
    private List<CategoryCountDto> quotedRiskDistribution = new ArrayList<>();
    private List<PricingVersionCountDto> pricingVersionDistribution = new ArrayList<>();

    private String currency = "INR";
    private String disclaimer = "Prototype illustrative quotes only. Excludes ineligible quotes from premium averages. Not real underwriting.";

    public InsurerPricingAnalyticsDto() {
    }

    public Long getTotalQuotes() {
        return totalQuotes;
    }

    public void setTotalQuotes(Long totalQuotes) {
        this.totalQuotes = totalQuotes;
    }

    public Long getEligibleQuotesCount() {
        return eligibleQuotesCount;
    }

    public void setEligibleQuotesCount(Long eligibleQuotesCount) {
        this.eligibleQuotesCount = eligibleQuotesCount;
    }

    public Long getIneligibleQuotesCount() {
        return ineligibleQuotesCount;
    }

    public void setIneligibleQuotesCount(Long ineligibleQuotesCount) {
        this.ineligibleQuotesCount = ineligibleQuotesCount;
    }

    public BigDecimal getAverageEstimatedPremiumInr() {
        return averageEstimatedPremiumInr;
    }

    public void setAverageEstimatedPremiumInr(BigDecimal averageEstimatedPremiumInr) {
        this.averageEstimatedPremiumInr = averageEstimatedPremiumInr;
    }

    public BigDecimal getMedianEstimatedPremiumInr() {
        return medianEstimatedPremiumInr;
    }

    public void setMedianEstimatedPremiumInr(BigDecimal medianEstimatedPremiumInr) {
        this.medianEstimatedPremiumInr = medianEstimatedPremiumInr;
    }

    public BigDecimal getMinEstimatedPremiumInr() {
        return minEstimatedPremiumInr;
    }

    public void setMinEstimatedPremiumInr(BigDecimal minEstimatedPremiumInr) {
        this.minEstimatedPremiumInr = minEstimatedPremiumInr;
    }

    public BigDecimal getMaxEstimatedPremiumInr() {
        return maxEstimatedPremiumInr;
    }

    public void setMaxEstimatedPremiumInr(BigDecimal maxEstimatedPremiumInr) {
        this.maxEstimatedPremiumInr = maxEstimatedPremiumInr;
    }

    public List<CategoryCountDto> getQuotedRiskDistribution() {
        return quotedRiskDistribution;
    }

    public void setQuotedRiskDistribution(List<CategoryCountDto> quotedRiskDistribution) {
        this.quotedRiskDistribution = quotedRiskDistribution;
    }

    public List<PricingVersionCountDto> getPricingVersionDistribution() {
        return pricingVersionDistribution;
    }

    public void setPricingVersionDistribution(List<PricingVersionCountDto> pricingVersionDistribution) {
        this.pricingVersionDistribution = pricingVersionDistribution;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
