package com.ubi.platform.repository;

import com.ubi.platform.entity.TelematicsRecord;
import com.ubi.platform.entity.TelematicsValidationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA Repository for time-series TelematicsRecord entities.
 */
@Repository
public interface TelematicsRepository extends JpaRepository<TelematicsRecord, Long> {

    /**
     * Retrieve paginated telematics records for a vehicle, sorted chronologically descending.
     */
    Page<TelematicsRecord> findByVehicleIdOrderByTimestampDesc(Long vehicleId, Pageable pageable);

    /**
     * Retrieve all telematics records for a vehicle within a time window, sorted ascending for trip stitching.
     */
    List<TelematicsRecord> findByVehicleIdAndTimestampBetweenOrderByTimestampAsc(Long vehicleId, Instant start, Instant end);

    /**
     * Retrieve all telematics records for a vehicle, sorted chronologically ascending.
     */
    List<TelematicsRecord> findByVehicleIdOrderByTimestampAsc(Long vehicleId);

    /**
     * Count total pings for a vehicle.
     */
    long countByVehicleId(Long vehicleId);

    /**
     * Count pings by validation status.
     */
    long countByVehicleIdAndValidationStatus(Long vehicleId, TelematicsValidationStatus validationStatus);

    /**
     * Delete all telematics records for a vehicle (useful for resetting simulated trips).
     */
    void deleteByVehicleId(Long vehicleId);

    /**
     * Fetch statistical telemetry aggregates directly via JPA query.
     */
    @Query("SELECT MAX(t.speedKmh) FROM TelematicsRecord t WHERE t.vehicle.id = :vehicleId AND t.validationStatus = 'VALID'")
    Double findMaxSpeedByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT AVG(t.speedKmh) FROM TelematicsRecord t WHERE t.vehicle.id = :vehicleId AND t.validationStatus = 'VALID'")
    Double findAvgSpeedByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT MIN(t.accelerationG) FROM TelematicsRecord t WHERE t.vehicle.id = :vehicleId AND t.validationStatus = 'VALID'")
    Double findMaxBrakingGByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT MAX(t.accelerationG) FROM TelematicsRecord t WHERE t.vehicle.id = :vehicleId AND t.validationStatus = 'VALID'")
    Double findMaxAccelGByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT COUNT(t) FROM TelematicsRecord t WHERE t.vehicle.id = :vehicleId AND t.validationStatus = 'VALID' AND t.accelerationG <= :harshBrakeG")
    long countHarshBrakesByVehicleId(@Param("vehicleId") Long vehicleId, @Param("harshBrakeG") Double harshBrakeG);

    @Query("SELECT COUNT(t) FROM TelematicsRecord t WHERE t.vehicle.id = :vehicleId AND t.validationStatus = 'VALID' AND t.accelerationG >= :rapidAccelG")
    long countRapidAccelerationsByVehicleId(@Param("vehicleId") Long vehicleId, @Param("rapidAccelG") Double rapidAccelG);

    @Query("SELECT COUNT(t) FROM TelematicsRecord t WHERE t.vehicle.id = :vehicleId AND t.validationStatus = 'VALID' AND t.speedKmh >= :speedThreshold")
    long countSpeedingEventsByVehicleId(@Param("vehicleId") Long vehicleId, @Param("speedThreshold") Double speedThreshold);
}
