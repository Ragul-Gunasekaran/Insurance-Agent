package com.ubi.platform.service;

import com.ubi.platform.dto.PricingFactorExplanationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the pure deterministic PricingCalculator component (Step 6).
 */
class PricingCalculatorTest {

    private PricingCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PricingCalculator();
        calculator.setBaseAnnualPremiumInr(new BigDecimal("12000.00"));
        calculator.setMinPremiumInr(new BigDecimal("5000.00"));
        calculator.setMaxPremiumInr(new BigDecimal("35000.00"));
        calculator.setExcellentMultiplier(new BigDecimal("0.9000"));
        calculator.setGoodMultiplier(new BigDecimal("1.0000"));
        calculator.setModerateMultiplier(new BigDecimal("1.1500"));
        calculator.setHighRiskMultiplier(new BigDecimal("1.3000"));
        calculator.setMinObservationDaysForProjection(7);
        calculator.setMinObservedDistanceKmForProjection(20.0);
    }

    @Test
    @DisplayName("Each risk category selects configured multiplier")
    void testEachRiskCategorySelectsConfiguredMultiplier() {
        // EXCELLENT (score 90.0) with standard 10,000 km user assumption
        PricingCalculator.PricingInputs inExcellent = new PricingCalculator.PricingInputs(
                90.0, "EXCELLENT", true, null, null, 10000.0, 0
        );
        PricingCalculator.PricingResult resExcellent = calculator.calculate(inExcellent);
        assertEquals(new BigDecimal("0.9000"), resExcellent.getRiskMultiplier());
        assertEquals("EXCELLENT", resExcellent.getRiskCategory());
        // 12000 * 0.90 * 1.00 = 10800.00
        assertEquals(new BigDecimal("10800.00"), resExcellent.getFinalAnnualPremiumInr());

        // GOOD (score 75.0)
        PricingCalculator.PricingInputs inGood = new PricingCalculator.PricingInputs(
                75.0, "GOOD", true, null, null, 10000.0, 0
        );
        PricingCalculator.PricingResult resGood = calculator.calculate(inGood);
        assertEquals(new BigDecimal("1.0000"), resGood.getRiskMultiplier());
        assertEquals(new BigDecimal("12000.00"), resGood.getFinalAnnualPremiumInr());

        // MODERATE (score 60.0)
        PricingCalculator.PricingInputs inModerate = new PricingCalculator.PricingInputs(
                60.0, "MODERATE", true, null, null, 10000.0, 0
        );
        PricingCalculator.PricingResult resModerate = calculator.calculate(inModerate);
        assertEquals(new BigDecimal("1.1500"), resModerate.getRiskMultiplier());
        // 12000 * 1.15 * 1.00 = 13800.00
        assertEquals(new BigDecimal("13800.00"), resModerate.getFinalAnnualPremiumInr());

        // HIGH_RISK (score 35.0)
        PricingCalculator.PricingInputs inHighRisk = new PricingCalculator.PricingInputs(
                35.0, "HIGH_RISK", true, null, null, 10000.0, 0
        );
        PricingCalculator.PricingResult resHighRisk = calculator.calculate(inHighRisk);
        assertEquals(new BigDecimal("1.3000"), resHighRisk.getRiskMultiplier());
        // 12000 * 1.30 * 1.00 = 15600.00
        assertEquals(new BigDecimal("15600.00"), resHighRisk.getFinalAnnualPremiumInr());
    }

    @Test
    @DisplayName("Scores at category boundaries are mapped accurately")
    void testScoresAtCategoryBoundaries() {
        // Boundary 85.0 -> EXCELLENT
        PricingCalculator.PricingResult r85 = calculator.calculate(new PricingCalculator.PricingInputs(
                85.0, null, true, null, null, 10000.0, 0
        ));
        assertEquals("EXCELLENT", r85.getRiskCategory());
        assertEquals(new BigDecimal("0.9000"), r85.getRiskMultiplier());

        // Boundary 84.9 -> GOOD
        PricingCalculator.PricingResult r849 = calculator.calculate(new PricingCalculator.PricingInputs(
                84.9, null, true, null, null, 10000.0, 0
        ));
        assertEquals("GOOD", r849.getRiskCategory());
        assertEquals(new BigDecimal("1.0000"), r849.getRiskMultiplier());

        // Boundary 70.0 -> GOOD
        PricingCalculator.PricingResult r70 = calculator.calculate(new PricingCalculator.PricingInputs(
                70.0, null, true, null, null, 10000.0, 0
        ));
        assertEquals("GOOD", r70.getRiskCategory());
        assertEquals(new BigDecimal("1.0000"), r70.getRiskMultiplier());

        // Boundary 69.9 -> MODERATE
        PricingCalculator.PricingResult r699 = calculator.calculate(new PricingCalculator.PricingInputs(
                69.9, null, true, null, null, 10000.0, 0
        ));
        assertEquals("MODERATE", r699.getRiskCategory());
        assertEquals(new BigDecimal("1.1500"), r699.getRiskMultiplier());

        // Boundary 50.0 -> MODERATE
        PricingCalculator.PricingResult r50 = calculator.calculate(new PricingCalculator.PricingInputs(
                50.0, null, true, null, null, 10000.0, 0
        ));
        assertEquals("MODERATE", r50.getRiskCategory());
        assertEquals(new BigDecimal("1.1500"), r50.getRiskMultiplier());

        // Boundary 49.9 -> HIGH_RISK
        PricingCalculator.PricingResult r499 = calculator.calculate(new PricingCalculator.PricingInputs(
                49.9, null, true, null, null, 10000.0, 0
        ));
        assertEquals("HIGH_RISK", r499.getRiskCategory());
        assertEquals(new BigDecimal("1.3000"), r499.getRiskMultiplier());
    }

    @Test
    @DisplayName("UNRATED or missing score cannot produce valid premium")
    void testUnratedOrMissingScoreCannotProduceValidPremium() {
        // UNRATED score
        PricingCalculator.PricingInputs unratedInputs = new PricingCalculator.PricingInputs(
                null, "UNRATED", false, null, null, 10000.0, 0
        );
        PricingCalculator.PricingResult result = calculator.calculate(unratedInputs);

        assertFalse(result.isEligible());
        assertEquals("INSUFFICIENT_RISK_DATA", result.getStatus());
        assertNull(result.getFinalAnnualPremiumInr());
        assertNull(result.getRawAnnualPremiumInr());
        assertNull(result.getRiskMultiplier());
    }

    @Test
    @DisplayName("Insufficient usage observation produces INSUFFICIENT_USAGE_DATA")
    void testInsufficientUsageObservationProducesStatus() {
        // Only 5 km over 2 days (below 20km and 7 days minimums) and no user override
        PricingCalculator.PricingInputs inputs = new PricingCalculator.PricingInputs(
                85.0, "EXCELLENT", true, 5.0, 2, null, 0
        );
        PricingCalculator.PricingResult result = calculator.calculate(inputs);

        assertFalse(result.isEligible());
        assertEquals("INSUFFICIENT_USAGE_DATA", result.getStatus());
        assertNull(result.getFinalAnnualPremiumInr());
    }

    @Test
    @DisplayName("Both risk and usage insufficient produces INSUFFICIENT_DATA")
    void testBothRiskAndUsageInsufficientProducesInsufficientData() {
        PricingCalculator.PricingInputs inputs = new PricingCalculator.PricingInputs(
                null, "UNRATED", false, null, null, null, 2
        );
        PricingCalculator.PricingResult result = calculator.calculate(inputs);

        assertFalse(result.isEligible());
        assertEquals("INSUFFICIENT_DATA", result.getStatus());
        assertNull(result.getFinalAnnualPremiumInr());
    }

    @Test
    @DisplayName("Telemetry annualization applies when thresholds met")
    void testTelemetryAnnualizationWhenThresholdsMet() {
        // 100 km over 10 days -> (100 / 10) * 365.25 = 3,652.5 km/year -> Tier 1 (<5000 km) -> 0.8500 multiplier
        PricingCalculator.PricingInputs inputs = new PricingCalculator.PricingInputs(
                85.0, "EXCELLENT", true, 100.0, 10, null, 0
        );
        PricingCalculator.PricingResult result = calculator.calculate(inputs);

        assertTrue(result.isEligible());
        assertEquals("READY", result.getStatus());
        assertEquals("TELEMETRY_PROJECTED", result.getAnnualDistanceSource());
        assertEquals(3652.5, result.getProjectedAnnualDistanceKm());
        assertEquals(new BigDecimal("0.8500"), result.getUsageMultiplier());
        // 12000 * 0.90 * 0.85 = 9180.00
        assertEquals(new BigDecimal("9180.00"), result.getFinalAnnualPremiumInr());
    }

    @Test
    @DisplayName("Policy floor clamp applied when raw premium falls below minimum")
    void testPolicyFloorClampApplied() {
        // Set base premium low so raw calculation falls below minPremiumInr (5000.00)
        calculator.setBaseAnnualPremiumInr(new BigDecimal("4000.00"));

        PricingCalculator.PricingInputs inputs = new PricingCalculator.PricingInputs(
                95.0, "EXCELLENT", true, null, null, 4000.0, 0 // low usage multiplier 0.85
        );
        PricingCalculator.PricingResult result = calculator.calculate(inputs);

        // Raw = 4000 * 0.90 * 0.85 = 3060.00
        assertEquals(new BigDecimal("3060.00"), result.getRawAnnualPremiumInr());
        // Clamped to floor 5000.00
        assertEquals(new BigDecimal("5000.00"), result.getFinalAnnualPremiumInr());
    }

    @Test
    @DisplayName("Policy ceiling clamp applied when raw premium exceeds maximum")
    void testPolicyCeilingClampApplied() {
        // Set base premium high and high risk + high mileage
        calculator.setBaseAnnualPremiumInr(new BigDecimal("30000.00"));

        PricingCalculator.PricingInputs inputs = new PricingCalculator.PricingInputs(
                30.0, "HIGH_RISK", true, null, null, 25000.0, 0 // usage multiplier 1.2500
        );
        PricingCalculator.PricingResult result = calculator.calculate(inputs);

        // Raw = 30000 * 1.30 * 1.25 = 48750.00
        assertEquals(new BigDecimal("48750.00"), result.getRawAnnualPremiumInr());
        // Clamped to ceiling 35000.00
        assertEquals(new BigDecimal("35000.00"), result.getFinalAnnualPremiumInr());
    }

    @Test
    @DisplayName("Deterministic repeatable calculation with identical inputs")
    void testDeterministicRepeatableCalculation() {
        PricingCalculator.PricingInputs inputs = new PricingCalculator.PricingInputs(
                88.0, "EXCELLENT", true, null, null, 12000.0, 0
        );

        PricingCalculator.PricingResult r1 = calculator.calculate(inputs);
        PricingCalculator.PricingResult r2 = calculator.calculate(inputs);

        assertEquals(r1.getFinalAnnualPremiumInr(), r2.getFinalAnnualPremiumInr());
        assertEquals(r1.getRawAnnualPremiumInr(), r2.getRawAnnualPremiumInr());
        assertEquals(r1.getRiskMultiplier(), r2.getRiskMultiplier());
        assertEquals(r1.getUsageMultiplier(), r2.getUsageMultiplier());
    }

    @Test
    @DisplayName("Rejects negative or invalid configuration values")
    void testRejectsInvalidConfigurationValues() {
        calculator.setBaseAnnualPremiumInr(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(
                new PricingCalculator.PricingInputs(90.0, "EXCELLENT", true, null, null, 10000.0, 0)
        ));

        calculator.setBaseAnnualPremiumInr(new BigDecimal("12000.00"));
        calculator.setMinPremiumInr(new BigDecimal("-100.00"));
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(
                new PricingCalculator.PricingInputs(90.0, "EXCELLENT", true, null, null, 10000.0, 0)
        ));
    }
}
