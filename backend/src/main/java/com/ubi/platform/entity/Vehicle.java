package com.ubi.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity representing an insured vehicle onboarded into the UBI platform.
 */
@Entity
@Table(
    name = "vehicles",
    indexes = {
        @Index(name = "idx_vehicles_vehicle_number", columnList = "vehicleNumber", unique = true),
        @Index(name = "idx_vehicles_owner", columnList = "owner_id")
    }
)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 32)
    private String vehicleNumber;

    @Column(name = "make", nullable = false, length = 64)
    private String make;

    @Column(name = "model", nullable = false, length = 64)
    private String model;

    @Column(name = "manufacturing_year", nullable = false)
    private Integer manufacturingYear;

    @Column(name = "engine_capacity_cc")
    private Integer engineCapacityCc;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 32)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 32)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 32)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "verification_notes", length = 512)
    private String verificationNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Vehicle() {
    }

    public Vehicle(String vehicleNumber, String make, String model, Integer manufacturingYear,
                   Integer engineCapacityCc, FuelType fuelType, VehicleType vehicleType) {
        this.vehicleNumber = vehicleNumber;
        this.make = make;
        this.model = model;
        this.manufacturingYear = manufacturingYear;
        this.engineCapacityCc = engineCapacityCc;
        this.fuelType = fuelType;
        this.vehicleType = vehicleType;
        this.verificationStatus = VerificationStatus.UNVERIFIED;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.verificationStatus == null) {
            this.verificationStatus = VerificationStatus.UNVERIFIED;
        }
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
