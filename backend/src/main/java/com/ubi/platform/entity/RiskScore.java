package com.ubi.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity representing the calculated behavioral risk safety score for a trip (Step 5).
 * Converts trip features into an explainable 0-100 safety score.
 * Kept strictly independent of actuarial pricing, policy premiums, and vehicle values.
 */
@Entity
@Table(
    name = "risk_scores",
    indexes = {
        @Index(name = "idx_risk_trip", columnList = "trip_id", unique = true),
        @Index(name = "idx_risk_vehicle", columnList = "vehicle_id, calculated_at")
    }
)
public class RiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "score")
    private Double score; // Nullable when data is insufficient

    @Column(name = "score_category", length = 32)
    private String scoreCategory; // EXCELLENT, GOOD, MODERATE, HIGH_RISK, UNRATED

    @Column(name = "scoring_version", nullable = false, length = 32)
    private String scoringVersion = "v1.0";

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "data_quality_status", nullable = false, length = 32)
    private String dataQualityStatus; // SUFFICIENT, INSUFFICIENT_DATA, HIGH_ANOMALY_RATE, etc.

    @Column(name = "is_scorable", nullable = false)
    private Boolean isScorable = true;

    @Column(name = "harsh_braking_penalty")
    private Double harshBrakingPenalty = 0.0;

    @Column(name = "rapid_accel_penalty")
    private Double rapidAccelerationPenalty = 0.0;

    @Column(name = "speeding_penalty")
    private Double speedingPenalty = 0.0;

    @Column(name = "night_driving_penalty")
    private Double nightDrivingPenalty = 0.0;

    @Column(name = "total_penalties")
    private Double totalPenalties = 0.0;

    @Column(name = "summary_explanation", length = 1000)
    private String summaryExplanation;

    @Column(name = "factor_explanations_json", columnDefinition = "TEXT")
    private String factorExplanationsJson;

    public RiskScore() {
    }

    public RiskScore(Trip trip, Vehicle vehicle, Double score, String scoreCategory,
                     String scoringVersion, String dataQualityStatus, Boolean isScorable,
                     Double harshBrakingPenalty, Double rapidAccelerationPenalty,
                     Double speedingPenalty, Double nightDrivingPenalty, Double totalPenalties,
                     String summaryExplanation, String factorExplanationsJson) {
        this.trip = trip;
        this.vehicle = vehicle;
        this.score = score;
        this.scoreCategory = scoreCategory;
        this.scoringVersion = scoringVersion != null ? scoringVersion : "v1.0";
        this.calculatedAt = Instant.now();
        this.dataQualityStatus = dataQualityStatus;
        this.isScorable = isScorable != null ? isScorable : true;
        this.harshBrakingPenalty = harshBrakingPenalty != null ? harshBrakingPenalty : 0.0;
        this.rapidAccelerationPenalty = rapidAccelerationPenalty != null ? rapidAccelerationPenalty : 0.0;
        this.speedingPenalty = speedingPenalty != null ? speedingPenalty : 0.0;
        this.nightDrivingPenalty = nightDrivingPenalty != null ? nightDrivingPenalty : 0.0;
        this.totalPenalties = totalPenalties != null ? totalPenalties : 0.0;
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

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getScoreCategory() {
        return scoreCategory;
    }

    public void setScoreCategory(String scoreCategory) {
        this.scoreCategory = scoreCategory;
    }

    public String getScoringVersion() {
        return scoringVersion;
    }

    public void setScoringVersion(String scoringVersion) {
        this.scoringVersion = scoringVersion;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public String getDataQualityStatus() {
        return dataQualityStatus;
    }

    public void setDataQualityStatus(String dataQualityStatus) {
        this.dataQualityStatus = dataQualityStatus;
    }

    public Boolean getIsScorable() {
        return isScorable;
    }

    public void setIsScorable(Boolean isScorable) {
        this.isScorable = isScorable;
    }

    public Double getHarshBrakingPenalty() {
        return harshBrakingPenalty;
    }

    public void setHarshBrakingPenalty(Double harshBrakingPenalty) {
        this.harshBrakingPenalty = harshBrakingPenalty;
    }

    public Double getRapidAccelerationPenalty() {
        return rapidAccelerationPenalty;
    }

    public void setRapidAccelerationPenalty(Double rapidAccelerationPenalty) {
        this.rapidAccelerationPenalty = rapidAccelerationPenalty;
    }

    public Double getSpeedingPenalty() {
        return speedingPenalty;
    }

    public void setSpeedingPenalty(Double speedingPenalty) {
        this.speedingPenalty = speedingPenalty;
    }

    public Double getNightDrivingPenalty() {
        return nightDrivingPenalty;
    }

    public void setNightDrivingPenalty(Double nightDrivingPenalty) {
        this.nightDrivingPenalty = nightDrivingPenalty;
    }

    public Double getTotalPenalties() {
        return totalPenalties;
    }

    public void setTotalPenalties(Double totalPenalties) {
        this.totalPenalties = totalPenalties;
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
