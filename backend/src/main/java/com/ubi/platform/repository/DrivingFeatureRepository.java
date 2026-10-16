package com.ubi.platform.repository;

import com.ubi.platform.entity.DrivingFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for DrivingFeature entities.
 */
@Repository
public interface DrivingFeatureRepository extends JpaRepository<DrivingFeature, Long> {

    /**
     * Finds calculated features associated with a specific trip.
     */
    Optional<DrivingFeature> findByTripId(Long tripId);

    /**
     * Finds all calculated features for a vehicle across trips.
     */
    List<DrivingFeature> findByVehicleIdOrderByCalculatedAtDesc(Long vehicleId);

    /**
     * Check if features have already been calculated for a trip.
     */
    boolean existsByTripId(Long tripId);

    /**
     * Deletes features for a trip (for safe recalculation).
     */
    @Modifying
    @Query("DELETE FROM DrivingFeature f WHERE f.trip.id = :tripId")
    void deleteByTripId(@Param("tripId") Long tripId);

    /**
     * Deletes all features for a vehicle.
     */
    @Modifying
    @Query("DELETE FROM DrivingFeature f WHERE f.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Aggregated sums of driving features across the entire fleet.
     * Returns: [sumHarshBraking, sumRapidAccel, sumSpeeding, sumNightMinutes, sumValidRecords, sumAnomalousRecords, sumDistanceKm, sumDurationSec, countFeatures]
     */
    @Query("SELECT " +
           "COALESCE(SUM(f.harshBrakingCount), 0), " +
           "COALESCE(SUM(f.rapidAccelerationCount), 0), " +
           "COALESCE(SUM(f.speedingObservationsCount), 0), " +
           "COALESCE(SUM(f.nightDrivingMinutes), 0), " +
           "COALESCE(SUM(f.validRecordCount), 0), " +
           "COALESCE(SUM(f.anomalousRecordCount), 0), " +
           "COALESCE(SUM(f.distanceKm), 0.0), " +
           "COALESCE(SUM(f.durationSeconds), 0), " +
           "COUNT(f) " +
           "FROM DrivingFeature f")
    List<Object[]> findFleetFeatureSums();

    /**
     * Aggregated sums of driving features for a specific vehicle.
     */
    @Query("SELECT " +
           "COALESCE(SUM(f.harshBrakingCount), 0), " +
           "COALESCE(SUM(f.rapidAccelerationCount), 0), " +
           "COALESCE(SUM(f.speedingObservationsCount), 0), " +
           "COALESCE(SUM(f.nightDrivingMinutes), 0), " +
           "COALESCE(SUM(f.validRecordCount), 0), " +
           "COALESCE(SUM(f.anomalousRecordCount), 0), " +
           "COALESCE(SUM(f.distanceKm), 0.0), " +
           "COALESCE(SUM(f.durationSeconds), 0), " +
           "COUNT(f) " +
           "FROM DrivingFeature f WHERE f.vehicle.id = :vehicleId")
    List<Object[]> findVehicleFeatureSums(@Param("vehicleId") Long vehicleId);
}
