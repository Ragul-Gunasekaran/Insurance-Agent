package com.ubi.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.PremiumQuoteResponseDto;
import com.ubi.platform.dto.PricingCalculationRequestDto;
import com.ubi.platform.dto.PricingConfigurationResponseDto;
import com.ubi.platform.entity.*;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.PremiumQuoteRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service orchestrating actuarial pricing quotes, input validation, and historical quote persistence (Step 6).
 */
@Service
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    private final PremiumQuoteRepository quoteRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final PricingCalculator pricingCalculator;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private OwnershipService ownershipService;

    @Value("${app.pricing.version:pricing-v1}")
    private String pricingVersion = "pricing-v1";

    @Value("${app.pricing.currency:INR}")
    private String currency = "INR";

    @Value("${app.pricing.stale-score-days:90}")
    private int staleScoreDays = 90;

    public PricingService(PremiumQuoteRepository quoteRepository,
                          VehicleRepository vehicleRepository,
                          TripRepository tripRepository,
                          RiskScoreRepository riskScoreRepository,
                          PricingCalculator pricingCalculator,
                          ObjectMapper objectMapper) {
        this.quoteRepository = quoteRepository;
        this.vehicleRepository = vehicleRepository;
        this.tripRepository = tripRepository;
        this.riskScoreRepository = riskScoreRepository;
        this.pricingCalculator = pricingCalculator;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public void setOwnershipService(OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    /**
     * Calculates and persists a new actuarial premium quote snapshot for a vehicle.
     */
    @Transactional
    public PremiumQuoteResponseDto calculateQuoteForVehicle(Long vehicleId, PricingCalculationRequestDto request) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        List<Trip> allTrips = tripRepository.findByVehicleIdOrderByStartTimeAsc(vehicleId);

        int excludedTripsCount = 0;
        List<EligibleTripScore> eligibleTripScores = new ArrayList<>();
        double totalObservedKm = 0.0;
        Instant earliestStart = null;
        Instant latestEnd = null;

        Instant staleCutoff = Instant.now().minus(staleScoreDays, ChronoUnit.DAYS);

        for (Trip trip : allTrips) {
            // Exclude in-progress trips
            if (trip.getStatus() != TripStatus.COMPLETED) {
                excludedTripsCount++;
                continue;
            }

            Optional<RiskScore> scoreOpt = riskScoreRepository.findByTripId(trip.getId());
            if (scoreOpt.isEmpty()) {
                excludedTripsCount++;
                continue;
            }

            RiskScore score = scoreOpt.get();

            // Exclude unrated or unscorable trips or stale scores
            if (!Boolean.TRUE.equals(score.getIsScorable())
                    || score.getScore() == null
                    || "UNRATED".equalsIgnoreCase(score.getScoreCategory())
                    || (score.getCalculatedAt() != null && score.getCalculatedAt().isBefore(staleCutoff))) {
                excludedTripsCount++;
                continue;
            }

            double dist = trip.getDistanceKm() != null ? trip.getDistanceKm() : 0.0;
            eligibleTripScores.add(new EligibleTripScore(score.getScore(), score.getScoreCategory(), dist));
            totalObservedKm += dist;

            if (trip.getStartTime() != null) {
                if (earliestStart == null || trip.getStartTime().isBefore(earliestStart)) {
                    earliestStart = trip.getStartTime();
                }
            }
            Instant tripEnd = trip.getEndTime() != null ? trip.getEndTime() : trip.getStartTime();
            if (tripEnd != null) {
                if (latestEnd == null || tripEnd.isAfter(latestEnd)) {
                    latestEnd = tripEnd;
                }
            }
        }

        // Aggregate eligible risk score (distance-weighted)
        Double aggregateScore = null;
        String aggregateCategory = "UNRATED";
        boolean isScoreEligible = false;

        if (!eligibleTripScores.isEmpty()) {
            double weightedSum = 0.0;
            double sumDist = 0.0;
            for (EligibleTripScore ets : eligibleTripScores) {
                weightedSum += (ets.score * Math.max(0.1, ets.distanceKm));
                sumDist += Math.max(0.1, ets.distanceKm);
            }
            aggregateScore = Math.round((weightedSum / sumDist) * 10.0) / 10.0;
            if (aggregateScore >= 85.0) {
                aggregateCategory = "EXCELLENT";
            } else if (aggregateScore >= 70.0) {
                aggregateCategory = "GOOD";
            } else if (aggregateScore >= 50.0) {
                aggregateCategory = "MODERATE";
            } else {
                aggregateCategory = "HIGH_RISK";
            }
            isScoreEligible = true;
        }

        // Observation period in days
        Integer observationDays = null;
        if (earliestStart != null && latestEnd != null) {
            long days = ChronoUnit.DAYS.between(earliestStart, latestEnd);
            observationDays = Math.max(1, (int) days);
        }

        Double userDistanceOverride = request != null ? request.getAnnualDistanceKmAssumption() : null;

        PricingCalculator.PricingInputs inputs = new PricingCalculator.PricingInputs(
                aggregateScore,
                aggregateCategory,
                isScoreEligible,
                totalObservedKm > 0 ? Math.round(totalObservedKm * 100.0) / 100.0 : null,
                observationDays,
                userDistanceOverride,
                excludedTripsCount
        );

        PricingCalculator.PricingResult result = pricingCalculator.calculate(inputs);

        // Generate unique quote reference
        String quoteRef = "QTE-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        PremiumQuote quote = new PremiumQuote();
        quote.setQuoteReference(quoteRef);
        quote.setVehicle(vehicle);
        quote.setStatus(result.getStatus());
        quote.setIsEligible(result.isEligible());
        quote.setBaseAnnualPremiumInr(result.getBaseAnnualPremiumInr());
        quote.setSafetyScore(result.getSafetyScore());
        quote.setRiskCategory(result.getRiskCategory());
        quote.setRiskMultiplier(result.getRiskMultiplier());
        quote.setObservedDistanceKm(result.getObservedDistanceKm());
        quote.setObservationDays(result.getObservationDays());
        quote.setProjectedAnnualDistanceKm(result.getProjectedAnnualDistanceKm());
        quote.setAnnualDistanceSource(result.getAnnualDistanceSource());
        quote.setUsageMultiplier(result.getUsageMultiplier());
        quote.setRawAnnualPremiumInr(result.getRawAnnualPremiumInr());
        quote.setFinalAnnualPremiumInr(result.getFinalAnnualPremiumInr());
        quote.setCurrency(currency);
        quote.setPricingVersion(pricingVersion);
        quote.setCalculatedAt(Instant.now());
        quote.setExcludedTripsCount(result.getExcludedTripsCount());
        quote.setSummaryExplanation(result.getSummaryExplanation());

        try {
            quote.setFactorExplanationsJson(objectMapper.writeValueAsString(result.getFactorExplanations()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize pricing factor explanations for vehicle ID {}", vehicleId, e);
            quote.setFactorExplanationsJson("[]");
        }

        PremiumQuote saved = quoteRepository.save(quote);
        log.info("Generated actuarial premium quote {} for vehicle ID {} [Status: {}, Final: ₹{}]",
                quoteRef, vehicleId, result.getStatus(), result.getFinalAnnualPremiumInr());

        return PremiumQuoteResponseDto.fromEntity(saved, objectMapper);
    }

    /**
     * Retrieves a quote snapshot by its primary database ID.
     */
    @Transactional(readOnly = true)
    public PremiumQuoteResponseDto getQuoteById(Long quoteId) {
        PremiumQuote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Premium quote not found with ID: " + quoteId));
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(quote.getVehicle().getId());
        }
        return PremiumQuoteResponseDto.fromEntity(quote, objectMapper);
    }

    /**
     * Retrieves paginated quote history for a vehicle.
     */
    @Transactional(readOnly = true)
    public Page<PremiumQuoteResponseDto> getQuotesForVehicle(Long vehicleId, Pageable pageable) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }

        Page<PremiumQuote> page = quoteRepository.findByVehicleId(vehicleId, pageable);
        return page.map(entity -> PremiumQuoteResponseDto.fromEntity(entity, objectMapper));
    }

    /**
     * Returns safe, non-sensitive prototype pricing configurations.
     */
    public PricingConfigurationResponseDto getPricingConfiguration() {
        Map<String, BigDecimal> multipliers = new LinkedHashMap<>();
        multipliers.put("EXCELLENT", pricingCalculator.getExcellentMultiplier());
        multipliers.put("GOOD", pricingCalculator.getGoodMultiplier());
        multipliers.put("MODERATE", pricingCalculator.getModerateMultiplier());
        multipliers.put("HIGH_RISK", pricingCalculator.getHighRiskMultiplier());

        return new PricingConfigurationResponseDto(
                pricingCalculator.getBaseAnnualPremiumInr(),
                pricingCalculator.getMinPremiumInr(),
                pricingCalculator.getMaxPremiumInr(),
                multipliers,
                pricingCalculator.getBaselineAnnualDistanceKm(),
                pricingCalculator.getMinObservationDaysForProjection(),
                pricingCalculator.getMinObservedDistanceKmForProjection(),
                pricingVersion,
                currency,
                "Illustrative pricing prototype for Hackathon Problem Statement PS-008. Not actuarially certified or regulator approved. Does not constitute a binding insurance offer or policy change."
        );
    }

    private static class EligibleTripScore {
        final double score;
        final String category;
        final double distanceKm;

        EligibleTripScore(double score, String category, double distanceKm) {
            this.score = score;
            this.category = category;
            this.distanceKm = distanceKm;
        }
    }
}
