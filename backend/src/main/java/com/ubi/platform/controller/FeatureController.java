package com.ubi.platform.controller;

import com.ubi.platform.dto.ApiResponse;
import com.ubi.platform.dto.DrivingFeatureResponseDto;
import com.ubi.platform.service.FeatureEngineeringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing Driving Behavior Feature Engineering endpoints (Step 4: Phase D & E).
 */
@RestController
@RequestMapping("/api/v1/features")
public class FeatureController {

    private final FeatureEngineeringService featureEngineeringService;

    public FeatureController(FeatureEngineeringService featureEngineeringService) {
        this.featureEngineeringService = featureEngineeringService;
    }

    /**
     * Calculates or recalculates driving behavioral features for a specific trip.
     */
    @PostMapping("/trips/{tripId}/calculate")
    public ResponseEntity<ApiResponse<DrivingFeatureResponseDto>> calculateFeaturesForTrip(@PathVariable Long tripId) {
        DrivingFeatureResponseDto response = featureEngineeringService.calculateFeaturesForTrip(tripId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Retrieves stored feature summary for a trip.
     */
    @GetMapping("/trips/{tripId}")
    public ResponseEntity<ApiResponse<DrivingFeatureResponseDto>> getFeaturesForTrip(@PathVariable Long tripId) {
        DrivingFeatureResponseDto response = featureEngineeringService.getFeaturesForTrip(tripId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Calculates features for all trips belonging to a vehicle in batch.
     */
    @PostMapping("/vehicles/{vehicleId}/calculate")
    public ResponseEntity<ApiResponse<List<DrivingFeatureResponseDto>>> calculateFeaturesForVehicle(@PathVariable Long vehicleId) {
        List<DrivingFeatureResponseDto> response = featureEngineeringService.calculateFeaturesForVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
