package com.ubi.platform.dto;

/**
 * Request payload for vehicle verification.
 * Accepts either an existing vehicle ID or raw vehicle attributes for pre-registration verification.
 */
public class VehicleVerificationRequest {

    private Long vehicleId;
    private String vehicleNumber;
    private String make;
    private String model;
    private Integer manufacturingYear;

    public VehicleVerificationRequest() {
    }

    public VehicleVerificationRequest(Long vehicleId, String vehicleNumber, String make, String model, Integer manufacturingYear) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.make = make;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
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

    public Integer getManufacturingYear() {
        return manufacturingYear;
    }

    public void setManufacturingYear(Integer manufacturingYear) {
        this.manufacturingYear = manufacturingYear;
    }
}
