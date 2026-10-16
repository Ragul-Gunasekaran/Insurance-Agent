package com.ubi.platform.service;

import com.ubi.platform.dto.TripResponseDto;
import com.ubi.platform.entity.*;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.TelematicsRepository;
import com.ubi.platform.repository.TripRepository;
import com.ubi.platform.repository.VehicleRepository;
import com.ubi.platform.security.OwnershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service managing trip identification, session boundary stitching,
 * and lifecycle management from raw telematics feeds (Step 4: Phase B).
 */
@Service
public class TripStitchingService {

    private static final Logger log = LoggerFactory.getLogger(TripStitchingService.class);

    private final TripRepository tripRepository;
    private final TelematicsRepository telematicsRepository;
    private final VehicleRepository vehicleRepository;
    private final GeoDistanceCalculator geoDistanceCalculator;

    @Autowired(required = false)
    private OwnershipService ownershipService;

    @Value("${app.trips.inactivity-threshold-minutes:10}")
    private int inactivityThresholdMinutes;

    public TripStitchingService(TripRepository tripRepository,
                                TelematicsRepository telematicsRepository,
                                VehicleRepository vehicleRepository,
                                GeoDistanceCalculator geoDistanceCalculator) {
        this.tripRepository = tripRepository;
        this.telematicsRepository = telematicsRepository;
        this.vehicleRepository = vehicleRepository;
        this.geoDistanceCalculator = geoDistanceCalculator;
    }

    public void setOwnershipService(OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    /**
     * Stitches chronological telemetry records into discrete trip sessions for a vehicle.
     * Reprocessing is idempotent: clears previous trip linkages and recalculates canonical trip boundaries.
     */
    @Transactional
    public List<TripResponseDto> stitchTripsForVehicle(Long vehicleId) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot stitch trips for unknown vehicle ID: " + vehicleId));

        // 1. Fetch all raw telemetry points sorted chronologically
        List<TelematicsRecord> allRecords = telematicsRepository.findByVehicleIdOrderByTimestampAsc(vehicleId);
        if (allRecords.isEmpty()) {
            log.info("No telemetry records found for vehicle ID: {}", vehicleId);
            return Collections.emptyList();
        }

        // 2. Sort safely and eliminate exact duplicate timestamps
        List<TelematicsRecord> sortedRecords = allRecords.stream()
                .filter(r -> r.getTimestamp() != null)
                .sorted(Comparator.comparing(TelematicsRecord::getTimestamp))
                .toList();

        // 3. Clear existing trips and references for safe, idempotent reprocessing
        tripRepository.deleteByVehicleId(vehicleId);
        for (TelematicsRecord rec : sortedRecords) {
            rec.setTrip(null);
        }

        long thresholdSeconds = Duration.ofMinutes(inactivityThresholdMinutes).toSeconds();
        Instant now = Instant.now();

        List<Trip> stitchedTrips = new ArrayList<>();
        List<TelematicsRecord> currentTripRecords = new ArrayList<>();

        for (TelematicsRecord record : sortedRecords) {
            if (currentTripRecords.isEmpty()) {
                currentTripRecords.add(record);
                continue;
            }

            TelematicsRecord previousRecord = currentTripRecords.get(currentTripRecords.size() - 1);
            long timeDeltaSeconds = ChronoUnit.SECONDS.between(previousRecord.getTimestamp(), record.getTimestamp());

            // If time gap exceeds inactivity threshold, finalize current trip and start new one
            if (timeDeltaSeconds > thresholdSeconds) {
                Trip finalizedTrip = buildTripFromRecords(vehicle, currentTripRecords, false, now);
                if (finalizedTrip != null) {
                    stitchedTrips.add(finalizedTrip);
                }
                currentTripRecords = new ArrayList<>();
            }

            currentTripRecords.add(record);
        }

        // Process remaining trailing trip records
        if (!currentTripRecords.isEmpty()) {
            Trip trailingTrip = buildTripFromRecords(vehicle, currentTripRecords, true, now);
            if (trailingTrip != null) {
                stitchedTrips.add(trailingTrip);
            }
        }

        // Persist trips and associate records
        List<Trip> savedTrips = tripRepository.saveAll(stitchedTrips);
        telematicsRepository.saveAll(sortedRecords);

        log.info("Successfully stitched {} trips for vehicle {} (Inactivity threshold: {}m)",
                savedTrips.size(), vehicle.getVehicleNumber(), inactivityThresholdMinutes);

        return savedTrips.stream()
                .map(TripResponseDto::fromEntity)
                .toList();
    }

    /**
     * Builds a Trip entity from an ordered cluster of telematics pings.
     */
    private Trip buildTripFromRecords(Vehicle vehicle, List<TelematicsRecord> records, boolean isLatestCluster, Instant now) {
        if (records == null || records.isEmpty()) {
            return null;
        }

        // Filter valid points for distance, coordinates, and duration calculations
        List<TelematicsRecord> validRecords = records.stream()
                .filter(r -> r.getValidationStatus() == TelematicsValidationStatus.VALID)
                .toList();

        int validCount = validRecords.size();
        int anomalousCount = records.size() - validCount;

        Instant startTime = records.get(0).getTimestamp();
        Instant endTime = records.get(records.size() - 1).getTimestamp();

        Double startLat = null;
        Double startLon = null;
        Double endLat = null;
        Double endLon = null;
        double totalDistanceMeters = 0.0;

        if (!validRecords.isEmpty()) {
            TelematicsRecord firstValid = validRecords.get(0);
            TelematicsRecord lastValid = validRecords.get(validRecords.size() - 1);

            startLat = firstValid.getLatitude();
            startLon = firstValid.getLongitude();
            endLat = lastValid.getLatitude();
            endLon = lastValid.getLongitude();

            // Calculate Haversine distance between consecutive valid points with jump filtering
            for (int i = 0; i < validRecords.size() - 1; i++) {
                TelematicsRecord p1 = validRecords.get(i);
                TelematicsRecord p2 = validRecords.get(i + 1);

                long deltaSec = Math.max(0, ChronoUnit.SECONDS.between(p1.getTimestamp(), p2.getTimestamp()));
                double segDist = geoDistanceCalculator.calculateDistanceMeters(
                        p1.getLatitude(), p1.getLongitude(),
                        p2.getLatitude(), p2.getLongitude()
                );

                if (geoDistanceCalculator.isPlausibleJump(segDist, deltaSec)) {
                    totalDistanceMeters += segDist;
                } else {
                    log.debug("Skipped implausible GPS jump of {:.1f}m in {}s between pings {} and {}",
                            segDist, deltaSec, p1.getId(), p2.getId());
                }
            }
        }

        long durationSeconds = Math.max(0, ChronoUnit.SECONDS.between(startTime, endTime));

        // Determine if trip is ongoing (in progress) or completed
        TripStatus status;
        if (isLatestCluster) {
            long minutesSinceLastRecord = ChronoUnit.MINUTES.between(endTime, now);
            if (minutesSinceLastRecord < inactivityThresholdMinutes) {
                status = TripStatus.IN_PROGRESS;
            } else {
                status = TripStatus.COMPLETED;
            }
        } else {
            status = TripStatus.COMPLETED;
        }

        Trip trip = new Trip(
                vehicle,
                startTime,
                status == TripStatus.IN_PROGRESS ? null : endTime,
                startLat,
                startLon,
                endLat,
                endLon,
                durationSeconds,
                totalDistanceMeters,
                validCount,
                anomalousCount,
                status
        );

        // Associate records with trip
        for (TelematicsRecord rec : records) {
            rec.setTrip(trip);
        }

        return trip;
    }

    /**
     * Retrieves paginated trips for a vehicle.
     */
    @Transactional(readOnly = true)
    public Page<TripResponseDto> getTripsForVehicle(Long vehicleId, Pageable pageable) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }
        return tripRepository.findByVehicleIdOrderByStartTimeDesc(vehicleId, pageable)
                .map(TripResponseDto::fromEntity);
    }

    /**
     * Retrieves a single trip by ID.
     */
    @Transactional(readOnly = true)
    public TripResponseDto getTripById(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));
        if (ownershipService != null) {
            ownershipService.verifyTripAccess(trip);
        }
        return TripResponseDto.fromEntity(trip);
    }

    public int getInactivityThresholdMinutes() {
        return inactivityThresholdMinutes;
    }

    public void setInactivityThresholdMinutes(int inactivityThresholdMinutes) {
        this.inactivityThresholdMinutes = inactivityThresholdMinutes;
    }
}
