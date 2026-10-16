package com.ubi.platform.repository;

import com.ubi.platform.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Vehicle entities.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Checks if a vehicle with the given normalized registration number already exists.
     */
    boolean existsByVehicleNumber(String vehicleNumber);

    /**
     * Finds a vehicle by its normalized registration number.
     */
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    /**
     * Finds paginated vehicles owned by a specific customer.
     */
    org.springframework.data.domain.Page<Vehicle> findByOwnerId(Long ownerId, org.springframework.data.domain.Pageable pageable);

    /**
     * Finds all vehicles owned by a specific customer.
     */
    java.util.List<Vehicle> findByOwnerId(Long ownerId);

    /**
     * Finds a vehicle by ID only if owned by the specific customer.
     */
    Optional<Vehicle> findByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Count vehicles owned by a specific customer.
     */
    long countByOwnerId(Long ownerId);
}
