package com.ubi.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.*;
import com.ubi.platform.entity.TelematicsValidationStatus;
import com.ubi.platform.exception.GlobalExceptionHandler;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.service.TelemetryIngestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TelemetryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TelemetryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TelemetryIngestionService telemetryService;

    @Test
    @DisplayName("POST /api/v1/telemetry/ingest returns 201 Created on valid batch")
    void testIngestBatch_Returns201() throws Exception {
        TelematicsPointRequest p1 = new TelematicsPointRequest(
                1L, Instant.now(), 12.9716, 77.5946, 50.0, 0.1, 90.0, 1800, 80.0
        );
        TelematicsBatchIngestRequest request = new TelematicsBatchIngestRequest(1L, List.of(p1));

        TelematicsBatchIngestResponse response = new TelematicsBatchIngestResponse(
                1L, "KA01AB1234", 1, 1, 0, "Ingested successfully", List.of()
        );

        when(telemetryService.ingestBatch(any(TelematicsBatchIngestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/telemetry/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicleId").value(1))
                .andExpect(jsonPath("$.data.totalIngested").value(1))
                .andExpect(jsonPath("$.data.validPoints").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/telemetry/simulate returns 201 Created with synthetic batch")
    void testSimulate_Returns201() throws Exception {
        SimulateTelemetryRequest request = new SimulateTelemetryRequest(1L, "SAFE_COMMUTE", 10);

        TelematicsBatchIngestResponse response = new TelematicsBatchIngestResponse(
                1L, "KA01AB1234", 10, 10, 0, "Simulation ingested", List.of()
        );

        when(telemetryService.simulateAndIngest(any(SimulateTelemetryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/telemetry/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIngested").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/telemetry/vehicles/{vehicleId}/summary returns 200 with metrics")
    void testGetSummary_Returns200() throws Exception {
        TelematicsSummaryResponse summary = new TelematicsSummaryResponse();
        summary.setVehicleId(1L);
        summary.setVehicleNumber("KA01AB1234");
        summary.setTotalPoints(45L);
        summary.setValidPoints(43L);
        summary.setAnomalousPoints(2L);
        summary.setMaxSpeedKmh(112.5);
        summary.setAverageSpeedKmh(54.0);
        summary.setMaxDecelerationG(-0.48);
        summary.setMaxAccelerationG(0.35);
        summary.setHarshBrakingEvents(2L);

        when(telemetryService.getVehicleTelemetrySummary(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/telemetry/vehicles/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPoints").value(45))
                .andExpect(jsonPath("$.data.maxSpeedKmh").value(112.5))
                .andExpect(jsonPath("$.data.harshBrakingEvents").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/telemetry/vehicles/{vehicleId}/summary returns 404 for unknown vehicle")
    void testGetSummary_NotFound_Returns404() throws Exception {
        when(telemetryService.getVehicleTelemetrySummary(999L))
                .thenThrow(new ResourceNotFoundException("Vehicle not found with ID: 999"));

        mockMvc.perform(get("/api/v1/telemetry/vehicles/999/summary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /api/v1/telemetry/vehicles/{vehicleId} returns 200 OK")
    void testClearTelemetry_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/telemetry/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
