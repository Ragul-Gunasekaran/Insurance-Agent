package com.ubi.platform.controller;

import com.ubi.platform.dto.DrivingFeatureResponseDto;
import com.ubi.platform.exception.GlobalExceptionHandler;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.service.FeatureEngineeringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeatureController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FeatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeatureEngineeringService featureEngineeringService;

    @Test
    @DisplayName("POST /api/v1/features/trips/{tripId}/calculate returns 200 with calculated features")
    void testCalculateFeaturesForTrip_Returns200() throws Exception {
        DrivingFeatureResponseDto dto = new DrivingFeatureResponseDto();
        dto.setId(201L);
        dto.setTripId(101L);
        dto.setDistanceKm(14.8);
        dto.setAverageSpeedKmh(48.5);
        dto.setMaxSpeedKmh(78.0);
        dto.setHarshBrakingCount(1);
        dto.setRapidAccelerationCount(2);

        when(featureEngineeringService.calculateFeaturesForTrip(101L)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/features/trips/101/calculate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(101))
                .andExpect(jsonPath("$.data.distanceKm").value(14.8))
                .andExpect(jsonPath("$.data.harshBrakingCount").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/features/trips/{tripId} returns stored features")
    void testGetFeaturesForTrip_Returns200() throws Exception {
        DrivingFeatureResponseDto dto = new DrivingFeatureResponseDto();
        dto.setId(201L);
        dto.setTripId(101L);
        dto.setDistanceKm(14.8);

        when(featureEngineeringService.getFeaturesForTrip(101L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/features/trips/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(101));
    }

    @Test
    @DisplayName("GET /api/v1/features/trips/{tripId} returns 404 for trip without features")
    void testGetFeaturesForTrip_NotFound_Returns404() throws Exception {
        when(featureEngineeringService.getFeaturesForTrip(999L))
                .thenThrow(new ResourceNotFoundException("No features calculated yet for trip ID: 999"));

        mockMvc.perform(get("/api/v1/features/trips/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/features/vehicles/{vehicleId}/calculate returns 200 with list of features")
    void testCalculateFeaturesForVehicle_Returns200() throws Exception {
        DrivingFeatureResponseDto dto = new DrivingFeatureResponseDto();
        dto.setId(201L);
        dto.setTripId(101L);

        when(featureEngineeringService.calculateFeaturesForVehicle(1L)).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/v1/features/vehicles/1/calculate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(201));
    }
}
