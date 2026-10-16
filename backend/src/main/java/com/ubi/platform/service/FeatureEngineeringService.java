package com.ubi.platform.service;

import com.ubi.platform.dto.DrivingFeatureResponseDto;
import com.ubi.platform.entity.*;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.DrivingFeatureRepository;
import com.ubi.platform.repository.TelematicsRepository;
import com.ubi.platform.repository.TripRepository;
import com.ubi.platform.repository.VehicleRepository;
import com.ubi.platform.security.OwnershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service calculating actuarial driving behavior features and safety risk vectors
 * from completed trip sessions (Step 4: Phase D).
 */
@Service
public class FeatureEngineeringService {

    private static final Logger log = LoggerFactory.getLogger(FeatureEngineeringService.class);

    private final DrivingFeatureRepository featureRepository;
    private final TripRepository tripRepository;
    private final TelematicsRepository telematicsRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired(required = false)
    private OwnershipService ownershipService;

    @Value("${app.features.harsh-braking-threshold-g:-0.35}")
    private double harshBrakingThresholdG = -0.35;

    @Value("${app.features.rapid-accel-threshold-g:0.30}")
    private double rapidAccelThresholdG = 0.30;

    @Value("${app.features.speeding-threshold-kmh:120.0}")
    private double speedingThresholdKmh = 120.0;

    @Value("${app.features.night-start-hour:22}")
    private int nightStartHour = 22;

    @Value("${app.features.night-end-hour:5}")
    private int nightEndHour = 5;

    @Value("${app.features.timezone:Asia/Kolkata}")
    private String appTimezone = "Asia/Kolkata";

    @Value("${app.features.calculation-version:v1.0}")
    private String calculationVersion = "v1.0";

    public FeatureEngineeringService(DrivingFeatureRepository featureRepository,
                                     TripRepository tripRepository,
                                     TelematicsRepository telematicsRepository,
                                     VehicleRepository vehicleRepository) {
        this.featureRepository = featureRepository;
        this.tripRepository = tripRepository;
        this.telematicsRepository = telematicsRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public void setOwnershipService(OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    /**
     * Calculates or safely recalculates driving risk features for a single trip.
     */
    @Transactional
    public DrivingFeatureResponseDto calculateFeaturesForTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        if (ownershipService != null) {
            ownershipService.verifyTripAccess(trip);
        }

        // 1. Fetch telematics records associated with this trip
        List<TelematicsRecord> tripRecords = telematicsRepository.findByTripIdOrderByTimestampAsc(tripId);

        // Fallback: If no records directly linked by foreign key, query by vehicle & time boundary
        if (tripRecords.isEmpty() && trip.getStartTime() != null) {
            Instant end = trip.getEndTime() != null ? trip.getEndTime() : Instant.now();
            tripRecords = telematicsRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampAsc(
                    trip.getVehicle().getId(), trip.getStartTime(), end
            );
        }

        // 2. Separate valid records from anomalies
        List<TelematicsRecord> validRecords = tripRecords.stream()
                .filter(r -> r.getValidationStatus() == TelematicsValidationStatus.VALID)
                .toList();

        int totalRecords = tripRecords.size();
        int validCount = validRecords.size();
        int anomalousCount = totalRecords - validCount;

        // Data quality score & status
        double dataQualityScore = totalRecords > 0
                ? Math.round(((double) validCount / totalRecords) * 1000.0) / 10.0
                : 100.0;
        String dataQualityStatus;
        if (dataQualityScore >= 90.0) {
            dataQualityStatus = "EXCELLENT";
        } else if (dataQualityScore >= 70.0) {
            dataQualityStatus = "DEGRADED";
        } else {
            dataQualityStatus = "POOR";
        }

        // 3. Velocity aggregates
        double maxSpeed = 0.0;
        double sumSpeed = 0.0;
        int speedingCount = 0;

        for (TelematicsRecord r : validRecords) {
            double speed = r.getSpeedKmh() != null ? r.getSpeedKmh() : 0.0;
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }
            sumSpeed += speed;
            if (speed >= speedingThresholdKmh) {
                speedingCount++;
            }
        }

        double avgSpeed = validCount > 0
                ? Math.round((sumSpeed / validCount) * 10.0) / 10.0
                : 0.0;
        double speedingPct = validCount > 0
                ? Math.round(((double) speedingCount / validCount) * 1000.0) / 10.0
                : 0.0;

        // 4. Debounced Event Counting (Prevents double counting sustained continuous events)
        int harshBrakeEvents = 0;
        boolean inHarshBrake = false;

        int rapidAccelEvents = 0;
        boolean inRapidAccel = false;

        for (TelematicsRecord r : validRecords) {
            double accel = r.getAccelerationG() != null ? r.getAccelerationG() : 0.0;

            // Harsh braking debounce (threshold <= harshBrakingThresholdG)
            if (accel <= harshBrakingThresholdG) {
                if (!inHarshBrake) {
                    harshBrakeEvents++;
                    inHarshBrake = true;
                }
            } else {
                inHarshBrake = false;
            }

            // Rapid acceleration debounce (threshold >= rapidAccelThresholdG)
            if (accel >= rapidAccelThresholdG) {
                if (!inRapidAccel) {
                    rapidAccelEvents++;
                    inRapidAccel = true;
                }
            } else {
                inRapidAccel = false;
            }
        }

        // 5. Night-Driving Exposure (Evaluated in configured local timezone)
        long nightDrivingSeconds = 0L;
        ZoneId zone;
        try {
            zone = ZoneId.of(appTimezone);
        } catch (Exception e) {
            log.warn("Invalid timezone '{}', falling back to UTC", appTimezone);
            zone = ZoneId.of("UTC");
        }

        for (int i = 0; i < validRecords.size() - 1; i++) {
            TelematicsRecord p1 = validRecords.get(i);
            TelematicsRecord p2 = validRecords.get(i + 1);

            long deltaSec = ChronoUnit.SECONDS.between(p1.getTimestamp(), p2.getTimestamp());
            // Filter out excessive gaps (prevent parking duration from accumulating into night driving)
            if (deltaSec > 0 && deltaSec <= 600) {
                Instant midpoint = p1.getTimestamp().plusSeconds(deltaSec / 2);
                ZonedDateTime localTime = midpoint.atZone(zone);
                int hour = localTime.getHour();

                // Night window: e.g., 22:00 to 05:00
                boolean isNight = (nightStartHour > nightEndHour)
                        ? (hour >= nightStartHour || hour < nightEndHour)
                        : (hour >= nightStartHour && hour < nightEndHour);

                if (isNight) {
                    nightDrivingSeconds += deltaSec;
                }
            }
        }

        long nightDrivingMinutes = nightDrivingSeconds / 60;
        long durationSeconds = trip.getDurationSeconds() != null ? trip.getDurationSeconds() : 0L;
        double nightDrivingRatio = durationSeconds > 0
                ? Math.min(1.0, Math.round(((double) nightDrivingSeconds / durationSeconds) * 1000.0) / 1000.0)
                : 0.0;

        // 6. Normalized Event Rates per 100 km (Safe division: requires distance >= 0.1 km)
        Double harshBrakeRate = null;
        Double rapidAccelRate = null;
        double tripDistanceKm = trip.getDistanceKm() != null ? trip.getDistanceKm() : 0.0;

        if (tripDistanceKm >= 0.1) {
            harshBrakeRate = Math.round(((double) harshBrakeEvents / tripDistanceKm) * 100.0 * 10.0) / 10.0;
            rapidAccelRate = Math.round(((double) rapidAccelEvents / tripDistanceKm) * 100.0 * 10.0) / 10.0;
        }

        // 7. Idempotent Persistence (Update in-place if already calculated)
        Optional<DrivingFeature> existingOpt = featureRepository.findByTripId(tripId);
        DrivingFeature feature = existingOpt.orElseGet(DrivingFeature::new);

        feature.setTrip(trip);
        feature.setVehicle(trip.getVehicle());
        feature.setCalculationVersion(calculationVersion);
        feature.setCalculatedAt(Instant.now());
        feature.setDistanceKm(tripDistanceKm);
        feature.setDurationSeconds(durationSeconds);
        feature.setAverageSpeedKmh(avgSpeed);
        feature.setMaxSpeedKmh(Math.round(maxSpeed * 10.0) / 10.0);
        feature.setHarshBrakingCount(harshBrakeEvents);
        feature.setRapidAccelerationCount(rapidAccelEvents);
        feature.setSpeedingObservationsCount(speedingCount);
        feature.setHarshBrakeRatePer100Km(harshBrakeRate);
        feature.setRapidAccelRatePer100Km(rapidAccelRate);
        feature.setSpeedingPct(speedingPct);
        feature.setNightDrivingMinutes(nightDrivingMinutes);
        feature.setNightDrivingRatio(nightDrivingRatio);
        feature.setValidRecordCount(validCount);
        feature.setAnomalousRecordCount(anomalousCount);
        feature.setDataQualityScore(dataQualityScore);
        feature.setDataQualityStatus(dataQualityStatus);

        DrivingFeature saved = featureRepository.save(feature);

        // Mark trip as features calculated
        trip.setFeaturesCalculated(true);
        tripRepository.save(trip);

        log.info("Calculated features for trip ID {} [Dist: {}km, HarshBrakes: {}, RapidAccels: {}, NightRatio: {}]",
                tripId, tripDistanceKm, harshBrakeEvents, rapidAccelEvents, nightDrivingRatio);

        return DrivingFeatureResponseDto.fromEntity(saved);
    }

    /**
     * Calculates features for all trips belonging to a vehicle.
     */
    @Transactional
    public List<DrivingFeatureResponseDto> calculateFeaturesForVehicle(Long vehicleId) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }

        List<Trip> trips = tripRepository.findByVehicleIdOrderByStartTimeAsc(vehicleId);
        List<DrivingFeatureResponseDto> results = new ArrayList<>();

        for (Trip trip : trips) {
            results.add(calculateFeaturesForTrip(trip.getId()));
        }

        return results;
    }

    /**
     * Retrieves stored feature summary for a trip.
     */
    @Transactional(readOnly = true)
    public DrivingFeatureResponseDto getFeaturesForTrip(Long tripId) {
        DrivingFeature feature = featureRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("No features calculated yet for trip ID: " + tripId));
        if (ownershipService != null) {
            ownershipService.verifyTripAccess(feature.getTrip());
        }
        return DrivingFeatureResponseDto.fromEntity(feature);
    }

    // Getters / Setters for test configurations

    public void setHarshBrakingThresholdG(double harshBrakingThresholdG) {
        this.harshBrakingThresholdG = harshBrakingThresholdG;
    }

    public void setRapidAccelThresholdG(double rapidAccelThresholdG) {
        this.rapidAccelThresholdG = rapidAccelThresholdG;
    }

    public void setSpeedingThresholdKmh(double speedingThresholdKmh) {
        this.speedingThresholdKmh = speedingThresholdKmh;
    }

    public void setAppTimezone(String appTimezone) {
        this.appTimezone = appTimezone;
    }

    public void setNightStartHour(int nightStartHour) {
        this.nightStartHour = nightStartHour;
    }

    public void setNightEndHour(int nightEndHour) {
        this.nightEndHour = nightEndHour;
    }
}
