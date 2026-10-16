package com.ubi.platform.dto;

import com.ubi.platform.entity.Trip;
import com.ubi.platform.entity.TripStatus;
import java.time.Instant;

/**
 * Outbound DTO representing a vehicle driving trip session.
 */
public class TripResponseDto {

    private Long id;
    private Long vehicleId;
    private String vehicleNumber;
    private Instant startTime;
    private Instant endTime;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private Long durationSeconds;
    private String durationFormatted;
    private Double distanceMeters;
    private Double distanceKm;
    private Integer validRecordCount;
    private Integer anomalousRecordCount;
    private TripStatus status;
    private Boolean featuresCalculated;
    private Instant createdAt;
    private Instant updatedAt;

    public TripResponseDto() {
    }

    public static TripResponseDto fromEntity(Trip trip) {
        if (trip == null) {
            return null;
        }
        TripResponseDto dto = new TripResponseDto();
        dto.setId(trip.getId());
        dto.setVehicleId(trip.getVehicle() != null ? trip.getVehicle().getId() : null);
        dto.setVehicleNumber(trip.getVehicle() != null ? trip.getVehicle().getVehicleNumber() : null);
        dto.setStartTime(trip.getStartTime());
        dto.setEndTime(trip.getEndTime());
        dto.setStartLatitude(trip.getStartLatitude());
        dto.setStartLongitude(trip.getStartLongitude());
        dto.setEndLatitude(trip.getEndLatitude());
        dto.setEndLongitude(trip.getEndLongitude());
        dto.setDurationSeconds(trip.getDurationSeconds());
        dto.setDurationFormatted(formatDuration(trip.getDurationSeconds()));
        dto.setDistanceMeters(trip.getDistanceMeters());
        dto.setDistanceKm(trip.getDistanceKm());
        dto.setValidRecordCount(trip.getValidRecordCount());
        dto.setAnomalousRecordCount(trip.getAnomalousRecordCount());
        dto.setStatus(trip.getStatus());
        dto.setFeaturesCalculated(trip.getFeaturesCalculated());
        dto.setCreatedAt(trip.getCreatedAt());
        dto.setUpdatedAt(trip.getUpdatedAt());
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

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
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

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
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

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public Boolean getFeaturesCalculated() {
        return featuresCalculated;
    }

    public void setFeaturesCalculated(Boolean featuresCalculated) {
        this.featuresCalculated = featuresCalculated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
