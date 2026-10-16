package com.ubi.platform.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.entity.RiskScore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * REST response DTO for behavioral risk safety scores and explainability factors (Step 5).
 */
public class RiskScoreResponseDto {

    private Long id;
    private Long tripId;
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Double score;
    private String scoreCategory;
    private String scoringVersion;
    private Instant calculatedAt;
    private String dataQualityStatus;
    private Boolean isScorable;
    private Double harshBrakingPenalty;
    private Double rapidAccelerationPenalty;
    private Double speedingPenalty;
    private Double nightDrivingPenalty;
    private Double totalPenalties;
    private String summaryExplanation;
    private List<RiskFactorExplanationDto> factorExplanations = new ArrayList<>();

    public RiskScoreResponseDto() {
    }

    public static RiskScoreResponseDto fromEntity(RiskScore entity, ObjectMapper objectMapper) {
        if (entity == null) {
            return null;
        }

        RiskScoreResponseDto dto = new RiskScoreResponseDto();
        dto.setId(entity.getId());
        dto.setTripId(entity.getTrip() != null ? entity.getTrip().getId() : null);
        dto.setVehicleId(entity.getVehicle() != null ? entity.getVehicle().getId() : null);
        dto.setVehicleRegistrationNumber(entity.getVehicle() != null ? entity.getVehicle().getVehicleNumber() : null);
        dto.setScore(entity.getScore());
        dto.setScoreCategory(entity.getScoreCategory());
        dto.setScoringVersion(entity.getScoringVersion());
        dto.setCalculatedAt(entity.getCalculatedAt());
        dto.setDataQualityStatus(entity.getDataQualityStatus());
        dto.setIsScorable(entity.getIsScorable());
        dto.setHarshBrakingPenalty(entity.getHarshBrakingPenalty());
        dto.setRapidAccelerationPenalty(entity.getRapidAccelerationPenalty());
        dto.setSpeedingPenalty(entity.getSpeedingPenalty());
        dto.setNightDrivingPenalty(entity.getNightDrivingPenalty());
        dto.setTotalPenalties(entity.getTotalPenalties());
        dto.setSummaryExplanation(entity.getSummaryExplanation());

        if (entity.getFactorExplanationsJson() != null && objectMapper != null) {
            try {
                List<RiskFactorExplanationDto> explanations = objectMapper.readValue(
                        entity.getFactorExplanationsJson(),
                        new TypeReference<List<RiskFactorExplanationDto>>() {}
                );
                dto.setFactorExplanations(explanations);
            } catch (Exception e) {
                // In case of parsing error, return empty list
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

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
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

    public List<RiskFactorExplanationDto> getFactorExplanations() {
        return factorExplanations;
    }

    public void setFactorExplanations(List<RiskFactorExplanationDto> factorExplanations) {
        this.factorExplanations = factorExplanations;
    }
}
