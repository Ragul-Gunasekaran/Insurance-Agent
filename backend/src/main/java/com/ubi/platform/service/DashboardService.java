package com.ubi.platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.*;
import com.ubi.platform.entity.DrivingFeature;
import com.ubi.platform.entity.PremiumQuote;
import com.ubi.platform.entity.RiskScore;
import com.ubi.platform.entity.Trip;
import com.ubi.platform.entity.TripStatus;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.*;
import com.ubi.platform.security.OwnershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service orchestrating customer driver scorecards and insurer analytics (Step 7).
 * Implements distance-weighted aggregates, zero-fabrication trends, and defensive null/empty handling.
 */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final DrivingFeatureRepository drivingFeatureRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final PremiumQuoteRepository premiumQuoteRepository;
    private final TelematicsRepository telematicsRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private OwnershipService ownershipService;

    public DashboardService(VehicleRepository vehicleRepository,
                            TripRepository tripRepository,
                            DrivingFeatureRepository drivingFeatureRepository,
                            RiskScoreRepository riskScoreRepository,
                            PremiumQuoteRepository premiumQuoteRepository,
                            TelematicsRepository telematicsRepository,
                            ObjectMapper objectMapper) {
        this.vehicleRepository = vehicleRepository;
        this.tripRepository = tripRepository;
        this.drivingFeatureRepository = drivingFeatureRepository;
        this.riskScoreRepository = riskScoreRepository;
        this.premiumQuoteRepository = premiumQuoteRepository;
        this.telematicsRepository = telematicsRepository;
        this.objectMapper = objectMapper;
    }

    public void setOwnershipService(OwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }

    /**
     * Aggregates vehicle summary, latest safety score, distance-weighted behavioral insights,
     * and latest quote for the Customer Driver Scorecard.
     */
    @Transactional(readOnly = true)
    public CustomerScorecardSummaryDto getCustomerScorecard(Long vehicleId) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        CustomerScorecardSummaryDto dto = new CustomerScorecardSummaryDto();
        dto.setVehicleId(vehicle.getId());
        dto.setVehicleNumber(vehicle.getVehicleNumber());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        dto.setManufacturingYear(vehicle.getManufacturingYear());
        dto.setFuelType(vehicle.getFuelType() != null ? vehicle.getFuelType().name() : null);
        dto.setVerificationStatus(vehicle.getVerificationStatus() != null ?
                vehicle.getVerificationStatus().name() + " (Mock Verification)" : "UNVERIFIED (Mock Verification)");
        dto.setIsMockVerification(true);

        // Trip counts and observed distance
        long completedTrips = tripRepository.countByVehicleIdAndStatus(vehicleId, TripStatus.COMPLETED);
        Double totalDistance = tripRepository.sumDistanceByVehicleIdAndStatus(vehicleId, TripStatus.COMPLETED);
        dto.setCompletedTripsCount(completedTrips);
        dto.setTotalObservedDistanceKm(round2(totalDistance != null ? totalDistance : 0.0));

        // Risk scores (ordered latest first)
        List<RiskScore> scores = riskScoreRepository.findByVehicleIdOrderByCalculatedAtDesc(vehicleId);
        if (!scores.isEmpty()) {
            RiskScore latest = scores.get(0);
            dto.setScoringVersion(latest.getScoringVersion());
            dto.setScoreCalculatedAt(latest.getCalculatedAt());
            dto.setDataQualityStatus(latest.getDataQualityStatus());
            dto.setScoreExplanations(parseRiskFactors(latest.getFactorExplanationsJson()));

            if (Boolean.TRUE.equals(latest.getIsScorable())) {
                dto.setLatestSafetyScore(round1(latest.getScore()));
                dto.setLatestRiskCategory(latest.getScoreCategory());
                dto.setUnratedReason(null);
            } else {
                // Do not display an unrated trip as a score of zero or 100
                dto.setLatestSafetyScore(null);
                dto.setLatestRiskCategory("UNRATED");
                dto.setUnratedReason(latest.getSummaryExplanation() != null ?
                        latest.getSummaryExplanation() : "Trip did not satisfy minimum quality or distance thresholds for scoring.");
            }

            // Build chronological score history for trend progression
            List<ScoreHistoryPointDto> history = new ArrayList<>();
            // Reverse so oldest is first for charts
            for (int i = scores.size() - 1; i >= 0; i--) {
                RiskScore s = scores.get(i);
                history.add(new ScoreHistoryPointDto(
                        s.getTrip() != null ? s.getTrip().getId() : null,
                        s.getCalculatedAt(),
                        Boolean.TRUE.equals(s.getIsScorable()) ? round1(s.getScore()) : null,
                        s.getScoreCategory(),
                        s.getTrip() != null ? s.getTrip().getDistanceKm() : null,
                        s.getIsScorable()
                ));
            }
            dto.setScoreHistory(history);
        } else {
            dto.setLatestSafetyScore(null);
            dto.setLatestRiskCategory("NOT_AVAILABLE");
            dto.setDataQualityStatus("NO_DATA");
            dto.setUnratedReason("No trips have been analyzed or scored yet for this vehicle.");
        }

        // Distance-weighted driving performance rates
        List<Object[]> sumsList = drivingFeatureRepository.findVehicleFeatureSums(vehicleId);
        if (sumsList != null && !sumsList.isEmpty() && sumsList.get(0) != null) {
            Object[] row = sumsList.get(0);
            long harshBraking = ((Number) row[0]).longValue();
            long rapidAccel = ((Number) row[1]).longValue();
            long speedingObs = ((Number) row[2]).longValue();
            long nightMinutes = ((Number) row[3]).longValue();
            double distKm = ((Number) row[6]).doubleValue();
            long durationSec = ((Number) row[7]).longValue();

            dto.setNightDrivingMinutes(nightMinutes);
            if (distKm > 0.0) {
                dto.setHarshBrakeRatePer100Km(round2((harshBraking / distKm) * 100.0));
                dto.setRapidAccelRatePer100Km(round2((rapidAccel / distKm) * 100.0));
                dto.setSpeedingObsPer100Km(round2((speedingObs / distKm) * 100.0));
            }
            if (durationSec > 0) {
                dto.setNightDrivingRatio(round4((double) (nightMinutes * 60) / durationSec));
            }
        }

        // Latest quote summary
        Optional<PremiumQuote> latestQuoteOpt = premiumQuoteRepository.findTopByVehicleIdOrderByCalculatedAtDesc(vehicleId);
        if (latestQuoteOpt.isPresent()) {
            PremiumQuote q = latestQuoteOpt.get();
            dto.setLatestQuoteReference(q.getQuoteReference());
            dto.setLatestQuoteStatus(q.getStatus());
            dto.setIsQuoteEligible(q.getIsEligible());
            dto.setLatestEstimatedAnnualPremiumInr(q.getFinalAnnualPremiumInr());
            dto.setLatestBasePremiumInr(q.getBaseAnnualPremiumInr());
            dto.setLatestRiskMultiplier(q.getRiskMultiplier());
            dto.setLatestUsageMultiplier(q.getUsageMultiplier());
            dto.setLatestAnnualDistanceSource(q.getAnnualDistanceSource());
            dto.setLatestPricingVersion(q.getPricingVersion());
            dto.setLatestQuoteCalculatedAt(q.getCalculatedAt());
            dto.setLatestQuoteSummary(q.getSummaryExplanation());
        }

        return dto;
    }

    /**
     * Retrieves paginated trip history with embedded driving features and risk scores.
     */
    @Transactional(readOnly = true)
    public PageResponse<CustomerTripSummaryDto> getCustomerTrips(Long vehicleId, Pageable pageable) {
        if (ownershipService != null) {
            ownershipService.verifyVehicleAccess(vehicleId);
        }
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId);
        }

        Page<Trip> trips = tripRepository.findByVehicleIdOrderByStartTimeDesc(vehicleId, pageable);
        List<CustomerTripSummaryDto> content = new ArrayList<>();

        for (Trip trip : trips.getContent()) {
            CustomerTripSummaryDto tDto = new CustomerTripSummaryDto();
            tDto.setTripId(trip.getId());
            tDto.setStartTime(trip.getStartTime());
            tDto.setEndTime(trip.getEndTime());
            tDto.setDistanceKm(trip.getDistanceKm());
            tDto.setDurationSeconds(trip.getDurationSeconds());
            tDto.setStatus(trip.getStatus() != null ? trip.getStatus().name() : null);

            // Fetch features
            Optional<DrivingFeature> featureOpt = drivingFeatureRepository.findByTripId(trip.getId());
            featureOpt.ifPresent(f -> {
                tDto.setDataQualityStatus(f.getDataQualityStatus());
                tDto.setDataQualityScore(f.getDataQualityScore());
                tDto.setHarshBrakingCount(f.getHarshBrakingCount());
                tDto.setRapidAccelerationCount(f.getRapidAccelerationCount());
                tDto.setSpeedingObservationsCount(f.getSpeedingObservationsCount());
                tDto.setNightDrivingMinutes(f.getNightDrivingMinutes());
                tDto.setHarshBrakeRatePer100Km(f.getHarshBrakeRatePer100Km());
                tDto.setRapidAccelRatePer100Km(f.getRapidAccelRatePer100Km());
                tDto.setSpeedingPct(f.getSpeedingPct());
            });

            // Fetch score
            Optional<RiskScore> scoreOpt = riskScoreRepository.findByTripId(trip.getId());
            if (scoreOpt.isPresent()) {
                RiskScore s = scoreOpt.get();
                tDto.setIsScorable(s.getIsScorable());
                tDto.setRiskCategory(s.getScoreCategory());
                tDto.setSafetyScore(Boolean.TRUE.equals(s.getIsScorable()) ? round1(s.getScore()) : null);
                tDto.setUnscorableReason(s.getSummaryExplanation());
                tDto.setFactorExplanations(parseRiskFactors(s.getFactorExplanationsJson()));
            } else {
                tDto.setIsScorable(false);
                tDto.setRiskCategory("UNRATED");
                tDto.setSafetyScore(null);
                tDto.setUnscorableReason("Score not calculated yet.");
            }

            content.add(tDto);
        }

        return PageResponse.fromPage(trips, content);
    }

    /**
     * Aggregates fleet macro overview metrics.
     */
    @Transactional(readOnly = true)
    public InsurerFleetOverviewDto getInsurerFleetOverview() {
        InsurerFleetOverviewDto dto = new InsurerFleetOverviewDto();
        dto.setTotalRegisteredVehicles(vehicleRepository.count());
        dto.setVehiclesWithTelematics((long) telematicsRepository.findDistinctVehicleIdsWithTelematics().size());
        dto.setVehiclesWithTrips((long) tripRepository.findDistinctVehicleIdsWithTrips().size());
        dto.setCompletedTripsCount(tripRepository.countByStatus(TripStatus.COMPLETED));

        Double totalDistance = tripRepository.sumDistanceByStatus(TripStatus.COMPLETED);
        dto.setTotalObservedDistanceKm(round2(totalDistance != null ? totalDistance : 0.0));

        dto.setScorableTripsCount(riskScoreRepository.countByIsScorableTrue());
        dto.setUnratedTripsCount(riskScoreRepository.countByIsScorableFalse());
        dto.setVehiclesWithEligibleQuotes((long) premiumQuoteRepository.findDistinctVehicleIdsWithEligibleQuotes().size());

        Double avgScore = riskScoreRepository.findAverageSafetyScore();
        dto.setAverageSafetyScore(avgScore != null ? round1(avgScore) : null);

        return dto;
    }

    /**
     * Aggregates safety score categories across trips and distinct vehicles.
     */
    @Transactional(readOnly = true)
    public InsurerRiskDistributionDto getInsurerRiskDistribution() {
        InsurerRiskDistributionDto dto = new InsurerRiskDistributionDto();

        // 1. Trip-level breakdown
        Map<String, Long> tripCounts = new LinkedHashMap<>();
        tripCounts.put("EXCELLENT", 0L);
        tripCounts.put("GOOD", 0L);
        tripCounts.put("MODERATE", 0L);
        tripCounts.put("HIGH_RISK", 0L);

        List<Object[]> tripGroups = riskScoreRepository.countGroupByScoreCategory();
        long scorableTotal = 0L;
        for (Object[] row : tripGroups) {
            String cat = (String) row[0];
            long count = ((Number) row[1]).longValue();
            tripCounts.put(cat, count);
            scorableTotal += count;
        }

        long unratedTotal = riskScoreRepository.countByIsScorableFalse();
        tripCounts.put("UNRATED", unratedTotal);
        long grandTotalTrips = scorableTotal + unratedTotal;

        List<CategoryCountDto> tripCategories = new ArrayList<>();
        for (Map.Entry<String, Long> entry : tripCounts.entrySet()) {
            double pct = grandTotalTrips > 0 ? round1((entry.getValue() * 100.0) / grandTotalTrips) : 0.0;
            tripCategories.add(new CategoryCountDto(entry.getKey(), entry.getValue(), pct));
        }

        dto.setTripCategories(tripCategories);
        dto.setTotalScoredTrips(scorableTotal);
        dto.setTotalUnratedTrips(unratedTotal);

        // 2. Vehicle-level breakdown (using each vehicle's latest scorable risk score)
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        Map<String, Long> vehicleCounts = new LinkedHashMap<>();
        vehicleCounts.put("EXCELLENT", 0L);
        vehicleCounts.put("GOOD", 0L);
        vehicleCounts.put("MODERATE", 0L);
        vehicleCounts.put("HIGH_RISK", 0L);

        long scoredVehiclesCount = 0L;
        for (Vehicle v : allVehicles) {
            List<RiskScore> vScores = riskScoreRepository.findByVehicleIdOrderByCalculatedAtDesc(v.getId());
            for (RiskScore s : vScores) {
                if (Boolean.TRUE.equals(s.getIsScorable()) && s.getScoreCategory() != null) {
                    vehicleCounts.put(s.getScoreCategory(), vehicleCounts.getOrDefault(s.getScoreCategory(), 0L) + 1L);
                    scoredVehiclesCount++;
                    break; // Only the latest scorable per vehicle
                }
            }
        }

        List<CategoryCountDto> vehicleCategories = new ArrayList<>();
        for (Map.Entry<String, Long> entry : vehicleCounts.entrySet()) {
            double pct = scoredVehiclesCount > 0 ? round1((entry.getValue() * 100.0) / scoredVehiclesCount) : 0.0;
            vehicleCategories.add(new CategoryCountDto(entry.getKey(), entry.getValue(), pct));
        }

        dto.setVehicleCategories(vehicleCategories);
        dto.setTotalScoredVehicles(scoredVehiclesCount);

        return dto;
    }

    /**
     * Aggregates fleet-wide driving behavior metrics with distance-weighted rates and anomaly statistics.
     */
    @Transactional(readOnly = true)
    public InsurerDrivingBehaviorDto getInsurerDrivingBehavior() {
        InsurerDrivingBehaviorDto dto = new InsurerDrivingBehaviorDto();

        List<Object[]> sumsList = drivingFeatureRepository.findFleetFeatureSums();
        if (sumsList != null && !sumsList.isEmpty() && sumsList.get(0) != null) {
            Object[] row = sumsList.get(0);
            long harshBraking = ((Number) row[0]).longValue();
            long rapidAccel = ((Number) row[1]).longValue();
            long speedingObs = ((Number) row[2]).longValue();
            long nightMinutes = ((Number) row[3]).longValue();
            long validRecords = ((Number) row[4]).longValue();
            long anomalousRecords = ((Number) row[5]).longValue();
            double distanceKm = ((Number) row[6]).doubleValue();
            long durationSeconds = ((Number) row[7]).longValue();
            long featureCount = ((Number) row[8]).longValue();

            dto.setTotalHarshBrakingEvents(harshBraking);
            dto.setTotalRapidAccelerationEvents(rapidAccel);
            dto.setTotalSpeedingObservations(speedingObs);
            dto.setTotalNightDrivingMinutes(nightMinutes);
            dto.setTotalValidRecords(validRecords);
            dto.setTotalAnomalousRecords(anomalousRecords);
            dto.setTotalDistanceEvaluatedKm(round2(distanceKm));
            dto.setTotalTripsEvaluated(featureCount);

            if (distanceKm > 0.0) {
                dto.setFleetHarshBrakeRatePer100Km(round2((harshBraking / distanceKm) * 100.0));
                dto.setFleetRapidAccelRatePer100Km(round2((rapidAccel / distanceKm) * 100.0));
                dto.setFleetSpeedingObsPer100Km(round2((speedingObs / distanceKm) * 100.0));
            }

            if (durationSeconds > 0) {
                dto.setFleetNightDrivingRatio(round4((double) (nightMinutes * 60) / durationSeconds));
            }

            long totalRecords = validRecords + anomalousRecords;
            if (totalRecords > 0) {
                dto.setFleetAnomalyRatePct(round2(((double) anomalousRecords / totalRecords) * 100.0));
            }
        }

        return dto;
    }

    /**
     * Aggregates observed mileage coverage and vehicle-level usage metrics.
     */
    @Transactional(readOnly = true)
    public InsurerUsageAnalyticsDto getInsurerUsageAnalytics() {
        InsurerUsageAnalyticsDto dto = new InsurerUsageAnalyticsDto();

        Double totalDistance = tripRepository.sumDistanceByStatus(TripStatus.COMPLETED);
        dto.setTotalObservedDistanceKm(round2(totalDistance != null ? totalDistance : 0.0));
        dto.setTotalCompletedTrips(tripRepository.countByStatus(TripStatus.COMPLETED));

        List<Vehicle> allVehicles = vehicleRepository.findAll();
        List<VehicleMileageSummaryDto> summaries = new ArrayList<>();
        long sufficientCount = 0L;
        long insufficientCount = 0L;

        for (Vehicle v : allVehicles) {
            long completed = tripRepository.countByVehicleIdAndStatus(v.getId(), TripStatus.COMPLETED);
            Double dist = tripRepository.sumDistanceByVehicleIdAndStatus(v.getId(), TripStatus.COMPLETED);
            double distKm = round2(dist != null ? dist : 0.0);
            boolean sufficient = (completed >= 1 && distKm >= 5.0);

            if (sufficient) {
                sufficientCount++;
            } else {
                insufficientCount++;
            }

            summaries.add(new VehicleMileageSummaryDto(
                    v.getId(),
                    v.getVehicleNumber(),
                    v.getMake(),
                    v.getModel(),
                    completed,
                    distKm,
                    sufficient
            ));
        }

        dto.setVehiclesWithSufficientUsage(sufficientCount);
        dto.setVehiclesWithInsufficientUsage(insufficientCount);
        dto.setVehicleMileageSummaries(summaries);

        return dto;
    }

    /**
     * Aggregates quote metrics, median and average calculations, risk distribution, and pricing versions.
     */
    @Transactional(readOnly = true)
    public InsurerPricingAnalyticsDto getInsurerPricingAnalytics() {
        InsurerPricingAnalyticsDto dto = new InsurerPricingAnalyticsDto();

        long eligible = premiumQuoteRepository.countByIsEligibleTrue();
        long ineligible = premiumQuoteRepository.countByIsEligibleFalse();
        dto.setEligibleQuotesCount(eligible);
        dto.setIneligibleQuotesCount(ineligible);
        dto.setTotalQuotes(eligible + ineligible);

        List<BigDecimal> sortedPremiums = premiumQuoteRepository.findAllEligiblePremiumsSorted();
        if (!sortedPremiums.isEmpty()) {
            dto.setMinEstimatedPremiumInr(sortedPremiums.get(0));
            dto.setMaxEstimatedPremiumInr(sortedPremiums.get(sortedPremiums.size() - 1));

            BigDecimal sum = sortedPremiums.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setAverageEstimatedPremiumInr(sum.divide(BigDecimal.valueOf(sortedPremiums.size()), 2, RoundingMode.HALF_UP));

            int n = sortedPremiums.size();
            if (n % 2 == 1) {
                dto.setMedianEstimatedPremiumInr(sortedPremiums.get(n / 2));
            } else {
                BigDecimal midSum = sortedPremiums.get(n / 2 - 1).add(sortedPremiums.get(n / 2));
                dto.setMedianEstimatedPremiumInr(midSum.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
            }
        }

        // Quoted risk category distribution
        List<Object[]> quoteCategories = premiumQuoteRepository.countEligibleQuotesGroupByRiskCategory();
        List<CategoryCountDto> quotedDist = new ArrayList<>();
        for (Object[] row : quoteCategories) {
            String category = (String) row[0];
            long count = ((Number) row[1]).longValue();
            double pct = eligible > 0 ? round1((count * 100.0) / eligible) : 0.0;
            quotedDist.add(new CategoryCountDto(category, count, pct));
        }
        dto.setQuotedRiskDistribution(quotedDist);

        // Pricing version distribution
        List<Object[]> versionRows = premiumQuoteRepository.countQuotesGroupByPricingVersion();
        List<PricingVersionCountDto> versionDist = new ArrayList<>();
        for (Object[] row : versionRows) {
            String version = (String) row[0];
            long count = ((Number) row[1]).longValue();
            versionDist.add(new PricingVersionCountDto(version, count));
        }
        dto.setPricingVersionDistribution(versionDist);

        return dto;
    }

    // Helper utilities

    private List<RiskFactorExplanationDto> parseRiskFactors(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<RiskFactorExplanationDto>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse factor explanations JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Double round1(Double val) {
        if (val == null) return null;
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private Double round2(Double val) {
        if (val == null) return 0.0;
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Double round4(Double val) {
        if (val == null) return 0.0;
        return BigDecimal.valueOf(val).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
