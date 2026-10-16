package com.ubi.platform.service;

import com.ubi.platform.dto.PricingFactorExplanationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure, deterministic actuarial calculation component for UBI premium quoting (Step 6).
 * Computes: Estimated Annual Premium = Base Annual Premium × Risk Multiplier × Usage Multiplier
 * Applies configured policy floor/ceiling bounds and INR (₹) rounding (HALF_UP).
 * Completely independent of database persistence and Spring web context for isolated unit testing.
 */
@Component
public class PricingCalculator {

    @Value("${app.pricing.base-annual-premium-inr:12000.00}")
    private BigDecimal baseAnnualPremiumInr = new BigDecimal("12000.00");

    @Value("${app.pricing.min-premium-inr:5000.00}")
    private BigDecimal minPremiumInr = new BigDecimal("5000.00");

    @Value("${app.pricing.max-premium-inr:35000.00}")
    private BigDecimal maxPremiumInr = new BigDecimal("35000.00");

    @Value("${app.pricing.multiplier.excellent:0.9000}")
    private BigDecimal excellentMultiplier = new BigDecimal("0.9000");

    @Value("${app.pricing.multiplier.good:1.0000}")
    private BigDecimal goodMultiplier = new BigDecimal("1.0000");

    @Value("${app.pricing.multiplier.moderate:1.1500}")
    private BigDecimal moderateMultiplier = new BigDecimal("1.1500");

    @Value("${app.pricing.multiplier.high-risk:1.3000}")
    private BigDecimal highRiskMultiplier = new BigDecimal("1.3000");

    @Value("${app.pricing.baseline-annual-distance-km:10000.0}")
    private double baselineAnnualDistanceKm = 10000.0;

    @Value("${app.pricing.min-observation-days-for-projection:7}")
    private int minObservationDaysForProjection = 7;

    @Value("${app.pricing.min-observed-distance-km-for-projection:20.0}")
    private double minObservedDistanceKmForProjection = 20.0;

    @Value("${app.pricing.min-usage-multiplier:0.8000}")
    private BigDecimal minUsageMultiplier = new BigDecimal("0.8000");

    @Value("${app.pricing.max-usage-multiplier:1.3500}")
    private BigDecimal maxUsageMultiplier = new BigDecimal("1.3500");

    public PricingCalculator() {
    }

    /**
     * Executes deterministic premium quote calculation for the provided input metrics.
     */
    public PricingResult calculate(PricingInputs inputs) {
        validateConfiguration();

        List<PricingFactorExplanationDto> explanations = new ArrayList<>();
        boolean riskValid = true;
        boolean usageValid = true;

        // 1. Evaluate Risk Score Eligibility
        Double safetyScore = inputs.getSafetyScore();
        String riskCategory = inputs.getRiskCategory();
        BigDecimal riskMultiplier = null;

        if (!inputs.isScoreEligible() || safetyScore == null || "UNRATED".equalsIgnoreCase(riskCategory)) {
            riskValid = false;
            explanations.add(new PricingFactorExplanationDto(
                    "RISK_MULTIPLIER",
                    "Driving Safety Risk Factor",
                    "INELIGIBLE",
                    "INSUFFICIENT_DATA",
                    "No eligible driving safety score available. Ongoing, unrated, or insufficient-data trips cannot be scored."
            ));
        } else {
            // Determine risk multiplier based on category
            if (safetyScore >= 85.0) {
                riskCategory = "EXCELLENT";
                riskMultiplier = excellentMultiplier;
            } else if (safetyScore >= 70.0) {
                riskCategory = "GOOD";
                riskMultiplier = goodMultiplier;
            } else if (safetyScore >= 50.0) {
                riskCategory = "MODERATE";
                riskMultiplier = moderateMultiplier;
            } else {
                riskCategory = "HIGH_RISK";
                riskMultiplier = highRiskMultiplier;
            }

            double discountPct = (BigDecimal.ONE.subtract(riskMultiplier)).multiply(new BigDecimal("100")).doubleValue();
            String discountOrSurcharge = discountPct >= 0
                    ? String.format("%.1f%% safe driver discount", discountPct)
                    : String.format("%.1f%% risk surcharge", Math.abs(discountPct));

            explanations.add(new PricingFactorExplanationDto(
                    "RISK_MULTIPLIER",
                    "Driving Safety Risk Factor",
                    riskMultiplier.toString(),
                    "APPLIED",
                    String.format("Safety score of %.1f/100 (%s) yields risk multiplier %s (%s).",
                            safetyScore, riskCategory, riskMultiplier, discountOrSurcharge)
            ));
        }

        // 2. Evaluate Usage & Mileage Eligibility
        Double projectedAnnualKm = null;
        String annualDistanceSource = "NONE";
        BigDecimal usageMultiplier = null;

        if (inputs.getUserAnnualDistanceAssumption() != null && inputs.getUserAnnualDistanceAssumption() > 0) {
            projectedAnnualKm = inputs.getUserAnnualDistanceAssumption();
            annualDistanceSource = "USER_SPECIFIED";
        } else if (inputs.getObservedDistanceKm() != null && inputs.getObservedDistanceKm() >= minObservedDistanceKmForProjection
                && inputs.getObservationDays() != null && inputs.getObservationDays() >= minObservationDaysForProjection) {
            projectedAnnualKm = Math.round(((inputs.getObservedDistanceKm() / inputs.getObservationDays()) * 365.25) * 10.0) / 10.0;
            annualDistanceSource = "TELEMETRY_PROJECTED";
        } else {
            usageValid = false;
            String reason = String.format("Observed telemetry (%.1f km over %d days) does not meet minimum projection threshold (>= %.1f km and >= %d days). Supply an annual distance assumption.",
                    inputs.getObservedDistanceKm() != null ? inputs.getObservedDistanceKm() : 0.0,
                    inputs.getObservationDays() != null ? inputs.getObservationDays() : 0,
                    minObservedDistanceKmForProjection,
                    minObservationDaysForProjection);

            explanations.add(new PricingFactorExplanationDto(
                    "USAGE_MULTIPLIER",
                    "Annual Mileage Road Exposure",
                    "INSUFFICIENT_DATA",
                    "INSUFFICIENT_DATA",
                    reason
            ));
        }

        if (usageValid) {
            usageMultiplier = deriveUsageMultiplier(projectedAnnualKm);
            String sourceDesc = "USER_SPECIFIED".equals(annualDistanceSource)
                    ? "User-supplied assumption"
                    : String.format("Annualized from %.1f km observed over %d days", inputs.getObservedDistanceKm(), inputs.getObservationDays());

            explanations.add(new PricingFactorExplanationDto(
                    "USAGE_MULTIPLIER",
                    "Annual Mileage Road Exposure",
                    usageMultiplier.toString(),
                    "APPLIED",
                    String.format("Projected annual distance of %,.0f km (%s) results in usage multiplier %s.",
                            projectedAnnualKm, sourceDesc, usageMultiplier)
            ));
        }

        // 3. Base Premium Factor Explanation
        explanations.add(0, new PricingFactorExplanationDto(
                "BASE_PREMIUM",
                "Baseline Annual Premium Tariff",
                String.format("₹%,.2f", baseAnnualPremiumInr),
                "APPLIED",
                String.format("Standard baseline tariff of ₹%,.2f per year for this vehicle category.", baseAnnualPremiumInr)
        ));

        // 4. Data Exclusions
        if (inputs.getExcludedTripsCount() > 0) {
            explanations.add(new PricingFactorExplanationDto(
                    "DATA_EXCLUSIONS",
                    "Excluded Telemetry Sessions",
                    String.valueOf(inputs.getExcludedTripsCount()),
                    "EXCLUDED",
                    String.format("%d incomplete or unrated trips were excluded from rating calculations in compliance with actuarial safeguards.", inputs.getExcludedTripsCount())
            ));
        }

        // 5. Final Outcome Evaluation
        if (!riskValid && !usageValid) {
            return PricingResult.ineligible(
                    "INSUFFICIENT_DATA",
                    baseAnnualPremiumInr,
                    safetyScore,
                    riskCategory,
                    inputs.getObservedDistanceKm(),
                    inputs.getObservationDays(),
                    projectedAnnualKm,
                    annualDistanceSource,
                    inputs.getExcludedTripsCount(),
                    "Insufficient driving safety telemetry and insufficient mileage observation data. Cannot generate premium quote.",
                    explanations
            );
        }

        if (!riskValid) {
            return PricingResult.ineligible(
                    "INSUFFICIENT_RISK_DATA",
                    baseAnnualPremiumInr,
                    safetyScore,
                    riskCategory,
                    inputs.getObservedDistanceKm(),
                    inputs.getObservationDays(),
                    projectedAnnualKm,
                    annualDistanceSource,
                    inputs.getExcludedTripsCount(),
                    "Eligible behavioral risk score unavailable. Trip must be finalized and scored before quote generation.",
                    explanations
            );
        }

        if (!usageValid) {
            return PricingResult.ineligible(
                    "INSUFFICIENT_USAGE_DATA",
                    baseAnnualPremiumInr,
                    safetyScore,
                    riskCategory,
                    inputs.getObservedDistanceKm(),
                    inputs.getObservationDays(),
                    projectedAnnualKm,
                    annualDistanceSource,
                    inputs.getExcludedTripsCount(),
                    "Insufficient observation period to project annual distance. Please supply an annual distance assumption.",
                    explanations
            );
        }

        // 6. Both valid: Compute Raw & Clamped Final Premium
        BigDecimal rawPremium = baseAnnualPremiumInr
                .multiply(riskMultiplier)
                .multiply(usageMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalPremium = rawPremium;
        String boundExplanation = null;

        if (rawPremium.compareTo(minPremiumInr) < 0) {
            finalPremium = minPremiumInr;
            boundExplanation = String.format("Raw premium ₹%,.2f adjusted to minimum policy floor of ₹%,.2f.", rawPremium, minPremiumInr);
        } else if (rawPremium.compareTo(maxPremiumInr) > 0) {
            finalPremium = maxPremiumInr;
            boundExplanation = String.format("Raw premium ₹%,.2f adjusted to maximum policy ceiling of ₹%,.2f.", rawPremium, maxPremiumInr);
        } else {
            boundExplanation = "Raw premium is within policy floor and ceiling bounds.";
        }

        explanations.add(new PricingFactorExplanationDto(
                "PREMIUM_BOUNDS",
                "Policy Floor and Ceiling Limits",
                String.format("₹%,.2f to ₹%,.2f", minPremiumInr, maxPremiumInr),
                rawPremium.compareTo(finalPremium) != 0 ? "ADJUSTED" : "WITHIN_BOUNDS",
                boundExplanation
        ));

        String summary = String.format("Estimated annual premium of ₹%,.2f INR calculated from base ₹%,.2f × %s (risk) × %s (usage).",
                finalPremium, baseAnnualPremiumInr, riskMultiplier, usageMultiplier);

        return new PricingResult(
                "READY",
                true,
                baseAnnualPremiumInr,
                safetyScore,
                riskCategory,
                riskMultiplier,
                inputs.getObservedDistanceKm(),
                inputs.getObservationDays(),
                projectedAnnualKm,
                annualDistanceSource,
                usageMultiplier,
                rawPremium,
                finalPremium,
                inputs.getExcludedTripsCount(),
                summary,
                explanations
        );
    }

    private BigDecimal deriveUsageMultiplier(double annualKm) {
        BigDecimal multiplier;
        if (annualKm < 5000.0) {
            multiplier = new BigDecimal("0.8500");
        } else if (annualKm < 8000.0) {
            multiplier = new BigDecimal("0.9200");
        } else if (annualKm <= 12000.0) {
            multiplier = new BigDecimal("1.0000");
        } else if (annualKm <= 18000.0) {
            multiplier = new BigDecimal("1.1000");
        } else {
            multiplier = new BigDecimal("1.2500");
        }

        // Apply configured bounds
        if (multiplier.compareTo(minUsageMultiplier) < 0) {
            return minUsageMultiplier;
        }
        if (multiplier.compareTo(maxUsageMultiplier) > 0) {
            return maxUsageMultiplier;
        }
        return multiplier;
    }

    private void validateConfiguration() {
        if (baseAnnualPremiumInr == null || baseAnnualPremiumInr.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Base annual premium must be strictly positive");
        }
        if (minPremiumInr == null || minPremiumInr.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum premium cannot be negative");
        }
        if (maxPremiumInr == null || maxPremiumInr.compareTo(minPremiumInr) < 0) {
            throw new IllegalArgumentException("Maximum premium must be greater than or equal to minimum premium");
        }
        if (excellentMultiplier == null || excellentMultiplier.compareTo(BigDecimal.ZERO) <= 0
                || goodMultiplier == null || goodMultiplier.compareTo(BigDecimal.ZERO) <= 0
                || moderateMultiplier == null || moderateMultiplier.compareTo(BigDecimal.ZERO) <= 0
                || highRiskMultiplier == null || highRiskMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Risk multipliers must be strictly positive");
        }
    }

    // Getters / Setters for tests and configuration

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

    public BigDecimal getExcellentMultiplier() {
        return excellentMultiplier;
    }

    public void setExcellentMultiplier(BigDecimal excellentMultiplier) {
        this.excellentMultiplier = excellentMultiplier;
    }

    public BigDecimal getGoodMultiplier() {
        return goodMultiplier;
    }

    public void setGoodMultiplier(BigDecimal goodMultiplier) {
        this.goodMultiplier = goodMultiplier;
    }

    public BigDecimal getModerateMultiplier() {
        return moderateMultiplier;
    }

    public void setModerateMultiplier(BigDecimal moderateMultiplier) {
        this.moderateMultiplier = moderateMultiplier;
    }

    public BigDecimal getHighRiskMultiplier() {
        return highRiskMultiplier;
    }

    public void setHighRiskMultiplier(BigDecimal highRiskMultiplier) {
        this.highRiskMultiplier = highRiskMultiplier;
    }

    public double getBaselineAnnualDistanceKm() {
        return baselineAnnualDistanceKm;
    }

    public void setBaselineAnnualDistanceKm(double baselineAnnualDistanceKm) {
        this.baselineAnnualDistanceKm = baselineAnnualDistanceKm;
    }

    public int getMinObservationDaysForProjection() {
        return minObservationDaysForProjection;
    }

    public void setMinObservationDaysForProjection(int minObservationDaysForProjection) {
        this.minObservationDaysForProjection = minObservationDaysForProjection;
    }

    public double getMinObservedDistanceKmForProjection() {
        return minObservedDistanceKmForProjection;
    }

    public void setMinObservedDistanceKmForProjection(double minObservedDistanceKmForProjection) {
        this.minObservedDistanceKmForProjection = minObservedDistanceKmForProjection;
    }

    public BigDecimal getMinUsageMultiplier() {
        return minUsageMultiplier;
    }

    public void setMinUsageMultiplier(BigDecimal minUsageMultiplier) {
        this.minUsageMultiplier = minUsageMultiplier;
    }

    public BigDecimal getMaxUsageMultiplier() {
        return maxUsageMultiplier;
    }

    public void setMaxUsageMultiplier(BigDecimal maxUsageMultiplier) {
        this.maxUsageMultiplier = maxUsageMultiplier;
    }

    /**
     * Input metrics required for premium calculation.
     */
    public static class PricingInputs {
        private final Double safetyScore;
        private final String riskCategory;
        private final boolean isScoreEligible;
        private final Double observedDistanceKm;
        private final Integer observationDays;
        private final Double userAnnualDistanceAssumption;
        private final int excludedTripsCount;

        public PricingInputs(Double safetyScore, String riskCategory, boolean isScoreEligible,
                             Double observedDistanceKm, Integer observationDays,
                             Double userAnnualDistanceAssumption, int excludedTripsCount) {
            this.safetyScore = safetyScore;
            this.riskCategory = riskCategory;
            this.isScoreEligible = isScoreEligible;
            this.observedDistanceKm = observedDistanceKm;
            this.observationDays = observationDays;
            this.userAnnualDistanceAssumption = userAnnualDistanceAssumption;
            this.excludedTripsCount = excludedTripsCount;
        }

        public Double getSafetyScore() {
            return safetyScore;
        }

        public String getRiskCategory() {
            return riskCategory;
        }

        public boolean isScoreEligible() {
            return isScoreEligible;
        }

        public Double getObservedDistanceKm() {
            return observedDistanceKm;
        }

        public Integer getObservationDays() {
            return observationDays;
        }

        public Double getUserAnnualDistanceAssumption() {
            return userAnnualDistanceAssumption;
        }

        public int getExcludedTripsCount() {
            return excludedTripsCount;
        }
    }

    /**
     * Immutable calculation outcome for actuarial pricing.
     */
    public static class PricingResult {
        private final String status;
        private final boolean isEligible;
        private final BigDecimal baseAnnualPremiumInr;
        private final Double safetyScore;
        private final String riskCategory;
        private final BigDecimal riskMultiplier;
        private final Double observedDistanceKm;
        private final Integer observationDays;
        private final Double projectedAnnualDistanceKm;
        private final String annualDistanceSource;
        private final BigDecimal usageMultiplier;
        private final BigDecimal rawAnnualPremiumInr;
        private final BigDecimal finalAnnualPremiumInr;
        private final int excludedTripsCount;
        private final String summaryExplanation;
        private final List<PricingFactorExplanationDto> factorExplanations;

        public PricingResult(String status, boolean isEligible, BigDecimal baseAnnualPremiumInr,
                             Double safetyScore, String riskCategory, BigDecimal riskMultiplier,
                             Double observedDistanceKm, Integer observationDays,
                             Double projectedAnnualDistanceKm, String annualDistanceSource,
                             BigDecimal usageMultiplier, BigDecimal rawAnnualPremiumInr,
                             BigDecimal finalAnnualPremiumInr, int excludedTripsCount,
                             String summaryExplanation, List<PricingFactorExplanationDto> factorExplanations) {
            this.status = status;
            this.isEligible = isEligible;
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
            this.excludedTripsCount = excludedTripsCount;
            this.summaryExplanation = summaryExplanation;
            this.factorExplanations = factorExplanations;
        }

        public static PricingResult ineligible(String status, BigDecimal basePremium, Double score, String category,
                                               Double observedKm, Integer days, Double projectedKm, String source,
                                               int excludedCount, String summary, List<PricingFactorExplanationDto> factors) {
            return new PricingResult(
                    status,
                    false,
                    basePremium,
                    score,
                    category,
                    null,
                    observedKm,
                    days,
                    projectedKm,
                    source,
                    null,
                    null,
                    null,
                    excludedCount,
                    summary,
                    factors
            );
        }

        public String getStatus() {
            return status;
        }

        public boolean isEligible() {
            return isEligible;
        }

        public BigDecimal getBaseAnnualPremiumInr() {
            return baseAnnualPremiumInr;
        }

        public Double getSafetyScore() {
            return safetyScore;
        }

        public String getRiskCategory() {
            return riskCategory;
        }

        public BigDecimal getRiskMultiplier() {
            return riskMultiplier;
        }

        public Double getObservedDistanceKm() {
            return observedDistanceKm;
        }

        public Integer getObservationDays() {
            return observationDays;
        }

        public Double getProjectedAnnualDistanceKm() {
            return projectedAnnualDistanceKm;
        }

        public String getAnnualDistanceSource() {
            return annualDistanceSource;
        }

        public BigDecimal getUsageMultiplier() {
            return usageMultiplier;
        }

        public BigDecimal getRawAnnualPremiumInr() {
            return rawAnnualPremiumInr;
        }

        public BigDecimal getFinalAnnualPremiumInr() {
            return finalAnnualPremiumInr;
        }

        public int getExcludedTripsCount() {
            return excludedTripsCount;
        }

        public String getSummaryExplanation() {
            return summaryExplanation;
        }

        public List<PricingFactorExplanationDto> getFactorExplanations() {
            return factorExplanations;
        }
    }
}
