package com.ubi.platform.service;

import com.ubi.platform.dto.*;
import com.ubi.platform.entity.TelematicsRecord;
import com.ubi.platform.entity.TelematicsValidationStatus;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.TelematicsRepository;
import com.ubi.platform.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing telematics ingestion, calibration validation,
 * persistence, time-series retrieval, and fleet risk summaries (Step 3).
 */
@Service
public class TelemetryIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryIngestionService.class);

    private static final double HARSH_BRAKING_THRESHOLD_G = -0.35;
    private static final double RAPID_ACCEL_THRESHOLD_G = 0.30;
    private static final double SPEEDING_THRESHOLD_KMH = 120.0;

    private final TelematicsRepository telematicsRepository;
    private final VehicleRepository vehicleRepository;
    private final TelemetryValidationService validationService;
    private final TelemetrySimulationService simulationService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.ubi.platform.security.OwnershipService ownershipService;

    public TelemetryIngestionService(TelematicsRepository telematicsRepository,
                                     VehicleRepository vehicleRepository,
                                     TelemetryValidationService validationService,
                                     TelemetrySimulationService simulationService) {
        this.telematicsRepository = telematicsRepository;
        this.vehicleRepository = vehicleRepository;
        this.validationService = validationService;
        this.simulationService = simulationService;
    }

    public void setOwnershipService(com.ubi.platform.security.OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    /**
     * Ingests a batch of raw IoT telematics data points for a specific vehicle.
     */
    @Transactional
    public TelematicsBatchIngestResponse ingestBatch(TelematicsBatchIngestRequest request) {
        Long vehicleId = request.getVehicleId();
        Vehicle vehicle;
        if (ownershipService != null) {
            vehicle = ownershipService.verifyVehicleAccess(vehicleId);
        } else {
            vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cannot ingest telematics for unknown vehicle ID: " + vehicleId));
        }

        List<TelematicsPointRequest> rawPoints = request.getPoints();
        List<TelematicsRecord> toSave = new ArrayList<>();
        int validCount = 0;
        int anomalyCount = 0;

        for (TelematicsPointRequest point : rawPoints) {
            TelemetryValidationService.ValidationResult val = validationService.validatePoint(point);
            if (val.isValid()) {
                validCount++;
            } else {
                anomalyCount++;
            }

            TelematicsRecord record = new TelematicsRecord(
                    vehicle,
                    point.getTimestamp(),
                    point.getLatitude(),
                    point.getLongitude(),
                    point.getSpeedKmh(),
                    point.getAccelerationG(),
                    point.getHeading(),
                    point.getEngineRpm(),
                    point.getFuelLevelPercent(),
                    val.status(),
                    val.notes()
            );
            toSave.add(record);
        }

        List<TelematicsRecord> saved = telematicsRepository.saveAll(toSave);
        log.info("Ingested {} telematics points for vehicle {} (Valid: {}, Anomalies: {})",
                saved.size(), vehicle.getVehicleNumber(), validCount, anomalyCount);

        List<TelematicsPointResponse> sampleDtos = saved.stream()
                .limit(10)
                .map(TelematicsPointResponse::fromEntity)
                .toList();

        return new TelematicsBatchIngestResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                saved.size(),
                validCount,
                anomalyCount,
                String.format("Successfully ingested %d telematics points (%d valid, %d filtered anomalies).",
                        saved.size(), validCount, anomalyCount),
                sampleDtos
        );
    }

    /**
     * Ingests a single telemetry point.
     */
    @Transactional
    public TelematicsPointResponse ingestSinglePoint(TelematicsPointRequest request) {
        if (request.getVehicleId() == null) {
            throw new IllegalArgumentException("Vehicle ID is required for telematics ingestion");
        }
        Vehicle vehicle;
        if (ownershipService != null) {
            vehicle = ownershipService.verifyVehicleAccess(request.getVehicleId());
        } else {
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + request.getVehicleId()));
        }

        TelemetryValidationService.ValidationResult val = validationService.validatePoint(request);
        TelematicsRecord record = new TelematicsRecord(
                vehicle,
                request.getTimestamp(),
                request.getLatitude(),
                request.getLongitude(),
                request.getSpeedKmh(),
                request.getAccelerationG(),
                request.getHeading(),
                request.getEngineRpm(),
                request.getFuelLevelPercent(),
                val.status(),
                val.notes()
        );

        TelematicsRecord saved = telematicsRepository.save(record);
        return TelematicsPointResponse.fromEntity(saved);
    }

    /**
     * Generates simulated driving route telemetry and ingests it immediately.
     */
    @Transactional
    public TelematicsBatchIngestResponse simulateAndIngest(SimulateTelemetryRequest request) {
        Long vehicleId = request.getVehicleId();
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }

        List<TelematicsPointRequest> syntheticPoints = simulationService.generateSimulationRoute(
                vehicleId,
                request.getProfile(),
                request.getPointCount()
        );

        TelematicsBatchIngestRequest ingestRequest = new TelematicsBatchIngestRequest(vehicleId, syntheticPoints);
        return ingestBatch(ingestRequest);
    }

    /**
     * Retrieves paginated telematics records for a vehicle.
     */
    @Transactional(readOnly = true)
    public PageResponse<TelematicsPointResponse> getVehicleTelemetry(Long vehicleId, Pageable pageable) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        } else if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }

        Page<TelematicsRecord> page = telematicsRepository.findByVehicleIdOrderByTimestampDesc(vehicleId, pageable);
        List<TelematicsPointResponse> dtos = page.getContent().stream()
                .map(TelematicsPointResponse::fromEntity)
                .toList();

        return PageResponse.fromPage(page, dtos);
    }

    /**
     * Aggregates telemetry summary and driving risk indicators for a vehicle.
     */
    @Transactional(readOnly = true)
    public TelematicsSummaryResponse getVehicleTelemetrySummary(Long vehicleId) {
        Vehicle vehicle;
        if (ownershipService != null) {
            vehicle = ownershipService.verifyVehicleAccess(vehicleId);
        } else {
            vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));
        }

        long totalCount = telematicsRepository.countByVehicleId(vehicleId);
        long validCount = telematicsRepository.countByVehicleIdAndValidationStatus(vehicleId, TelematicsValidationStatus.VALID);
        long anomalousCount = totalCount - validCount;

        TelematicsSummaryResponse summary = new TelematicsSummaryResponse();
        summary.setVehicleId(vehicle.getId());
        summary.setVehicleNumber(vehicle.getVehicleNumber());
        summary.setTotalPoints(totalCount);
        summary.setValidPoints(validCount);
        summary.setAnomalousPoints(anomalousCount);

        if (validCount > 0) {
            Double maxSpeed = telematicsRepository.findMaxSpeedByVehicleId(vehicleId);
            Double avgSpeed = telematicsRepository.findAvgSpeedByVehicleId(vehicleId);
            Double maxDecel = telematicsRepository.findMaxBrakingGByVehicleId(vehicleId);
            Double maxAccel = telematicsRepository.findMaxAccelGByVehicleId(vehicleId);

            long harshBrakes = telematicsRepository.countHarshBrakesByVehicleId(vehicleId, HARSH_BRAKING_THRESHOLD_G);
            long rapidAccels = telematicsRepository.countRapidAccelerationsByVehicleId(vehicleId, RAPID_ACCEL_THRESHOLD_G);
            long speedingEvents = telematicsRepository.countSpeedingEventsByVehicleId(vehicleId, SPEEDING_THRESHOLD_KMH);

            summary.setMaxSpeedKmh(maxSpeed != null ? Math.round(maxSpeed * 10.0) / 10.0 : 0.0);
            summary.setAverageSpeedKmh(avgSpeed != null ? Math.round(avgSpeed * 10.0) / 10.0 : 0.0);
            summary.setMaxDecelerationG(maxDecel != null ? Math.round(maxDecel * 100.0) / 100.0 : 0.0);
            summary.setMaxAccelerationG(maxAccel != null ? Math.round(maxAccel * 100.0) / 100.0 : 0.0);
            summary.setHarshBrakingEvents(harshBrakes);
            summary.setRapidAccelerationEvents(rapidAccels);
            summary.setSpeedingEvents(speedingEvents);
        }

        return summary;
    }

    /**
     * Resets/clears all telematics records for a vehicle.
     */
    @Transactional
    public void clearVehicleTelemetry(Long vehicleId) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        } else if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        telematicsRepository.deleteByVehicleId(vehicleId);
        log.info("Cleared all telemetry records for vehicle ID: {}", vehicleId);
    }
}
