package com.ubi.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity representing a single time-series IoT telematics ping from a vehicle.
 */
@Entity
@Table(
    name = "telematics_records",
    indexes = {
        @Index(name = "idx_telemetry_vehicle_ts", columnList = "vehicle_id, timestamp"),
        @Index(name = "idx_telemetry_status", columnList = "validation_status"),
        @Index(name = "idx_telemetry_trip", columnList = "trip_id")
    }
)
public class TelematicsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "speed_kmh", nullable = false)
    private Double speedKmh;

    @Column(name = "acceleration_g", nullable = false)
    private Double accelerationG;

    @Column(name = "heading")
    private Double heading;

    @Column(name = "engine_rpm")
    private Integer engineRpm;

    @Column(name = "fuel_level_percent")
    private Double fuelLevelPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 32)
    private TelematicsValidationStatus validationStatus = TelematicsValidationStatus.VALID;

    @Column(name = "validation_notes", length = 512)
    private String validationNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public TelematicsRecord() {
    }

    public TelematicsRecord(Vehicle vehicle, Instant timestamp, Double latitude, Double longitude,
                            Double speedKmh, Double accelerationG, Double heading,
                            Integer engineRpm, Double fuelLevelPercent,
                            TelematicsValidationStatus validationStatus, String validationNotes) {
        this.vehicle = vehicle;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.accelerationG = accelerationG;
        this.heading = heading;
        this.engineRpm = engineRpm;
        this.fuelLevelPercent = fuelLevelPercent;
        this.validationStatus = validationStatus != null ? validationStatus : TelematicsValidationStatus.VALID;
        this.validationNotes = validationNotes;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
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
