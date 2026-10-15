package com.ubi.platform.controller;

import com.ubi.platform.dto.*;
import com.ubi.platform.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing Vehicle Onboarding & Mock Verification APIs.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * Registers a new vehicle into the platform.
     * Returns HTTP 201 Created on successful onboarding.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> registerVehicle(
            @Valid @RequestBody VehicleRegistrationRequest request) {
        VehicleResponse response = vehicleService.registerVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Retrieves an onboarded vehicle by its unique database ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Long id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Retrieves a paginated list of registered vehicles.
     * Supports page, size, and sorting parameters.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        PageResponse<VehicleResponse> response = vehicleService.getAllVehicles(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Runs simulated vehicle verification against mock rules or sample records.
     * Can verify either an existing vehicle ID or a pre-registration vehicle payload.
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<VehicleVerificationResponse>> verifyVehicle(
            @RequestBody VehicleVerificationRequest request) {
        VehicleVerificationResponse response = vehicleService.verifyVehicle(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
