package com.ubi.platform.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Inbound DTO requesting simulated telemetry generation for demo/testing.
 */
public class SimulateTelemetryRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    /**
     * Simulation profile:
     * - SAFE_COMMUTE (smooth driving, no harsh events)
     * - AGGRESSIVE_DRIVING (multiple harsh brakes, high acceleration)
     * - SPEEDING_HIGHWAY (high sustained velocity)
     * - ANOMALOUS_SENSOR (contains sensor spikes / calibration anomalies)
     */
    private String profile = "SAFE_COMMUTE";

    /**
     * Number of telemetry points to generate (default 20, max 100).
     */
    private Integer pointCount = 20;

    public SimulateTelemetryRequest() {
    }

    public SimulateTelemetryRequest(Long vehicleId, String profile, Integer pointCount) {
        this.vehicleId = vehicleId;
        this.profile = profile != null ? profile : "SAFE_COMMUTE";
        this.pointCount = pointCount != null ? pointCount : 20;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Integer getPointCount() {
        return pointCount;
    }

    public void setPointCount(Integer pointCount) {
        this.pointCount = pointCount;
    }
}
