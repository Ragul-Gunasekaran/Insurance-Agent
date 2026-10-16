package com.ubi.platform.dto;

import java.time.Instant;

/**
 * Historical safety score data point for time-series visualization.
 */
public class ScoreHistoryPointDto {

    private Long tripId;
    private Instant calculatedAt;
    private Double score;
    private String riskCategory;
    private Double distanceKm;
    private Boolean isScorable;

    public ScoreHistoryPointDto() {
    }

    public ScoreHistoryPointDto(Long tripId, Instant calculatedAt, Double score, String riskCategory, Double distanceKm, Boolean isScorable) {
        this.tripId = tripId;
        this.calculatedAt = calculatedAt;
        this.score = score;
        this.riskCategory = riskCategory;
        this.distanceKm = distanceKm;
        this.isScorable = isScorable;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Boolean getIsScorable() {
        return isScorable;
    }

    public void setIsScorable(Boolean isScorable) {
        this.isScorable = isScorable;
    }
}
