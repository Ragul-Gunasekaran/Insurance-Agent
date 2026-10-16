package com.ubi.platform.dto;

/**
 * Observed usage and trip metrics per vehicle for usage analytics.
 */
public class VehicleMileageSummaryDto {

    private Long vehicleId;
    private String vehicleNumber;
    private String make;
    private String model;
    private Long completedTrips = 0L;
    private Double observedDistanceKm = 0.0;
    private Boolean hasSufficientUsage = false;

    public VehicleMileageSummaryDto() {
    }

    public VehicleMileageSummaryDto(Long vehicleId, String vehicleNumber, String make, String model,
                                   Long completedTrips, Double observedDistanceKm, Boolean hasSufficientUsage) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.make = make;
        this.model = model;
        this.completedTrips = completedTrips != null ? completedTrips : 0L;
        this.observedDistanceKm = observedDistanceKm != null ? observedDistanceKm : 0.0;
        this.hasSufficientUsage = hasSufficientUsage != null ? hasSufficientUsage : false;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Long getCompletedTrips() {
        return completedTrips;
    }

    public void setCompletedTrips(Long completedTrips) {
        this.completedTrips = completedTrips;
    }

    public Double getObservedDistanceKm() {
        return observedDistanceKm;
    }

    public void setObservedDistanceKm(Double observedDistanceKm) {
        this.observedDistanceKm = observedDistanceKm;
    }

    public Boolean getHasSufficientUsage() {
        return hasSufficientUsage;
    }

    public void setHasSufficientUsage(Boolean hasSufficientUsage) {
        this.hasSufficientUsage = hasSufficientUsage;
    }
}
