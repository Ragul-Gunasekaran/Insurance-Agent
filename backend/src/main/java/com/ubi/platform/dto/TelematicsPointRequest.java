package com.ubi.platform.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Inbound DTO representing a single telematics data sample from an on-vehicle IoT tracker.
 */
public class TelematicsPointRequest {

    private Long vehicleId;

    @NotNull(message = "Timestamp is required")
    private Instant timestamp;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Speed is required")
    private Double speedKmh;

    @NotNull(message = "Acceleration is required")
    private Double accelerationG;

    private Double heading;
    private Integer engineRpm;
    private Double fuelLevelPercent;

    public TelematicsPointRequest() {
    }

    public TelematicsPointRequest(Long vehicleId, Instant timestamp, Double latitude, Double longitude,
                                  Double speedKmh, Double accelerationG, Double heading,
                                  Integer engineRpm, Double fuelLevelPercent) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.accelerationG = accelerationG;
        this.heading = heading;
        this.engineRpm = engineRpm;
        this.fuelLevelPercent = fuelLevelPercent;
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
}
