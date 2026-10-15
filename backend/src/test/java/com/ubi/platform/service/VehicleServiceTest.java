package com.ubi.platform.service;

import com.ubi.platform.dto.VehicleRegistrationRequest;
import com.ubi.platform.dto.VehicleResponse;
import com.ubi.platform.entity.FuelType;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.entity.VehicleType;
import com.ubi.platform.entity.VerificationStatus;
import com.ubi.platform.exception.DuplicateResourceException;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleVerificationService verificationService;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleRegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new VehicleRegistrationRequest(
                "MH-02-AB-1234",
                "Honda",
                "City",
                2022,
                1498,
                FuelType.PETROL,
                VehicleType.CAR
        );
    }

    @Test
    @DisplayName("Register vehicle succeeds with normalized registration number")
    void registerVehicle_Success() {
        when(verificationService.normalizeVehicleNumber("MH-02-AB-1234")).thenReturn("MH02AB1234");
        when(vehicleRepository.existsByVehicleNumber("MH02AB1234")).thenReturn(false);

        Vehicle savedVehicle = new Vehicle(
                "MH02AB1234", "Honda", "City", 2022, 1498, FuelType.PETROL, VehicleType.CAR
        );
        savedVehicle.setId(1L);
        savedVehicle.setVerificationStatus(VerificationStatus.UNVERIFIED);

        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleResponse response = vehicleService.registerVehicle(validRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("MH02AB1234", response.getVehicleNumber());
        assertEquals("Honda", response.getMake());
        assertEquals(VerificationStatus.UNVERIFIED, response.getVerificationStatus());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Register vehicle throws DuplicateResourceException if vehicle number exists")
    void registerVehicle_Duplicate_ThrowsException() {
        when(verificationService.normalizeVehicleNumber("MH-02-AB-1234")).thenReturn("MH02AB1234");
        when(vehicleRepository.existsByVehicleNumber("MH02AB1234")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.registerVehicle(validRequest));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Get vehicle by ID succeeds when vehicle exists")
    void getVehicleById_Success() {
        Vehicle vehicle = new Vehicle("DL01AB9999", "Tata", "Nexon", 2023, 1199, FuelType.PETROL, VehicleType.SUV);
        vehicle.setId(5L);
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getVehicleById(5L);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("Tata", response.getMake());
    }

    @Test
    @DisplayName("Get vehicle by ID throws ResourceNotFoundException when not found")
    void getVehicleById_NotFound_ThrowsException() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.getVehicleById(999L));
    }

    @Test
    @DisplayName("Get all vehicles returns paginated list")
    void getAllVehicles_ReturnsPage() {
        Vehicle v1 = new Vehicle("MH01A1111", "Hyundai", "i20", 2021, 1197, FuelType.PETROL, VehicleType.CAR);
        v1.setId(1L);
        Page<Vehicle> page = new PageImpl<>(List.of(v1), PageRequest.of(0, 10), 1);

        when(vehicleRepository.findAll(any(PageRequest.class))).thenReturn(page);

        var result = vehicleService.getAllVehicles(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("MH01A1111", result.getContent().get(0).getVehicleNumber());
    }
}
