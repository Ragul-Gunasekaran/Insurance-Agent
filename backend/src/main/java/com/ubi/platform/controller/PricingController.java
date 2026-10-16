package com.ubi.platform.controller;

import com.ubi.platform.dto.*;
import com.ubi.platform.service.PricingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing Actuarial Pricing Engine endpoints (Step 6).
 * Computes illustrative, explainable UBI premium quotes in INR (₹) based on driving behavior and mileage.
 */
@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    /**
     * Calculates and persists a new actuarial premium quote snapshot for a vehicle.
     */
    @PostMapping("/vehicles/{vehicleId}/quotes")
    public ResponseEntity<ApiResponse<PremiumQuoteResponseDto>> calculateQuoteForVehicle(
            @PathVariable Long vehicleId,
            @RequestBody(required = false) PricingCalculationRequestDto request) {
        PremiumQuoteResponseDto quote = pricingService.calculateQuoteForVehicle(vehicleId, request);
        return ResponseEntity.ok(ApiResponse.ok(quote));
    }

    /**
     * Retrieves an existing quote snapshot by its primary database ID.
     */
    @GetMapping("/quotes/{quoteId}")
    public ResponseEntity<ApiResponse<PremiumQuoteResponseDto>> getQuoteById(@PathVariable Long quoteId) {
        PremiumQuoteResponseDto quote = pricingService.getQuoteById(quoteId);
        return ResponseEntity.ok(ApiResponse.ok(quote));
    }

    /**
     * Retrieves paginated quote history for a vehicle.
     */
    @GetMapping("/vehicles/{vehicleId}/quotes")
    public ResponseEntity<ApiResponse<PageResponse<PremiumQuoteResponseDto>>> getQuotesForVehicle(
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
        Page<PremiumQuoteResponseDto> quotePage = pricingService.getQuotesForVehicle(vehicleId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.fromPage(quotePage, quotePage.getContent())));
    }

    /**
     * Exposes safe, non-sensitive prototype pricing assumptions and multipliers.
     */
    @GetMapping("/configuration")
    public ResponseEntity<ApiResponse<PricingConfigurationResponseDto>> getPricingConfiguration() {
        PricingConfigurationResponseDto config = pricingService.getPricingConfiguration();
        return ResponseEntity.ok(ApiResponse.ok(config));
    }
}
