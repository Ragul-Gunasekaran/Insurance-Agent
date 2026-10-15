package com.ubi.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.*;
import com.ubi.platform.entity.FuelType;
import com.ubi.platform.entity.VehicleType;
import com.ubi.platform.entity.VerificationStatus;
import com.ubi.platform.exception.DuplicateResourceException;
import com.ubi.platform.exception.GlobalExceptionHandler;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@Import(GlobalExceptionHandler.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    @Test
    @DisplayName("POST /api/v1/vehicles with valid request returns 201 Created")
    void registerVehicle_Valid_Returns201() throws Exception {
        VehicleRegistrationRequest request = new VehicleRegistrationRequest(
                "MH02AB1234", "Honda", "City", 2022, 1498, FuelType.PETROL, VehicleType.CAR
        );

        VehicleResponse mockResponse = new VehicleResponse();
        mockResponse.setId(10L);
        mockResponse.setVehicleNumber("MH02AB1234");
        mockResponse.setMake("Honda");
        mockResponse.setModel("City");
        mockResponse.setManufacturingYear(2022);
        mockResponse.setEngineCapacityCc(1498);
        mockResponse.setFuelType(FuelType.PETROL);
        mockResponse.setVehicleType(VehicleType.CAR);
        mockResponse.setVerificationStatus(VerificationStatus.UNVERIFIED);
        mockResponse.setCreatedAt(Instant.now());

        when(vehicleService.registerVehicle(any(VehicleRegistrationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.vehicleNumber").value("MH02AB1234"))
                .andExpect(jsonPath("$.data.verificationStatus").value("UNVERIFIED"));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles with missing required fields returns 400 Bad Request")
    void registerVehicle_InvalidPayload_Returns400() throws Exception {
        VehicleRegistrationRequest invalidRequest = new VehicleRegistrationRequest();
        // Missing vehicleNumber, make, model, manufacturingYear, etc.

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles with duplicate vehicle number returns 409 Conflict")
    void registerVehicle_Duplicate_Returns409() throws Exception {
        VehicleRegistrationRequest request = new VehicleRegistrationRequest(
                "MH02AB1234", "Honda", "City", 2022, 1498, FuelType.PETROL, VehicleType.CAR
        );

        when(vehicleService.registerVehicle(any(VehicleRegistrationRequest.class)))
                .thenThrow(new DuplicateResourceException("Vehicle with registration number 'MH02AB1234' is already registered."));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.error.message").value("Vehicle with registration number 'MH02AB1234' is already registered."));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} found returns 200 OK")
    void getVehicleById_Found_Returns200() throws Exception {
        VehicleResponse mockResponse = new VehicleResponse();
        mockResponse.setId(1L);
        mockResponse.setVehicleNumber("KA01CD5678");
        mockResponse.setMake("Toyota");

        when(vehicleService.getVehicleById(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.vehicleNumber").value("KA01CD5678"));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} nonexistent returns 404 Not Found")
    void getVehicleById_NotFound_Returns404() throws Exception {
        when(vehicleService.getVehicleById(999L))
                .thenThrow(new ResourceNotFoundException("Vehicle not found with ID: 999"));

        mockMvc.perform(get("/api/v1/vehicles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles returns paginated list with 200 OK")
    void getAllVehicles_Returns200() throws Exception {
        VehicleResponse v = new VehicleResponse();
        v.setId(1L);
        v.setVehicleNumber("DL03EF9012");

        PageResponse<VehicleResponse> pageResponse = new PageResponse<>(
                List.of(v), 0, 10, 1, 1, true, true
        );

        when(vehicleService.getAllVehicles(any(Pageable.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/vehicles?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/vehicles/verify returns 200 OK with verification result")
    void verifyVehicle_Returns200() throws Exception {
        VehicleVerificationRequest request = new VehicleVerificationRequest(
                1L, "MH02AB1234", "Honda", "City", 2021
        );

        VehicleVerificationResponse mockResponse = new VehicleVerificationResponse(
                1L, "MH02AB1234", VerificationStatus.VERIFIED, true,
                "Mock Verification: Vehicle record verified successfully.",
                Map.of("registryLookup", "EXACT_MATCH")
        );

        when(vehicleService.verifyVehicle(any(VehicleVerificationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/vehicles/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.data.simulated").value(true));
    }
}
