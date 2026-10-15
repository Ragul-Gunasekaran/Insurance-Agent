package com.ubi.platform.dto;

import com.ubi.platform.entity.FuelType;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.entity.VehicleType;
import com.ubi.platform.entity.VerificationStatus;

import java.time.Instant;

/**
 * Outbound DTO representing an onboarded vehicle.
 * Ensures JPA entities are not exposed directly over the REST API boundary.
 */
public class VehicleResponse {

    private Long id;
    private String vehicleNumber;
    private String make;
    private String model;
    private Integer manufacturingYear;
    private Integer engineCapacityCc;
    private FuelType fuelType;
    private VehicleType vehicleType;
    private VerificationStatus verificationStatus;
    private String verificationNotes;
    private Instant createdAt;

    public VehicleResponse() {
    }

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        VehicleResponse dto = new VehicleResponse();
        dto.setId(vehicle.getId());
        dto.setVehicleNumber(vehicle.getVehicleNumber());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        dto.setManufacturingYear(vehicle.getManufacturingYear());
        dto.setEngineCapacityCc(vehicle.getEngineCapacityCc());
        dto.setFuelType(vehicle.getFuelType());
        dto.setVehicleType(vehicle.getVehicleType());
        dto.setVerificationStatus(vehicle.getVerificationStatus());
        dto.setVerificationNotes(vehicle.getVerificationNotes());
        dto.setCreatedAt(vehicle.getCreatedAt());
        return dto;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getEngineCapacityCc() {
        return engineCapacityCc;
    }

    public void setEngineCapacityCc(Integer engineCapacityCc) {
        this.engineCapacityCc = engineCapacityCc;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
