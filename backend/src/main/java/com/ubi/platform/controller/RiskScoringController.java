package com.ubi.platform.controller;

import com.ubi.platform.dto.ApiResponse;
import com.ubi.platform.dto.PageResponse;
import com.ubi.platform.dto.RiskScoreResponseDto;
import com.ubi.platform.service.RiskScoringService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing Behavioral Risk Safety Scoring endpoints (Step 5).
 * Strictly decoupled from insurance premium calculation, rating factors, and actuarial pricing.
 */
@RestController
@RequestMapping("/api/v1/risk-scores")
public class RiskScoringController {

    private final RiskScoringService riskScoringService;

    public RiskScoringController(RiskScoringService riskScoringService) {
        this.riskScoringService = riskScoringService;
    }

    /**
     * Calculates and persists a safety risk score for a trip with sufficient feature data.
     */
    @PostMapping("/trips/{tripId}/calculate")
    public ResponseEntity<ApiResponse<RiskScoreResponseDto>> calculateScoreForTrip(@PathVariable Long tripId) {
        RiskScoreResponseDto response = riskScoringService.calculateScoreForTrip(tripId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Retrieves the most recent score and transparent factor explanations for a trip.
     */
    @GetMapping("/trips/{tripId}")
    public ResponseEntity<ApiResponse<RiskScoreResponseDto>> getScoreForTrip(@PathVariable Long tripId) {
        RiskScoreResponseDto response = riskScoringService.getScoreForTrip(tripId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Calculates scores for all eligible completed trips associated with a vehicle.
     */
    @PostMapping("/vehicles/{vehicleId}/calculate")
    public ResponseEntity<ApiResponse<List<RiskScoreResponseDto>>> calculateScoresForVehicle(@PathVariable Long vehicleId) {
        List<RiskScoreResponseDto> response = riskScoringService.calculateScoresForVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Returns a paginated scoring history for a vehicle.
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<ApiResponse<PageResponse<RiskScoreResponseDto>>> getScoresForVehicle(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "calculatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<RiskScoreResponseDto> scorePage = riskScoringService.getScoresForVehicle(vehicleId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.fromPage(scorePage, scorePage.getContent())));
    }
}
