package com.ubi.platform.dto;

import com.ubi.platform.entity.DrivingFeature;
import java.time.Instant;

/**
 * Outbound DTO representing calculated trip-level driving risk features.
 */
public class DrivingFeatureResponseDto {

    private Long id;
    private Long tripId;
    private Long vehicleId;
    private String calculationVersion;
    private Instant calculatedAt;
    private Double distanceKm;
    private Long durationSeconds;
    private String durationFormatted;
    private Double averageSpeedKmh;
    private Double maxSpeedKmh;
    private Integer harshBrakingCount;
    private Integer rapidAccelerationCount;
    private Integer speedingObservationsCount;
    private Double harshBrakeRatePer100Km;
    private Double rapidAccelRatePer100Km;
    private Double speedingPct;
    private Long nightDrivingMinutes;
    private Double nightDrivingRatio;
    private Integer validRecordCount;
    private Integer anomalousRecordCount;
    private Double dataQualityScore;
    private String dataQualityStatus;

    public DrivingFeatureResponseDto() {
    }

    public static DrivingFeatureResponseDto fromEntity(DrivingFeature feature) {
        if (feature == null) {
            return null;
        }
        DrivingFeatureResponseDto dto = new DrivingFeatureResponseDto();
        dto.setId(feature.getId());
        dto.setTripId(feature.getTrip() != null ? feature.getTrip().getId() : null);
        dto.setVehicleId(feature.getVehicle() != null ? feature.getVehicle().getId() : null);
        dto.setCalculationVersion(feature.getCalculationVersion());
        dto.setCalculatedAt(feature.getCalculatedAt());
        dto.setDistanceKm(feature.getDistanceKm());
        dto.setDurationSeconds(feature.getDurationSeconds());
        dto.setDurationFormatted(formatDuration(feature.getDurationSeconds()));
        dto.setAverageSpeedKmh(feature.getAverageSpeedKmh());
        dto.setMaxSpeedKmh(feature.getMaxSpeedKmh());
        dto.setHarshBrakingCount(feature.getHarshBrakingCount());
        dto.setRapidAccelerationCount(feature.getRapidAccelerationCount());
        dto.setSpeedingObservationsCount(feature.getSpeedingObservationsCount());
        dto.setHarshBrakeRatePer100Km(feature.getHarshBrakeRatePer100Km());
        dto.setRapidAccelRatePer100Km(feature.getRapidAccelRatePer100Km());
        dto.setSpeedingPct(feature.getSpeedingPct());
        dto.setNightDrivingMinutes(feature.getNightDrivingMinutes());
        dto.setNightDrivingRatio(feature.getNightDrivingRatio());
        dto.setValidRecordCount(feature.getValidRecordCount());
        dto.setAnomalousRecordCount(feature.getAnomalousRecordCount());
        dto.setDataQualityScore(feature.getDataQualityScore());
        dto.setDataQualityStatus(feature.getDataQualityStatus());
        return dto;
    }

    private static String formatDuration(Long seconds) {
        if (seconds == null || seconds <= 0) {
            return "0s";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %02ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
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

    public String getCalculationVersion() {
        return calculationVersion;
    }

    public void setCalculationVersion(String calculationVersion) {
        this.calculationVersion = calculationVersion;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(Instant calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getDurationFormatted() {
        return durationFormatted;
    }

    public void setDurationFormatted(String durationFormatted) {
        this.durationFormatted = durationFormatted;
    }

    public Double getAverageSpeedKmh() {
        return averageSpeedKmh;
    }

    public void setAverageSpeedKmh(Double averageSpeedKmh) {
        this.averageSpeedKmh = averageSpeedKmh;
    }

    public Double getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    public void setMaxSpeedKmh(Double maxSpeedKmh) {
        this.maxSpeedKmh = maxSpeedKmh;
    }

    public Integer getHarshBrakingCount() {
        return harshBrakingCount;
    }

    public void setHarshBrakingCount(Integer harshBrakingCount) {
        this.harshBrakingCount = harshBrakingCount;
    }

    public Integer getRapidAccelerationCount() {
        return rapidAccelerationCount;
    }

    public void setRapidAccelerationCount(Integer rapidAccelerationCount) {
        this.rapidAccelerationCount = rapidAccelerationCount;
    }

    public Integer getSpeedingObservationsCount() {
        return speedingObservationsCount;
    }

    public void setSpeedingObservationsCount(Integer speedingObservationsCount) {
        this.speedingObservationsCount = speedingObservationsCount;
    }

    public Double getHarshBrakeRatePer100Km() {
        return harshBrakeRatePer100Km;
    }

    public void setHarshBrakeRatePer100Km(Double harshBrakeRatePer100Km) {
        this.harshBrakeRatePer100Km = harshBrakeRatePer100Km;
    }

    public Double getRapidAccelRatePer100Km() {
        return rapidAccelRatePer100Km;
    }

    public void setRapidAccelRatePer100Km(Double rapidAccelRatePer100Km) {
        this.rapidAccelRatePer100Km = rapidAccelRatePer100Km;
    }

    public Double getSpeedingPct() {
        return speedingPct;
    }

    public void setSpeedingPct(Double speedingPct) {
        this.speedingPct = speedingPct;
    }

    public Long getNightDrivingMinutes() {
        return nightDrivingMinutes;
    }

    public void setNightDrivingMinutes(Long nightDrivingMinutes) {
        this.nightDrivingMinutes = nightDrivingMinutes;
    }

    public Double getNightDrivingRatio() {
        return nightDrivingRatio;
    }

    public void setNightDrivingRatio(Double nightDrivingRatio) {
        this.nightDrivingRatio = nightDrivingRatio;
    }

    public Integer getValidRecordCount() {
        return validRecordCount;
    }

    public void setValidRecordCount(Integer validRecordCount) {
        this.validRecordCount = validRecordCount;
    }

    public Integer getAnomalousRecordCount() {
        return anomalousRecordCount;
    }

    public void setAnomalousRecordCount(Integer anomalousRecordCount) {
        this.anomalousRecordCount = anomalousRecordCount;
    }

    public Double getDataQualityScore() {
        return dataQualityScore;
    }

    public void setDataQualityScore(Double dataQualityScore) {
        this.dataQualityScore = dataQualityScore;
    }

    public String getDataQualityStatus() {
        return dataQualityStatus;
    }

    public void setDataQualityStatus(String dataQualityStatus) {
        this.dataQualityStatus = dataQualityStatus;
    }
}
