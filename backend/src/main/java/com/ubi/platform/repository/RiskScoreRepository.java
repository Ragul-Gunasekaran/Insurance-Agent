package com.ubi.platform.repository;

import com.ubi.platform.entity.RiskScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for RiskScore entities.
 */
@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {

    /**
     * Finds the risk score associated with a specific trip.
     */
    Optional<RiskScore> findByTripId(Long tripId);

    /**
     * Finds paginated risk scores for a vehicle.
     */
    Page<RiskScore> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Finds all risk scores for a vehicle ordered by calculation timestamp.
     */
    List<RiskScore> findByVehicleIdOrderByCalculatedAtDesc(Long vehicleId);

    /**
     * Checks if a score exists for a trip.
     */
    boolean existsByTripId(Long tripId);

    /**
     * Deletes risk score for a trip.
     */
    @Modifying
    @Query("DELETE FROM RiskScore r WHERE r.trip.id = :tripId")
    void deleteByTripId(@Param("tripId") Long tripId);

    /**
     * Deletes all risk scores for a vehicle.
     */
    @Modifying
    @Query("DELETE FROM RiskScore r WHERE r.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Count scorable trips.
     */
    long countByIsScorableTrue();

    /**
     * Count unscorable or unrated trips.
     */
    long countByIsScorableFalse();

    /**
     * Count trips by category.
     */
    long countByScoreCategory(String scoreCategory);

    /**
     * Group count by score category.
     */
    @Query("SELECT r.scoreCategory, COUNT(r) FROM RiskScore r WHERE r.isScorable = true GROUP BY r.scoreCategory")
    List<Object[]> countGroupByScoreCategory();

    /**
     * Fleet average safety score across eligible scores.
     */
    @Query("SELECT AVG(r.score) FROM RiskScore r WHERE r.isScorable = true AND r.score IS NOT NULL")
    Double findAverageSafetyScore();
}
