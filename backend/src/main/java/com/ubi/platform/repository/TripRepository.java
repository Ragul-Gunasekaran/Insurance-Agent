package com.ubi.platform.repository;

import com.ubi.platform.entity.Trip;
import com.ubi.platform.entity.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Trip entities.
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Paginated retrieval of trips for a vehicle sorted chronologically descending.
     */
    Page<Trip> findByVehicleIdOrderByStartTimeDesc(Long vehicleId, Pageable pageable);

    /**
     * Retrieve all trips for a vehicle sorted ascending.
     */
    List<Trip> findByVehicleIdOrderByStartTimeAsc(Long vehicleId);

    /**
     * Find latest trip for a vehicle.
     */
    Optional<Trip> findTopByVehicleIdOrderByStartTimeDesc(Long vehicleId);

    /**
     * Find trips by status.
     */
    List<Trip> findByVehicleIdAndStatus(Long vehicleId, TripStatus status);

    /**
     * Count trips for a vehicle.
     */
    long countByVehicleId(Long vehicleId);

    /**
     * Check if a trip already exists covering an exact start time for a vehicle.
     */
    Optional<Trip> findByVehicleIdAndStartTime(Long vehicleId, Instant startTime);

    /**
     * Delete all trips for a vehicle (for clean recalculation/resets).
     */
    @Modifying
    @Query("DELETE FROM Trip t WHERE t.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Count trips across entire fleet by status.
     */
    long countByStatus(TripStatus status);

    /**
     * Count completed trips for a vehicle.
     */
    long countByVehicleIdAndStatus(Long vehicleId, TripStatus status);

    /**
     * Total observed distance across entire fleet by status.
     */
    @Query("SELECT COALESCE(SUM(t.distanceKm), 0.0) FROM Trip t WHERE t.status = :status")
    Double sumDistanceByStatus(@Param("status") TripStatus status);

    /**
     * Total observed distance for a specific vehicle by status.
     */
    @Query("SELECT COALESCE(SUM(t.distanceKm), 0.0) FROM Trip t WHERE t.vehicle.id = :vehicleId AND t.status = :status")
    Double sumDistanceByVehicleIdAndStatus(@Param("vehicleId") Long vehicleId, @Param("status") TripStatus status);

    /**
     * Unique vehicle IDs with recorded trips.
     */
    @Query("SELECT DISTINCT t.vehicle.id FROM Trip t")
    List<Long> findDistinctVehicleIdsWithTrips();
}
