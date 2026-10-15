package com.ubi.platform.dto;

import com.ubi.platform.entity.TelematicsRecord;
import com.ubi.platform.entity.TelematicsValidationStatus;
import java.time.Instant;

/**
 * Outbound DTO representing a persisted and validated telematics sample.
 */
public class TelematicsPointResponse {

    private Long id;
    private Long vehicleId;
    private Instant timestamp;
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double accelerationG;
    private Double heading;
    private Integer engineRpm;
    private Double fuelLevelPercent;
    private TelematicsValidationStatus validationStatus;
    private String validationNotes;
    private Instant createdAt;

    public TelematicsPointResponse() {
    }

    public static TelematicsPointResponse fromEntity(TelematicsRecord record) {
        if (record == null) {
            return null;
        }
        TelematicsPointResponse dto = new TelematicsPointResponse();
        dto.setId(record.getId());
        dto.setVehicleId(record.getVehicle() != null ? record.getVehicle().getId() : null);
        dto.setTimestamp(record.getTimestamp());
        dto.setLatitude(record.getLatitude());
        dto.setLongitude(record.getLongitude());
        dto.setSpeedKmh(record.getSpeedKmh());
        dto.setAccelerationG(record.getAccelerationG());
        dto.setHeading(record.getHeading());
        dto.setEngineRpm(record.getEngineRpm());
        dto.setFuelLevelPercent(record.getFuelLevelPercent());
        dto.setValidationStatus(record.getValidationStatus());
        dto.setValidationNotes(record.getValidationNotes());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getSpeedKmh() {
        return speedKmh;
    }

    public void setSpeedKmh(Double speedKmh) {
        this.speedKmh = speedKmh;
    }

    public Double getAccelerationG() {
        return accelerationG;
    }

    public void setAccelerationG(Double accelerationG) {
        this.accelerationG = accelerationG;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Integer getEngineRpm() {
        return engineRpm;
    }

    public void setEngineRpm(Integer engineRpm) {
        this.engineRpm = engineRpm;
    }

    public Double getFuelLevelPercent() {
        return fuelLevelPercent;
    }

    public void setFuelLevelPercent(Double fuelLevelPercent) {
        this.fuelLevelPercent = fuelLevelPercent;
    }

    public TelematicsValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(TelematicsValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getValidationNotes() {
        return validationNotes;
    }

    public void setValidationNotes(String validationNotes) {
        this.validationNotes = validationNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
