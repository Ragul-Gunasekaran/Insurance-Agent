package com.ubi.platform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Inbound DTO representing a batch of IoT telematics readings.
 */
public class TelematicsBatchIngestRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotEmpty(message = "Points list cannot be empty")
    @Valid
    private List<TelematicsPointRequest> points;

    public TelematicsBatchIngestRequest() {
    }

    public TelematicsBatchIngestRequest(Long vehicleId, List<TelematicsPointRequest> points) {
        this.vehicleId = vehicleId;
        this.points = points;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public List<TelematicsPointRequest> getPoints() {
        return points;
    }

    public void setPoints(List<TelematicsPointRequest> points) {
        this.points = points;
    }
}
