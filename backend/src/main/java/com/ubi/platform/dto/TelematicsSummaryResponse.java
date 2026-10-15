package com.ubi.platform.dto;

import java.time.Instant;

/**
 * Outbound DTO providing statistical aggregates and risk indicator counts for a vehicle's telematics fleet.
 */
public class TelematicsSummaryResponse {

    private Long vehicleId;
    private String vehicleNumber;
    private long totalPoints;
    private long validPoints;
    private long anomalousPoints;
    private Double maxSpeedKmh;
    private Double averageSpeedKmh;
    private Double maxDecelerationG; // Harsh braking peak (negative value)
    private Double maxAccelerationG; // Rapid acceleration peak (positive value)
    private long harshBrakingEvents;  // Deceleration <= -0.35G
    private long rapidAccelerationEvents; // Acceleration >= +0.30G
    private long speedingEvents;      // Speed >= 120.0 km/h
    private Instant earliestTimestamp;
    private Instant latestTimestamp;

    public TelematicsSummaryResponse() {
    }

    // Getters and Setters

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

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public long getValidPoints() {
        return validPoints;
    }

    public void setValidPoints(long validPoints) {
        this.validPoints = validPoints;
    }

    public long getAnomalousPoints() {
        return anomalousPoints;
    }

    public void setAnomalousPoints(long anomalousPoints) {
        this.anomalousPoints = anomalousPoints;
    }

    public Double getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    public void setMaxSpeedKmh(Double maxSpeedKmh) {
        this.maxSpeedKmh = maxSpeedKmh;
    }

    public Double getAverageSpeedKmh() {
        return averageSpeedKmh;
    }

    public void setAverageSpeedKmh(Double averageSpeedKmh) {
        this.averageSpeedKmh = averageSpeedKmh;
    }

    public Double getMaxDecelerationG() {
        return maxDecelerationG;
    }

    public void setMaxDecelerationG(Double maxDecelerationG) {
        this.maxDecelerationG = maxDecelerationG;
    }

    public Double getMaxAccelerationG() {
        return maxAccelerationG;
    }

    public void setMaxAccelerationG(Double maxAccelerationG) {
        this.maxAccelerationG = maxAccelerationG;
    }

    public long getHarshBrakingEvents() {
        return harshBrakingEvents;
    }

    public void setHarshBrakingEvents(long harshBrakingEvents) {
        this.harshBrakingEvents = harshBrakingEvents;
    }

    public long getRapidAccelerationEvents() {
        return rapidAccelerationEvents;
    }

    public void setRapidAccelerationEvents(long rapidAccelerationEvents) {
        this.rapidAccelerationEvents = rapidAccelerationEvents;
    }

    public long getSpeedingEvents() {
        return speedingEvents;
    }

    public void setSpeedingEvents(long speedingEvents) {
        this.speedingEvents = speedingEvents;
    }

    public Instant getEarliestTimestamp() {
        return earliestTimestamp;
    }

    public void setEarliestTimestamp(Instant earliestTimestamp) {
        this.earliestTimestamp = earliestTimestamp;
    }

    public Instant getLatestTimestamp() {
        return latestTimestamp;
    }

    public void setLatestTimestamp(Instant latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }
}
