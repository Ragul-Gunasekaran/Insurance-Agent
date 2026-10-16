package com.ubi.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.RiskScoreResponseDto;
import com.ubi.platform.entity.*;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.DrivingFeatureRepository;
import com.ubi.platform.repository.RiskScoreRepository;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service managing behavioral risk scoring and explainability for trips and vehicles (Step 5).
 * Completely decoupled from insurance premium rating and actuarial underwriting.
 */
@Service
public class RiskScoringService {

    private static final Logger log = LoggerFactory.getLogger(RiskScoringService.class);

    private final RiskScoreRepository riskScoreRepository;
    private final TripRepository tripRepository;
    private final DrivingFeatureRepository featureRepository;
    private final VehicleRepository vehicleRepository;
    private final RiskScoringCalculator scoringCalculator;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private OwnershipService ownershipService;

    @Value("${app.risk.scoring-version:v1.0}")
    private String scoringVersion = "v1.0";

    public RiskScoringService(RiskScoreRepository riskScoreRepository,
                              TripRepository tripRepository,
                              DrivingFeatureRepository featureRepository,
                              VehicleRepository vehicleRepository,
                              RiskScoringCalculator scoringCalculator,
                              ObjectMapper objectMapper) {
        this.riskScoreRepository = riskScoreRepository;
        this.tripRepository = tripRepository;
        this.featureRepository = featureRepository;
        this.vehicleRepository = vehicleRepository;
        this.scoringCalculator = scoringCalculator;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public void setOwnershipService(OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    /**
     * Calculates or recalculates the behavioral safety risk score for a single trip.
     * Guaranteed to be idempotent: updates existing record in place without unique constraint conflicts.
     */
    @Transactional
    public RiskScoreResponseDto calculateScoreForTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with ID: " + tripId));

        if (ownershipService != null) {
            ownershipService.verifyTripAccess(trip);
        }

        DrivingFeature feature = featureRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("No driving features calculated yet for trip ID: " + tripId + ". Please calculate trip features first."));

        RiskScoringCalculator.ScoringResult result = scoringCalculator.calculateScore(trip, feature);

        // Idempotent upsert
        Optional<RiskScore> existingOpt = riskScoreRepository.findByTripId(tripId);
        RiskScore riskScore = existingOpt.orElseGet(RiskScore::new);

        riskScore.setTrip(trip);
        riskScore.setVehicle(trip.getVehicle());
        riskScore.setScore(result.getScore());
        riskScore.setScoreCategory(result.getScoreCategory());
        riskScore.setScoringVersion(scoringVersion);
        riskScore.setCalculatedAt(Instant.now());
        riskScore.setDataQualityStatus(result.getDataQualityStatus());
        riskScore.setIsScorable(result.isScorable());
        riskScore.setHarshBrakingPenalty(result.getHarshBrakingPenalty());
        riskScore.setRapidAccelerationPenalty(result.getRapidAccelerationPenalty());
        riskScore.setSpeedingPenalty(result.getSpeedingPenalty());
        riskScore.setNightDrivingPenalty(result.getNightDrivingPenalty());
        riskScore.setTotalPenalties(result.getTotalPenalties());
        riskScore.setSummaryExplanation(result.getSummaryExplanation());

        try {
            riskScore.setFactorExplanationsJson(objectMapper.writeValueAsString(result.getFactorExplanations()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize factor explanations for trip ID {}", tripId, e);
            riskScore.setFactorExplanationsJson("[]");
        }

        RiskScore saved = riskScoreRepository.save(riskScore);

        trip.setScoreCalculated(true);
        tripRepository.save(trip);

        log.info("Calculated behavioral safety score for trip ID {} [Score: {}, Category: {}, Status: {}]",
                tripId, result.getScore(), result.getScoreCategory(), result.getDataQualityStatus());

        return RiskScoreResponseDto.fromEntity(saved, objectMapper);
    }

    /**
     * Retrieves the stored risk score summary for a trip.
     */
    @Transactional(readOnly = true)
    public RiskScoreResponseDto getScoreForTrip(Long tripId) {
        RiskScore score = riskScoreRepository.findByTripId(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("No risk score calculated yet for trip ID: " + tripId));
        if (ownershipService != null) {
            ownershipService.verifyTripAccess(score.getTrip());
        }
        return RiskScoreResponseDto.fromEntity(score, objectMapper);
    }

    /**
     * Batch calculates risk scores for eligible completed trips associated with a vehicle.
     */
    @Transactional
    public List<RiskScoreResponseDto> calculateScoresForVehicle(Long vehicleId) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }

        List<Trip> trips = tripRepository.findByVehicleIdOrderByStartTimeAsc(vehicleId);
        List<RiskScoreResponseDto> results = new ArrayList<>();

        for (Trip trip : trips) {
            if (featureRepository.existsByTripId(trip.getId())) {
                try {
                    results.add(calculateScoreForTrip(trip.getId()));
                } catch (Exception e) {
                    log.warn("Skipping score calculation for trip ID {} due to error: {}", trip.getId(), e.getMessage());
                }
            }
        }

        return results;
    }

    /**
     * Retrieves paginated risk scores for a vehicle.
     */
    @Transactional(readOnly = true)
    public Page<RiskScoreResponseDto> getScoresForVehicle(Long vehicleId, Pageable pageable) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }

        Page<RiskScore> page = riskScoreRepository.findByVehicleId(vehicleId, pageable);
        return page.map(entity -> RiskScoreResponseDto.fromEntity(entity, objectMapper));
    }

    public void setScoringVersion(String scoringVersion) {
        this.scoringVersion = scoringVersion;
    }
}
