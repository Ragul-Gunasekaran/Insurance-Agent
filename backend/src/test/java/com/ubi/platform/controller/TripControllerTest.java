package com.ubi.platform.controller;

import com.ubi.platform.dto.TripResponseDto;
import com.ubi.platform.entity.TripStatus;
import com.ubi.platform.exception.GlobalExceptionHandler;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.service.TripStitchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TripController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripStitchingService tripStitchingService;

    @Test
    @DisplayName("POST /api/v1/trips/stitch/{vehicleId} returns 200 OK with stitched trips")
    void testStitchTrips_Returns200() throws Exception {
        TripResponseDto trip = new TripResponseDto();
        trip.setId(101L);
        trip.setVehicleId(1L);
        trip.setVehicleNumber("MH02AB1234");
        trip.setStartTime(Instant.now());
        trip.setStatus(TripStatus.COMPLETED);
        trip.setDistanceKm(12.5);

        when(tripStitchingService.stitchTripsForVehicle(1L)).thenReturn(List.of(trip));

        mockMvc.perform(post("/api/v1/trips/stitch/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].distanceKm").value(12.5));
    }

    @Test
    @DisplayName("GET /api/v1/trips/vehicles/{vehicleId} returns paginated trips")
    void testGetTripsForVehicle_Returns200() throws Exception {
        TripResponseDto trip = new TripResponseDto();
        trip.setId(101L);
        trip.setVehicleId(1L);

        when(tripStitchingService.getTripsForVehicle(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(trip)));

        mockMvc.perform(get("/api/v1/trips/vehicles/1?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(101));
    }

    @Test
    @DisplayName("GET /api/v1/trips/{tripId} returns trip details")
    void testGetTripById_Returns200() throws Exception {
        TripResponseDto trip = new TripResponseDto();
        trip.setId(101L);
        trip.setDistanceKm(15.2);

        when(tripStitchingService.getTripById(101L)).thenReturn(trip);

        mockMvc.perform(get("/api/v1/trips/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(101));
    }

    @Test
    @DisplayName("GET /api/v1/trips/{tripId} returns 404 for unknown trip")
    void testGetTripById_NotFound_Returns404() throws Exception {
        when(tripStitchingService.getTripById(999L))
                .thenThrow(new ResourceNotFoundException("Trip not found with ID: 999"));

        mockMvc.perform(get("/api/v1/trips/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }
}
