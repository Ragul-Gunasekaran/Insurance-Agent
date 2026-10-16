package com.ubi.platform.controller;

import com.ubi.platform.dto.RiskFactorExplanationDto;
import com.ubi.platform.dto.RiskScoreResponseDto;
import com.ubi.platform.exception.GlobalExceptionHandler;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.service.RiskScoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskScoringController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RiskScoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiskScoringService riskScoringService;

    private RiskScoreResponseDto createSampleDto() {
        RiskScoreResponseDto dto = new RiskScoreResponseDto();
        dto.setId(301L);
        dto.setTripId(101L);
        dto.setVehicleId(1L);
        dto.setVehicleRegistrationNumber("MH02AB1234");
        dto.setScore(85.5);
        dto.setScoreCategory("EXCELLENT");
        dto.setScoringVersion("v1.0");
        dto.setCalculatedAt(Instant.now());
        dto.setDataQualityStatus("SUFFICIENT");
        dto.setIsScorable(true);
        dto.setHarshBrakingPenalty(6.0);
        dto.setRapidAccelerationPenalty(4.5);
        dto.setSpeedingPenalty(4.0);
        dto.setNightDrivingPenalty(0.0);
        dto.setTotalPenalties(14.5);
        dto.setSummaryExplanation("Safety score of 85.5/100 (EXCELLENT).");

        RiskFactorExplanationDto f1 = new RiskFactorExplanationDto(
                "HARSH_BRAKING", 3.0, "events/100km", 6.0, 30.0, true, "Observed 3.0 harsh brakes"
        );
        dto.setFactorExplanations(List.of(f1));
        return dto;
    }

    @Test
    @DisplayName("POST /api/v1/risk-scores/trips/{tripId}/calculate returns 200 with calculated score")
    void testCalculateScoreForTrip_Returns200() throws Exception {
        RiskScoreResponseDto dto = createSampleDto();
        when(riskScoringService.calculateScoreForTrip(101L)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/risk-scores/trips/101/calculate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(101))
                .andExpect(jsonPath("$.data.score").value(85.5))
                .andExpect(jsonPath("$.data.scoreCategory").value("EXCELLENT"))
                .andExpect(jsonPath("$.data.totalPenalties").value(14.5))
                .andExpect(jsonPath("$.data.factorExplanations[0].factorName").value("HARSH_BRAKING"));
    }

    @Test
    @DisplayName("GET /api/v1/risk-scores/trips/{tripId} returns 200 with stored score")
    void testGetScoreForTrip_Returns200() throws Exception {
        RiskScoreResponseDto dto = createSampleDto();
        when(riskScoringService.getScoreForTrip(101L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/risk-scores/trips/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(101))
                .andExpect(jsonPath("$.data.score").value(85.5));
    }

    @Test
    @DisplayName("GET /api/v1/risk-scores/trips/{tripId} returns 404 when score not found")
    void testGetScoreForTrip_Returns404WhenNotFound() throws Exception {
        when(riskScoringService.getScoreForTrip(999L))
                .thenThrow(new ResourceNotFoundException("No risk score calculated yet for trip ID: 999"));

        mockMvc.perform(get("/api/v1/risk-scores/trips/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/risk-scores/vehicles/{vehicleId}/calculate returns 200 with batch scores")
    void testCalculateScoresForVehicle_Returns200() throws Exception {
        RiskScoreResponseDto dto = createSampleDto();
        when(riskScoringService.calculateScoresForVehicle(1L)).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/v1/risk-scores/vehicles/1/calculate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].tripId").value(101));
    }

    @Test
    @DisplayName("GET /api/v1/risk-scores/vehicles/{vehicleId} returns paginated risk score history")
    void testGetScoresForVehicle_Returns200() throws Exception {
        RiskScoreResponseDto dto = createSampleDto();
        PageImpl<RiskScoreResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(riskScoringService.getScoresForVehicle(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/risk-scores/vehicles/1?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].score").value(85.5))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
