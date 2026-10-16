package com.ubi.platform.repository;

import com.ubi.platform.entity.PremiumQuote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for PremiumQuote entities.
 */
@Repository
public interface PremiumQuoteRepository extends JpaRepository<PremiumQuote, Long> {

    /**
     * Finds paginated quote history for a vehicle.
     */
    Page<PremiumQuote> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Finds all quotes for a vehicle in chronological descending order.
     */
    List<PremiumQuote> findByVehicleIdOrderByCalculatedAtDesc(Long vehicleId);

    /**
     * Finds a quote by its unique quote reference string.
     */
    Optional<PremiumQuote> findByQuoteReference(String quoteReference);

    /**
     * Deletes all quotes for a vehicle.
     */
    @Modifying
    @Query("DELETE FROM PremiumQuote q WHERE q.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Count eligible quotes across fleet.
     */
    long countByIsEligibleTrue();

    /**
     * Count ineligible quotes across fleet.
     */
    long countByIsEligibleFalse();

    /**
     * Distinct vehicles that have at least one eligible quote.
     */
    @Query("SELECT DISTINCT q.vehicle.id FROM PremiumQuote q WHERE q.isEligible = true")
    List<Long> findDistinctVehicleIdsWithEligibleQuotes();

    /**
     * All eligible final annual premiums sorted ascending for median and distribution calculation.
     */
    @Query("SELECT q.finalAnnualPremiumInr FROM PremiumQuote q WHERE q.isEligible = true AND q.finalAnnualPremiumInr IS NOT NULL ORDER BY q.finalAnnualPremiumInr ASC")
    List<java.math.BigDecimal> findAllEligiblePremiumsSorted();

    /**
     * Count eligible quotes grouped by risk category.
     */
    @Query("SELECT q.riskCategory, COUNT(q) FROM PremiumQuote q WHERE q.isEligible = true GROUP BY q.riskCategory")
    List<Object[]> countEligibleQuotesGroupByRiskCategory();

    /**
     * Count quotes grouped by pricing version.
     */
    @Query("SELECT q.pricingVersion, COUNT(q) FROM PremiumQuote q GROUP BY q.pricingVersion")
    List<Object[]> countQuotesGroupByPricingVersion();

    /**
     * Latest quote for a vehicle.
     */
    Optional<PremiumQuote> findTopByVehicleIdOrderByCalculatedAtDesc(Long vehicleId);
}
