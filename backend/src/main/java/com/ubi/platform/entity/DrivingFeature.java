package com.ubi.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity representing calculated behavioral driving features for a completed trip (Step 4: Phase D).
 * Keeps raw features decoupled from actuarial pricing and underwriting rules.
 */
@Entity
@Table(
    name = "driving_features",
    indexes = {
        @Index(name = "idx_features_trip", columnList = "trip_id", unique = true),
        @Index(name = "idx_features_vehicle", columnList = "vehicle_id")
    }
)
public class DrivingFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false, unique = true)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "calculation_version", nullable = false, length = 32)
    private String calculationVersion = "v1.0";

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Column(name = "duration_seconds", nullable = false)
    private Long durationSeconds;

    @Column(name = "average_speed_kmh", nullable = false)
    private Double averageSpeedKmh;

    @Column(name = "max_speed_kmh", nullable = false)
    private Double maxSpeedKmh;

    @Column(name = "harsh_braking_count", nullable = false)
    private Integer harshBrakingCount;

    @Column(name = "rapid_acceleration_count", nullable = false)
    private Integer rapidAccelerationCount;

    @Column(name = "speeding_observations_count", nullable = false)
    private Integer speedingObservationsCount;

    @Column(name = "harsh_brake_rate_per_100km")
    private Double harshBrakeRatePer100Km;

    @Column(name = "rapid_accel_rate_per_100km")
    private Double rapidAccelRatePer100Km;

    @Column(name = "speeding_pct")
    private Double speedingPct;

    @Column(name = "night_driving_minutes")
    private Long nightDrivingMinutes = 0L;

    @Column(name = "night_driving_ratio")
    private Double nightDrivingRatio = 0.0;

    @Column(name = "valid_record_count", nullable = false)
    private Integer validRecordCount;

    @Column(name = "anomalous_record_count", nullable = false)
    private Integer anomalousRecordCount;

    @Column(name = "data_quality_score")
    private Double dataQualityScore;

    @Column(name = "data_quality_status", length = 32)
    private String dataQualityStatus;

    public DrivingFeature() {
    }

    public DrivingFeature(Trip trip, Vehicle vehicle, String calculationVersion,
                          Double distanceKm, Long durationSeconds,
                          Double averageSpeedKmh, Double maxSpeedKmh,
                          Integer harshBrakingCount, Integer rapidAccelerationCount,
                          Integer speedingObservationsCount, Double harshBrakeRatePer100Km,
                          Double rapidAccelRatePer100Km, Double speedingPct,
                          Long nightDrivingMinutes, Double nightDrivingRatio,
                          Integer validRecordCount, Integer anomalousRecordCount,
                          Double dataQualityScore, String dataQualityStatus) {
        this.trip = trip;
        this.vehicle = vehicle;
        this.calculationVersion = calculationVersion != null ? calculationVersion : "v1.0";
        this.calculatedAt = Instant.now();
        this.distanceKm = distanceKm;
        this.durationSeconds = durationSeconds;
        this.averageSpeedKmh = averageSpeedKmh;
        this.maxSpeedKmh = maxSpeedKmh;
        this.harshBrakingCount = harshBrakingCount;
        this.rapidAccelerationCount = rapidAccelerationCount;
        this.speedingObservationsCount = speedingObservationsCount;
        this.harshBrakeRatePer100Km = harshBrakeRatePer100Km;
        this.rapidAccelRatePer100Km = rapidAccelRatePer100Km;
        this.speedingPct = speedingPct;
        this.nightDrivingMinutes = nightDrivingMinutes != null ? nightDrivingMinutes : 0L;
        this.nightDrivingRatio = nightDrivingRatio != null ? nightDrivingRatio : 0.0;
        this.validRecordCount = validRecordCount;
        this.anomalousRecordCount = anomalousRecordCount;
        this.dataQualityScore = dataQualityScore;
        this.dataQualityStatus = dataQualityStatus;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
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
