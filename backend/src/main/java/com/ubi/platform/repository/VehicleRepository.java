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
}
