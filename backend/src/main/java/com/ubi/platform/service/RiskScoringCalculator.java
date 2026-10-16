package com.ubi.platform.service;

import com.ubi.platform.dto.RiskFactorExplanationDto;
import com.ubi.platform.entity.DrivingFeature;
import com.ubi.platform.entity.Trip;
import com.ubi.platform.entity.TripStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure deterministic rule-based calculation engine for behavioral risk scoring (Step 5).
 * Completely decoupled from persistence, Spring web context, or machine learning frameworks.
 */
@Component
public class RiskScoringCalculator {

    @Value("${app.risk.base-score:100.0}")
    private double baseScore = 100.0;

    @Value("${app.risk.harsh-braking-weight:2.0}")
    private double harshBrakingWeight = 2.0;

    @Value("${app.risk.harsh-braking-cap:30.0}")
    private double harshBrakingCap = 30.0;

    @Value("${app.risk.rapid-accel-weight:1.5}")
    private double rapidAccelWeight = 1.5;

    @Value("${app.risk.rapid-accel-cap:25.0}")
    private double rapidAccelCap = 25.0;

    @Value("${app.risk.speeding-weight:2.5}")
    private double speedingWeight = 2.5;

    @Value("${app.risk.speeding-cap:35.0}")
    private double speedingCap = 35.0;

    @Value("${app.risk.speed-severity-threshold-kmh:130.0}")
    private double speedSeverityThresholdKmh = 130.0;

    @Value("${app.risk.speed-severity-weight:0.5}")
    private double speedSeverityWeight = 0.5;

    @Value("${app.risk.night-driving-max-penalty:5.0}")
    private double nightDrivingMaxPenalty = 5.0;

    @Value("${app.risk.night-driving-cap:5.0}")
    private double nightDrivingCap = 5.0;

    @Value("${app.risk.min-distance-km:0.5}")
    private double minDistanceKm = 0.5;

    @Value("${app.risk.min-valid-records:10}")
    private int minValidRecords = 10;

    @Value("${app.risk.min-duration-seconds:60}")
    private long minDurationSeconds = 60L;

    @Value("${app.risk.min-data-quality-score:50.0}")
    private double minDataQualityScore = 50.0;

    public RiskScoringCalculator() {
    }

    /**
     * Calculates the safety risk score and explanations for a trip and its extracted feature vector.
     */
    public ScoringResult calculateScore(Trip trip, DrivingFeature feature) {
        // 1. Check for missing entities
        if (trip == null || feature == null) {
            return ScoringResult.insufficientData(
                    "INSUFFICIENT_DATA",
                    "Trip or extracted driving behavioral features are not available.",
                    buildExcludedFactors("Feature telemetry not computed")
            );
        }

        // 2. Check for incomplete ongoing trip
        if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            return ScoringResult.insufficientData(
                    "IN_PROGRESS_TRIP",
                    "Trip journey is currently in progress. Safety risk scoring requires a completed trip session.",
                    buildExcludedFactors("Trip session in progress")
            );
        }

        // 3. Minimum distance requirement
        Double distanceKm = feature.getDistanceKm() != null ? feature.getDistanceKm() : trip.getDistanceKm();
        if (distanceKm == null || distanceKm < minDistanceKm) {
            double dist = distanceKm != null ? distanceKm : 0.0;
            return ScoringResult.insufficientData(
                    "INSUFFICIENT_DISTANCE",
                    String.format("Trip distance (%.2f km) is below the minimum required distance (%.2f km) for statistically reliable behavioral scoring.", dist, minDistanceKm),
                    buildExcludedFactors(String.format("Distance %.2f km < %.2f km threshold", dist, minDistanceKm))
            );
        }

        // 4. Minimum valid telemetry sample size
        Integer validRecords = feature.getValidRecordCount();
        if (validRecords == null || validRecords < minValidRecords) {
            int vCount = validRecords != null ? validRecords : 0;
            return ScoringResult.insufficientData(
                    "INSUFFICIENT_TELEMETRY",
                    String.format("Valid telemetry record count (%d) is below the minimum sample size (%d pings).", vCount, minValidRecords),
                    buildExcludedFactors(String.format("Telemetry sample count %d < %d", vCount, minValidRecords))
            );
        }

        // 5. Minimum duration requirement
        Long durationSeconds = feature.getDurationSeconds() != null ? feature.getDurationSeconds() : trip.getDurationSeconds();
        if (durationSeconds == null || durationSeconds < minDurationSeconds) {
            long dur = durationSeconds != null ? durationSeconds : 0L;
            return ScoringResult.insufficientData(
                    "INSUFFICIENT_DURATION",
                    String.format("Trip duration (%d s) is below the minimum required duration (%d s).", dur, minDurationSeconds),
                    buildExcludedFactors(String.format("Duration %d s < %d s threshold", dur, minDurationSeconds))
            );
        }

        // 6. Data quality threshold
        Double dataQualityScore = feature.getDataQualityScore();
        if (dataQualityScore != null && dataQualityScore < minDataQualityScore) {
            return ScoringResult.insufficientData(
                    "HIGH_ANOMALY_RATE",
                    String.format("Data quality score (%.1f%%) indicates excessive sensor anomalies or GPS jumps, failing reliability standards (>= %.1f%%).", dataQualityScore, minDataQualityScore),
                    buildExcludedFactors(String.format("Data quality %.1f%% below acceptable reliability threshold", dataQualityScore))
            );
        }

        // 7. Check for null normalized rates
        if (feature.getHarshBrakeRatePer100Km() == null || feature.getRapidAccelRatePer100Km() == null) {
            return ScoringResult.insufficientData(
                    "INSUFFICIENT_DATA",
                    "Normalized event rates are null or undefined for this journey.",
                    buildExcludedFactors("Undefined normalized rates")
            );
        }

        // --- Sufficient Data: Rule-based Deductions ---
        List<RiskFactorExplanationDto> factors = new ArrayList<>();

        // A. Harsh Braking Penalty
        double harshBrakeRate = feature.getHarshBrakeRatePer100Km();
        double rawHarshBrakePenalty = Math.round(harshBrakeRate * harshBrakingWeight * 10.0) / 10.0;
        double harshBrakingPenalty = Math.min(rawHarshBrakePenalty, harshBrakingCap);
        String harshBrakeExpl = (harshBrakingPenalty > 0)
                ? String.format("Observed %.1f harsh braking events/100km resulting in a %.1f point deduction (cap: %.1f).", harshBrakeRate, harshBrakingPenalty, harshBrakingCap)
                : "Smooth deceleration observed; zero harsh braking events detected.";
        factors.add(new RiskFactorExplanationDto("HARSH_BRAKING", harshBrakeRate, "events/100km", harshBrakingPenalty, harshBrakingCap, true, harshBrakeExpl));

        // B. Rapid Acceleration Penalty
        double rapidAccelRate = feature.getRapidAccelRatePer100Km();
        double rawRapidAccelPenalty = Math.round(rapidAccelRate * rapidAccelWeight * 10.0) / 10.0;
        double rapidAccelerationPenalty = Math.min(rawRapidAccelPenalty, rapidAccelCap);
        String rapidAccelExpl = (rapidAccelerationPenalty > 0)
                ? String.format("Observed %.1f rapid acceleration events/100km resulting in a %.1f point deduction (cap: %.1f).", rapidAccelRate, rapidAccelerationPenalty, rapidAccelCap)
                : "Controlled throttle progression observed; zero rapid acceleration events detected.";
        factors.add(new RiskFactorExplanationDto("RAPID_ACCELERATION", rapidAccelRate, "events/100km", rapidAccelerationPenalty, rapidAccelCap, true, rapidAccelExpl));

        // C. Speeding Penalty
        int speedingCount = feature.getSpeedingObservationsCount() != null ? feature.getSpeedingObservationsCount() : 0;
        double speedingRatePer100Km = Math.round(((double) speedingCount / distanceKm) * 100.0 * 10.0) / 10.0;
        double rawSpeedingPenalty = Math.round(speedingRatePer100Km * speedingWeight * 10.0) / 10.0;
        double maxSpeed = feature.getMaxSpeedKmh() != null ? feature.getMaxSpeedKmh() : 0.0;
        double severityExtra = 0.0;
        if (maxSpeed > speedSeverityThresholdKmh) {
            severityExtra = Math.round((maxSpeed - speedSeverityThresholdKmh) * speedSeverityWeight * 10.0) / 10.0;
        }
        double speedingPenalty = Math.min(Math.round((rawSpeedingPenalty + severityExtra) * 10.0) / 10.0, speedingCap);
        String speedingExpl = (speedingPenalty > 0)
                ? String.format("Observed %d speeding observations (%.1f/100km) with max speed %.1f km/h, incurring a %.1f point deduction (cap: %.1f).",
                speedingCount, speedingRatePer100Km, maxSpeed, speedingPenalty, speedingCap)
                : String.format("Vehicle operated within speed limits; max speed recorded at %.1f km/h.", maxSpeed);
        factors.add(new RiskFactorExplanationDto("SPEEDING", speedingRatePer100Km, "events/100km", speedingPenalty, speedingCap, true, speedingExpl));

        // D. Night-Driving Exposure Penalty
        double nightRatio = feature.getNightDrivingRatio() != null ? feature.getNightDrivingRatio() : 0.0;
        double rawNightPenalty = Math.round(nightRatio * nightDrivingMaxPenalty * 10.0) / 10.0;
        double nightDrivingPenalty = Math.min(rawNightPenalty, nightDrivingCap);
        String nightExpl = (nightDrivingPenalty > 0)
                ? String.format("Night driving exposure was %.1f%% of trip duration (22:00-05:00), incurring a %.1f point deduction (cap: %.1f).",
                nightRatio * 100.0, nightDrivingPenalty, nightDrivingCap)
                : "Trip conducted entirely during daylight/safe operating hours; zero night penalty.";
        factors.add(new RiskFactorExplanationDto("NIGHT_DRIVING", Math.round(nightRatio * 100.0 * 10.0) / 10.0, "%", nightDrivingPenalty, nightDrivingCap, true, nightExpl));

        // E. Data Quality Metric
        factors.add(new RiskFactorExplanationDto("DATA_QUALITY", dataQualityScore, "%", 0.0, 0.0, true,
                String.format("Data quality is %.1f%% (%s) with %d valid and %d anomalous records.",
                        dataQualityScore != null ? dataQualityScore : 100.0,
                        feature.getDataQualityStatus(),
                        validRecords,
                        feature.getAnomalousRecordCount() != null ? feature.getAnomalousRecordCount() : 0)));

        // Total Penalties & Clamped Final Score
        double totalPenalties = Math.round((harshBrakingPenalty + rapidAccelerationPenalty + speedingPenalty + nightDrivingPenalty) * 10.0) / 10.0;
        double finalScore = Math.max(0.0, Math.min(100.0, Math.round((baseScore - totalPenalties) * 10.0) / 10.0));

        // Score Categorization
        String category;
        if (finalScore >= 85.0) {
            category = "EXCELLENT";
        } else if (finalScore >= 70.0) {
            category = "GOOD";
        } else if (finalScore >= 50.0) {
            category = "MODERATE";
        } else {
            category = "HIGH_RISK";
        }

        String summary = String.format("Safety score of %.1f/100 (%s). Base score 100.0 minus %.1f total deductions: Harsh Braking (-%.1f), Rapid Accel (-%.1f), Speeding (-%.1f), Night Driving (-%.1f).",
                finalScore, category, totalPenalties, harshBrakingPenalty, rapidAccelerationPenalty, speedingPenalty, nightDrivingPenalty);

        return new ScoringResult(
                finalScore,
                category,
                "SUFFICIENT",
                true,
                harshBrakingPenalty,
                rapidAccelerationPenalty,
                speedingPenalty,
                nightDrivingPenalty,
                totalPenalties,
                summary,
                factors
        );
    }

    private List<RiskFactorExplanationDto> buildExcludedFactors(String reason) {
        List<RiskFactorExplanationDto> list = new ArrayList<>();
        list.add(RiskFactorExplanationDto.excluded("HARSH_BRAKING", "Excluded: " + reason));
        list.add(RiskFactorExplanationDto.excluded("RAPID_ACCELERATION", "Excluded: " + reason));
        list.add(RiskFactorExplanationDto.excluded("SPEEDING", "Excluded: " + reason));
        list.add(RiskFactorExplanationDto.excluded("NIGHT_DRIVING", "Excluded: " + reason));
        list.add(RiskFactorExplanationDto.excluded("DATA_QUALITY", "Data reliability insufficient for scoring"));
        return list;
    }

    // Getters and Setters for unit testing

    public void setBaseScore(double baseScore) {
        this.baseScore = baseScore;
    }

    public void setHarshBrakingWeight(double harshBrakingWeight) {
        this.harshBrakingWeight = harshBrakingWeight;
    }

    public void setHarshBrakingCap(double harshBrakingCap) {
        this.harshBrakingCap = harshBrakingCap;
    }

    public void setRapidAccelWeight(double rapidAccelWeight) {
        this.rapidAccelWeight = rapidAccelWeight;
    }

    public void setRapidAccelCap(double rapidAccelCap) {
        this.rapidAccelCap = rapidAccelCap;
    }

    public void setSpeedingWeight(double speedingWeight) {
        this.speedingWeight = speedingWeight;
    }

    public void setSpeedingCap(double speedingCap) {
        this.speedingCap = speedingCap;
    }

    public void setSpeedSeverityThresholdKmh(double speedSeverityThresholdKmh) {
        this.speedSeverityThresholdKmh = speedSeverityThresholdKmh;
    }

    public void setSpeedSeverityWeight(double speedSeverityWeight) {
        this.speedSeverityWeight = speedSeverityWeight;
    }

    public void setNightDrivingMaxPenalty(double nightDrivingMaxPenalty) {
        this.nightDrivingMaxPenalty = nightDrivingMaxPenalty;
    }

    public void setNightDrivingCap(double nightDrivingCap) {
        this.nightDrivingCap = nightDrivingCap;
    }

    public void setMinDistanceKm(double minDistanceKm) {
        this.minDistanceKm = minDistanceKm;
    }

    public void setMinValidRecords(int minValidRecords) {
        this.minValidRecords = minValidRecords;
    }

    public void setMinDurationSeconds(long minDurationSeconds) {
        this.minDurationSeconds = minDurationSeconds;
    }

    public void setMinDataQualityScore(double minDataQualityScore) {
        this.minDataQualityScore = minDataQualityScore;
    }

    /**
     * Immutable value object holding the calculation outcome.
     */
    public static class ScoringResult {
        private final Double score;
        private final String scoreCategory;
        private final String dataQualityStatus;
        private final boolean isScorable;
        private final double harshBrakingPenalty;
        private final double rapidAccelerationPenalty;
        private final double speedingPenalty;
        private final double nightDrivingPenalty;
        private final double totalPenalties;
        private final String summaryExplanation;
        private final List<RiskFactorExplanationDto> factorExplanations;

        public ScoringResult(Double score, String scoreCategory, String dataQualityStatus, boolean isScorable,
                             double harshBrakingPenalty, double rapidAccelerationPenalty, double speedingPenalty,
                             double nightDrivingPenalty, double totalPenalties, String summaryExplanation,
                             List<RiskFactorExplanationDto> factorExplanations) {
            this.score = score;
            this.scoreCategory = scoreCategory;
            this.dataQualityStatus = dataQualityStatus;
            this.isScorable = isScorable;
            this.harshBrakingPenalty = harshBrakingPenalty;
            this.rapidAccelerationPenalty = rapidAccelerationPenalty;
            this.speedingPenalty = speedingPenalty;
            this.nightDrivingPenalty = nightDrivingPenalty;
            this.totalPenalties = totalPenalties;
            this.summaryExplanation = summaryExplanation;
            this.factorExplanations = factorExplanations;
        }

        public static ScoringResult insufficientData(String status, String summary, List<RiskFactorExplanationDto> factors) {
            return new ScoringResult(
                    null,
                    "UNRATED",
                    status,
                    false,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    summary,
                    factors
            );
        }

        public Double getScore() {
            return score;
        }

        public String getScoreCategory() {
            return scoreCategory;
        }

        public String getDataQualityStatus() {
            return dataQualityStatus;
        }

        public boolean isScorable() {
            return isScorable;
        }

        public double getHarshBrakingPenalty() {
            return harshBrakingPenalty;
        }

        public double getRapidAccelerationPenalty() {
            return rapidAccelerationPenalty;
        }

        public double getSpeedingPenalty() {
            return speedingPenalty;
        }

        public double getNightDrivingPenalty() {
            return nightDrivingPenalty;
        }

        public double getTotalPenalties() {
            return totalPenalties;
        }

        public String getSummaryExplanation() {
            return summaryExplanation;
        }

        public List<RiskFactorExplanationDto> getFactorExplanations() {
            return factorExplanations;
        }
    }
}
