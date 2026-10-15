package com.ubi.platform.dto;

import com.ubi.platform.entity.FuelType;
import com.ubi.platform.entity.VehicleType;
import jakarta.validation.constraints.*;

/**
 * Incoming payload to register an insured vehicle.
 */
public class VehicleRegistrationRequest {

    @NotBlank(message = "Vehicle registration number is required")
    @Pattern(regexp = "^[A-Za-z0-9 -]{4,20}$", message = "Vehicle registration number must be 4 to 20 alphanumeric characters")
    private String vehicleNumber;

    @NotBlank(message = "Vehicle manufacturer/make is required")
    @Size(min = 2, max = 60, message = "Manufacturer name must be between 2 and 60 characters")
    private String make;

    @NotBlank(message = "Vehicle model is required")
    @Size(min = 1, max = 60, message = "Vehicle model must be between 1 and 60 characters")
    private String model;

    @NotNull(message = "Manufacturing year is required")
    @Min(value = 1990, message = "Manufacturing year must be 1990 or newer")
    @Max(value = 2027, message = "Manufacturing year cannot be in the future")
    private Integer manufacturingYear;

    @Min(value = 49, message = "Engine capacity must be at least 49 CC if provided")
    @Max(value = 10000, message = "Engine capacity cannot exceed 10,000 CC")
    private Integer engineCapacityCc;

    @NotNull(message = "Fuel type is required (PETROL, DIESEL, ELECTRIC, HYBRID, CNG)")
    private FuelType fuelType;

    @NotNull(message = "Vehicle type is required (CAR, MOTORCYCLE, SUV, COMMERCIAL, OTHER)")
    private VehicleType vehicleType;

    public VehicleRegistrationRequest() {
    }

    public VehicleRegistrationRequest(String vehicleNumber, String make, String model,
                                      Integer manufacturingYear, Integer engineCapacityCc,
                                      FuelType fuelType, VehicleType vehicleType) {
        this.vehicleNumber = vehicleNumber;
        this.make = make;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
        this.engineCapacityCc = engineCapacityCc;
        this.fuelType = fuelType;
        this.vehicleType = vehicleType;
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
}
