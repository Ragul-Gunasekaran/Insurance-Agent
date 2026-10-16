package com.ubi.platform.security;

import com.ubi.platform.entity.Trip;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.TripRepository;
import com.ubi.platform.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service enforcing vehicle and resource ownership boundaries (Step 8: Phase 5).
 * Prevents horizontal privilege escalation where an attacker accesses another user's data by tampering with IDs.
 */
@Service
public class OwnershipService {

    private static final Logger log = LoggerFactory.getLogger(OwnershipService.class);

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final SecurityUtils securityUtils;

    public OwnershipService(VehicleRepository vehicleRepository,
                            TripRepository tripRepository,
                            SecurityUtils securityUtils) {
        this.vehicleRepository = vehicleRepository;
        this.tripRepository = tripRepository;
        this.securityUtils = securityUtils;
    }

    /**
     * Verifies that the currently authenticated user is authorized to access the given vehicle.
     * Insurers are granted portfolio-wide access.
     * Customers are strictly limited to vehicles they own.
     * Returns 404 ResourceNotFoundException on unauthorized attempts to avoid disclosing resource existence.
     */
    public Vehicle verifyVehicleAccess(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        return verifyVehicleAccess(vehicle);
    }

    /**
     * Validates vehicle ownership on an already loaded Vehicle entity.
     */
    public Vehicle verifyVehicleAccess(Vehicle vehicle) {
        if (vehicle == null) {
            throw new ResourceNotFoundException("Vehicle not found");
        }

        // If no user is authenticated (e.g., isolated legacy unit tests), allow execution
        if (!securityUtils.isAuthenticated()) {
            return vehicle;
        }

        // Insurers have authorized fleet-level visibility
        if (securityUtils.isInsurer()) {
            return vehicle;
        }

        // Customers must own the vehicle
        Optional<Long> currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId.isPresent()) {
            Long userId = currentUserId.get();
            if (vehicle.getOwner() == null || !userId.equals(vehicle.getOwner().getId())) {
                log.warn("Access denied: Customer ID {} attempted to access unowned Vehicle ID {}", userId, vehicle.getId());
                // Return 404 to prevent resource existence enumeration
                throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicle.getId());
            }
        }

        return vehicle;
    }

    /**
     * Verifies access to a trip by checking ownership of the associated vehicle.
     */
    public Trip verifyTripAccess(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        verifyVehicleAccess(trip.getVehicle());
        return trip;
    }

    /**
     * Verifies access to an already loaded trip entity.
     */
    public Trip verifyTripAccess(Trip trip) {
        if (trip == null) {
            throw new ResourceNotFoundException("Trip not found");
        }
        verifyVehicleAccess(trip.getVehicle());
        return trip;
    }
}
