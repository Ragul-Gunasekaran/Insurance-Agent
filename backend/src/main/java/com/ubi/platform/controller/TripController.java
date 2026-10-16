package com.ubi.platform.controller;

import com.ubi.platform.dto.ApiResponse;
import com.ubi.platform.dto.PageResponse;
import com.ubi.platform.dto.TripResponseDto;
import com.ubi.platform.service.TripStitchingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing Trip Stitching & Lifecycle Management endpoints (Step 4: Phase B & E).
 */
@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripStitchingService tripStitchingService;

    public TripController(TripStitchingService tripStitchingService) {
        this.tripStitchingService = tripStitchingService;
    }

    /**
     * Stitches available telemetry for the selected vehicle and creates/updates its journeys.
     */
    @PostMapping("/stitch/{vehicleId}")
    public ResponseEntity<ApiResponse<List<TripResponseDto>>> stitchTrips(@PathVariable Long vehicleId) {
        List<TripResponseDto> trips = tripStitchingService.stitchTripsForVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(trips));
    }

    /**
     * Retrieves a paginated list of trips for a vehicle.
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<ApiResponse<PageResponse<TripResponseDto>>> getTripsForVehicle(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<TripResponseDto> tripPage = tripStitchingService.getTripsForVehicle(vehicleId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.fromPage(tripPage, tripPage.getContent())));
    }

    /**
     * Retrieves trip details by its unique database ID.
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<ApiResponse<TripResponseDto>> getTripById(@PathVariable Long tripId) {
        TripResponseDto trip = tripStitchingService.getTripById(tripId);
        return ResponseEntity.ok(ApiResponse.ok(trip));
    }
}
