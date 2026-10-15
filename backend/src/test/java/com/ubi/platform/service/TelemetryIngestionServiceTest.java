package com.ubi.platform.service;

import com.ubi.platform.dto.SimulateTelemetryRequest;
import com.ubi.platform.dto.TelematicsBatchIngestRequest;
import com.ubi.platform.dto.TelematicsBatchIngestResponse;
import com.ubi.platform.dto.TelematicsPointRequest;
import com.ubi.platform.dto.TelematicsSummaryResponse;
import com.ubi.platform.entity.FuelType;
import com.ubi.platform.entity.TelematicsRecord;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.entity.VehicleType;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.TelematicsRepository;
import com.ubi.platform.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryIngestionServiceTest {

    @Mock
    private TelematicsRepository telematicsRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Spy
    private TelemetryValidationService validationService = new TelemetryValidationService();

    @Spy
    private TelemetrySimulationService simulationService = new TelemetrySimulationService();

    @InjectMocks
    private TelemetryIngestionService ingestionService;

    private Vehicle mockVehicle;

    @BeforeEach
    void setUp() {
        mockVehicle = new Vehicle("KA01AB1234", "Toyota", "Innova", 2022, 2393, FuelType.DIESEL, VehicleType.SUV);
        mockVehicle.setId(10L);
    }

    @Test
    @DisplayName("Should successfully ingest batch of telematics points for registered vehicle")
    void testIngestBatchSuccess() {
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(mockVehicle));
        when(telematicsRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        TelematicsPointRequest p1 = new TelematicsPointRequest(10L, Instant.now(), 12.9716, 77.5946, 40.0, 0.1, 90.0, 1500, 80.0);
        TelematicsPointRequest p2 = new TelematicsPointRequest(10L, Instant.now(), 12.9720, 77.5950, 45.0, -0.05, 92.0, 1600, 79.9);

        TelematicsBatchIngestRequest request = new TelematicsBatchIngestRequest(10L, List.of(p1, p2));
        TelematicsBatchIngestResponse response = ingestionService.ingestBatch(request);

        assertThat(response.getVehicleId()).isEqualTo(10L);
        assertThat(response.getTotalIngested()).isEqualTo(2);
        assertThat(response.getValidPoints()).isEqualTo(2);
        assertThat(response.getAnomalousPoints()).isEqualTo(0);
        verify(telematicsRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ingesting for unknown vehicle")
    void testIngestUnknownVehicle() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        TelematicsPointRequest p1 = new TelematicsPointRequest(999L, Instant.now(), 12.9716, 77.5946, 40.0, 0.1, 90.0, 1500, 80.0);
        TelematicsBatchIngestRequest request = new TelematicsBatchIngestRequest(999L, List.of(p1));

        assertThatThrownBy(() -> ingestionService.ingestBatch(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cannot ingest telematics for unknown vehicle ID: 999");
    }

    @Test
    @DisplayName("Should simulate and ingest telemetry route")
    void testSimulateAndIngest() {
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(mockVehicle));
        when(telematicsRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        SimulateTelemetryRequest request = new SimulateTelemetryRequest(10L, "SAFE_COMMUTE", 10);
        TelematicsBatchIngestResponse response = ingestionService.simulateAndIngest(request);

        assertThat(response.getVehicleId()).isEqualTo(10L);
        assertThat(response.getTotalIngested()).isEqualTo(10);
        assertThat(response.getValidPoints()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should compute telemetry statistical summary and actuarial risk counts")
    void testGetVehicleTelemetrySummary() {
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(mockVehicle));
        when(telematicsRepository.countByVehicleId(10L)).thenReturn(25L);
        when(telematicsRepository.countByVehicleIdAndValidationStatus(eq(10L), any())).thenReturn(24L);

        when(telematicsRepository.findMaxSpeedByVehicleId(10L)).thenReturn(115.5);
        when(telematicsRepository.findAvgSpeedByVehicleId(10L)).thenReturn(52.3);
        when(telematicsRepository.findMaxBrakingGByVehicleId(10L)).thenReturn(-0.48);
        when(telematicsRepository.findMaxAccelGByVehicleId(10L)).thenReturn(0.38);

        when(telematicsRepository.countHarshBrakesByVehicleId(eq(10L), anyDouble())).thenReturn(3L);
        when(telematicsRepository.countRapidAccelerationsByVehicleId(eq(10L), anyDouble())).thenReturn(2L);
        when(telematicsRepository.countSpeedingEventsByVehicleId(eq(10L), anyDouble())).thenReturn(0L);

        TelematicsSummaryResponse summary = ingestionService.getVehicleTelemetrySummary(10L);

        assertThat(summary.getVehicleId()).isEqualTo(10L);
        assertThat(summary.getTotalPoints()).isEqualTo(25L);
        assertThat(summary.getValidPoints()).isEqualTo(24L);
        assertThat(summary.getAnomalousPoints()).isEqualTo(1L);
        assertThat(summary.getMaxSpeedKmh()).isEqualTo(115.5);
        assertThat(summary.getHarshBrakingEvents()).isEqualTo(3L);
        assertThat(summary.getRapidAccelerationEvents()).isEqualTo(2L);
    }
}
