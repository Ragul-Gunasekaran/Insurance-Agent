package com.ubi.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity representing a stitched continuous driving journey for an insured vehicle (Step 4).
 */
@Entity
@Table(
    name = "trips",
    indexes = {
        @Index(name = "idx_trips_vehicle_start", columnList = "vehicle_id, start_time"),
        @Index(name = "idx_trips_status", columnList = "status")
    }
)
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;

    @Column(name = "end_latitude")
    private Double endLatitude;

    @Column(name = "end_longitude")
    private Double endLongitude;

    @Column(name = "duration_seconds")
    private Long durationSeconds = 0L;

    @Column(name = "distance_meters")
    private Double distanceMeters = 0.0;

    @Column(name = "distance_km")
    private Double distanceKm = 0.0;

    @Column(name = "valid_record_count")
    private Integer validRecordCount = 0;

    @Column(name = "anomalous_record_count")
    private Integer anomalousRecordCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TripStatus status = TripStatus.IN_PROGRESS;

    @Column(name = "features_calculated")
    private Boolean featuresCalculated = false;

    @Column(name = "score_calculated")
    private Boolean scoreCalculated = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Trip() {
    }

    public Trip(Vehicle vehicle, Instant startTime, Instant endTime,
                Double startLatitude, Double startLongitude,
                Double endLatitude, Double endLongitude,
                Long durationSeconds, Double distanceMeters,
                Integer validRecordCount, Integer anomalousRecordCount,
                TripStatus status) {
        this.vehicle = vehicle;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.durationSeconds = durationSeconds != null ? durationSeconds : 0L;
        this.distanceMeters = distanceMeters != null ? distanceMeters : 0.0;
        this.distanceKm = this.distanceMeters != null ? Math.round((this.distanceMeters / 1000.0) * 100.0) / 100.0 : 0.0;
        this.validRecordCount = validRecordCount != null ? validRecordCount : 0;
        this.anomalousRecordCount = anomalousRecordCount != null ? anomalousRecordCount : 0;
        this.status = status != null ? status : TripStatus.IN_PROGRESS;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
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

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
        this.distanceKm = distanceMeters != null ? Math.round((distanceMeters / 1000.0) * 100.0) / 100.0 : 0.0;
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

    public Boolean getScoreCalculated() {
        return scoreCalculated;
    }

    public void setScoreCalculated(Boolean scoreCalculated) {
        this.scoreCalculated = scoreCalculated;
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
