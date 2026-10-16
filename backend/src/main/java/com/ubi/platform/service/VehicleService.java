package com.ubi.platform.service;

import com.ubi.platform.dto.*;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.exception.DuplicateResourceException;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.UserRepository;
import com.ubi.platform.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing vehicle onboarding, normalization, retrieval, and verification updates.
 */
@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final VehicleVerificationService verificationService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.ubi.platform.security.SecurityUtils securityUtils;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.ubi.platform.security.OwnershipService ownershipService;

    public VehicleService(VehicleRepository vehicleRepository, VehicleVerificationService verificationService) {
        this.vehicleRepository = vehicleRepository;
        this.verificationService = verificationService;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setSecurityUtils(com.ubi.platform.security.SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    public void setOwnershipService(com.ubi.platform.security.OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    /**
     * Registers a new vehicle into the platform.
     * Normalizes the registration number, verifies uniqueness, and persists with default UNVERIFIED status.
     * Associating with the currently authenticated customer account when present.
     */
    @Transactional
    public VehicleResponse registerVehicle(VehicleRegistrationRequest request) {
        String normalizedNumber = verificationService.normalizeVehicleNumber(request.getVehicleNumber());

        log.info("Attempting to register vehicle with normalized number: {}", normalizedNumber);

        // Pre-check for duplicate registration
        if (vehicleRepository.existsByVehicleNumber(normalizedNumber)) {
            throw new DuplicateResourceException("Vehicle with registration number '" + normalizedNumber + "' is already registered.");
        }

        Vehicle vehicle = new Vehicle(
                normalizedNumber,
                request.getMake().trim(),
                request.getModel().trim(),
                request.getManufacturingYear(),
                request.getEngineCapacityCc(),
                request.getFuelType(),
                request.getVehicleType()
        );

        // Associate with current authenticated user
        if (securityUtils != null && userRepository != null) {
            securityUtils.getCurrentUserEntity(userRepository).ifPresent(vehicle::setOwner);
        }

        try {
            Vehicle saved = vehicleRepository.save(vehicle);
            log.info("Successfully registered vehicle ID: {} [{}]", saved.getId(), saved.getVehicleNumber());
            return VehicleResponse.fromEntity(saved);
        } catch (DataIntegrityViolationException ex) {
            log.error("Concurrent collision saving vehicle {}: {}", normalizedNumber, ex.getMessage());
            throw new DuplicateResourceException("Vehicle with registration number '" + normalizedNumber + "' already exists.");
        }
    }

    /**
     * Retrieves an onboarded vehicle by its primary key ID, enforcing ownership check.
     */
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + id));

        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicle);
        }

        return VehicleResponse.fromEntity(vehicle);
    }

    /**
     * Retrieves a paginated list of onboarded vehicles.
     * Customers are scoped strictly to their own registered vehicles; Insurers see fleet-wide.
     */
    @Transactional(readOnly = true)
    public PageResponse<VehicleResponse> getAllVehicles(Pageable pageable) {
        Page<Vehicle> page;
        if (securityUtils != null && securityUtils.isCustomer() && securityUtils.getCurrentUserId().isPresent()) {
            page = vehicleRepository.findByOwnerId(securityUtils.getCurrentUserId().get(), pageable);
        } else {
            page = vehicleRepository.findAll(pageable);
        }

        List<VehicleResponse> dtos = page.getContent().stream()
                .map(VehicleResponse::fromEntity)
                .toList();
        return PageResponse.fromPage(page, dtos);
    }

    /**
     * Runs mock verification for an existing registered vehicle or arbitrary vehicle details.
     * If an existing vehicle ID is provided, updates and persists the verification outcome.
     */
    @Transactional
    public VehicleVerificationResponse verifyVehicle(VehicleVerificationRequest request) {
        if (request.getVehicleId() != null) {
            Vehicle vehicle;
            if (ownershipService != null) {
                vehicle = ownershipService.verifyVehicleAccess(request.getVehicleId());
            } else {
                vehicle = vehicleRepository.findById(request.getVehicleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cannot verify nonexistent vehicle ID: " + request.getVehicleId()));
            }

            // Populate request attributes from database entity if not specified
            if (request.getVehicleNumber() == null) {
                request.setVehicleNumber(vehicle.getVehicleNumber());
            }
            if (request.getMake() == null) {
                request.setMake(vehicle.getMake());
            }
            if (request.getModel() == null) {
                request.setModel(vehicle.getModel());
            }
            if (request.getManufacturingYear() == null) {
                request.setManufacturingYear(vehicle.getManufacturingYear());
            }

            VehicleVerificationResponse response = verificationService.verify(request);

            // Persist the updated verification status onto the vehicle
            vehicle.setVerificationStatus(response.getVerificationStatus());
            vehicle.setVerificationNotes(response.getExplanation());
            vehicleRepository.save(vehicle);
            log.info("Updated verification status for vehicle ID {}: {}", vehicle.getId(), vehicle.getVerificationStatus());

            return response;
        }

        // Ad-hoc verification for pre-registration checks
        return verificationService.verify(request);
    }
}
