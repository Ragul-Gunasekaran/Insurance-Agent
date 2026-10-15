package com.ubi.platform.controller;

import com.ubi.platform.dto.*;
import com.ubi.platform.service.TelemetryIngestionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing IoT Telematics Ingestion, Simulation, and Fleet Diagnostics APIs (Step 3).
 */
@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final TelemetryIngestionService telemetryService;

    public TelemetryController(TelemetryIngestionService telemetryService) {
        this.telemetryService = telemetryService;
    }

    /**
     * Ingests a batch of raw IoT telematics data points from edge trackers or mobile SDKs.
     */
    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<TelematicsBatchIngestResponse>> ingestBatch(
            @Valid @RequestBody TelematicsBatchIngestRequest request) {
        TelematicsBatchIngestResponse response = telemetryService.ingestBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Ingests a single real-time telematics point.
     */
    @PostMapping("/point")
    public ResponseEntity<ApiResponse<TelematicsPointResponse>> ingestSinglePoint(
            @Valid @RequestBody TelematicsPointRequest request) {
        TelematicsPointResponse response = telemetryService.ingestSinglePoint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Generates a realistic synthetic driving route matching a requested behavioral profile.
     * Profiles: SAFE_COMMUTE, AGGRESSIVE_DRIVING, SPEEDING_HIGHWAY, ANOMALOUS_SENSOR.
     */
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<TelematicsBatchIngestResponse>> simulateTelemetry(
            @Valid @RequestBody SimulateTelemetryRequest request) {
        TelematicsBatchIngestResponse response = telemetryService.simulateAndIngest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * Retrieves paginated telematics data points for a specific vehicle.
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<ApiResponse<PageResponse<TelematicsPointResponse>>> getVehicleTelemetry(
            @PathVariable Long vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        PageResponse<TelematicsPointResponse> response = telemetryService.getVehicleTelemetry(vehicleId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Returns statistical telemetry aggregates and risk indicators (harsh braking, rapid accel, top speed).
     */
    @GetMapping("/vehicles/{vehicleId}/summary")
    public ResponseEntity<ApiResponse<TelematicsSummaryResponse>> getVehicleTelemetrySummary(
            @PathVariable Long vehicleId) {
        TelematicsSummaryResponse summary = telemetryService.getVehicleTelemetrySummary(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    /**
     * Clears all telematics data points for a vehicle (resets simulation runs).
     */
    @DeleteMapping("/vehicles/{vehicleId}")
    public ResponseEntity<ApiResponse<String>> clearVehicleTelemetry(@PathVariable Long vehicleId) {
        telemetryService.clearVehicleTelemetry(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok("Telemetry records cleared successfully for vehicle ID: " + vehicleId));
    }
}
