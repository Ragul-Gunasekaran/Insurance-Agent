package com.ubi.platform.controller;

import com.ubi.platform.dto.*;
import com.ubi.platform.exception.GlobalExceptionHandler;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.service.PricingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PricingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PricingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PricingService pricingService;

    private PremiumQuoteResponseDto createSampleQuoteDto() {
        PremiumQuoteResponseDto dto = new PremiumQuoteResponseDto();
        dto.setId(701L);
        dto.setQuoteReference("QTE-20261015-SAMPLE");
        dto.setVehicleId(1L);
        dto.setVehicleRegistrationNumber("MH02AB1234");
        dto.setVehicleMake("Honda");
        dto.setVehicleModel("City");
        dto.setStatus("READY");
        dto.setIsEligible(true);
        dto.setBaseAnnualPremiumInr(new BigDecimal("12000.00"));
        dto.setSafetyScore(88.0);
        dto.setRiskCategory("EXCELLENT");
        dto.setRiskMultiplier(new BigDecimal("0.9000"));
        dto.setObservedDistanceKm(45.0);
        dto.setObservationDays(14);
        dto.setProjectedAnnualDistanceKm(10000.0);
        dto.setAnnualDistanceSource("USER_SPECIFIED");
        dto.setUsageMultiplier(new BigDecimal("1.0000"));
        dto.setRawAnnualPremiumInr(new BigDecimal("10800.00"));
        dto.setFinalAnnualPremiumInr(new BigDecimal("10800.00"));
        dto.setCurrency("INR");
        dto.setPricingVersion("pricing-v1");
        dto.setCalculatedAt(Instant.now());
        dto.setExcludedTripsCount(0);
        dto.setSummaryExplanation("Estimated annual premium of ₹10,800.00 INR.");

        PricingFactorExplanationDto f1 = new PricingFactorExplanationDto(
                "RISK_MULTIPLIER", "Driving Safety Risk Factor", "0.9000", "APPLIED", "10.0% safe driver discount"
        );
        dto.setFactorExplanations(List.of(f1));
        return dto;
    }

    @Test
    @DisplayName("POST /api/v1/pricing/vehicles/{vehicleId}/quotes returns 200 with quote")
    void testCalculateQuoteForVehicle_Returns200() throws Exception {
        PremiumQuoteResponseDto dto = createSampleQuoteDto();
        when(pricingService.calculateQuoteForVehicle(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/pricing/vehicles/1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"annualDistanceKmAssumption\": 10000.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quoteReference").value("QTE-20261015-SAMPLE"))
                .andExpect(jsonPath("$.data.finalAnnualPremiumInr").value(10800.00))
                .andExpect(jsonPath("$.data.currency").value("INR"))
                .andExpect(jsonPath("$.data.riskCategory").value("EXCELLENT"));
    }

    @Test
    @DisplayName("GET /api/v1/pricing/quotes/{quoteId} returns 200 with quote")
    void testGetQuoteById_Returns200() throws Exception {
        PremiumQuoteResponseDto dto = createSampleQuoteDto();
        when(pricingService.getQuoteById(701L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/pricing/quotes/701"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(701))
                .andExpect(jsonPath("$.data.finalAnnualPremiumInr").value(10800.00));
    }

    @Test
    @DisplayName("GET /api/v1/pricing/quotes/{quoteId} returns 404 when quote not found")
    void testGetQuoteById_Returns404WhenNotFound() throws Exception {
        when(pricingService.getQuoteById(999L))
                .thenThrow(new ResourceNotFoundException("Premium quote not found with ID: 999"));

        mockMvc.perform(get("/api/v1/pricing/quotes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/pricing/vehicles/{vehicleId}/quotes returns paginated quote history")
    void testGetQuotesForVehicle_Returns200() throws Exception {
        PremiumQuoteResponseDto dto = createSampleQuoteDto();
        PageImpl<PremiumQuoteResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(pricingService.getQuotesForVehicle(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/pricing/vehicles/1/quotes?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].quoteReference").value("QTE-20261015-SAMPLE"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/pricing/configuration returns safe non-sensitive configuration")
    void testGetPricingConfiguration_Returns200() throws Exception {
        Map<String, BigDecimal> multipliers = new LinkedHashMap<>();
        multipliers.put("EXCELLENT", new BigDecimal("0.9000"));
        multipliers.put("GOOD", new BigDecimal("1.0000"));

        PricingConfigurationResponseDto config = new PricingConfigurationResponseDto(
                new BigDecimal("12000.00"),
                new BigDecimal("5000.00"),
                new BigDecimal("35000.00"),
                multipliers,
                10000.0,
                7,
                20.0,
                "pricing-v1",
                "INR",
                "Illustrative prototype disclaimer"
        );
        when(pricingService.getPricingConfiguration()).thenReturn(config);

        mockMvc.perform(get("/api/v1/pricing/configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.baseAnnualPremiumInr").value(12000.00))
                .andExpect(jsonPath("$.data.currency").value("INR"))
                .andExpect(jsonPath("$.data.pricingVersion").value("pricing-v1"));
    }
}
