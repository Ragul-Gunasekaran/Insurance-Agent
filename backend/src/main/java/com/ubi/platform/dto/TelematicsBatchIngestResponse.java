package com.ubi.platform.dto;

import java.util.List;

/**
 * Outbound DTO returned after ingesting a batch of telematics readings.
 */
public class TelematicsBatchIngestResponse {

    private Long vehicleId;
    private String vehicleNumber;
    private int totalIngested;
    private int validPoints;
    private int anomalousPoints;
    private String message;
    private List<TelematicsPointResponse> samplePoints;

    public TelematicsBatchIngestResponse() {
    }

    public TelematicsBatchIngestResponse(Long vehicleId, String vehicleNumber, int totalIngested,
                                         int validPoints, int anomalousPoints, String message,
                                         List<TelematicsPointResponse> samplePoints) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.totalIngested = totalIngested;
        this.validPoints = validPoints;
        this.anomalousPoints = anomalousPoints;
        this.message = message;
        this.samplePoints = samplePoints;
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

    public int getTotalIngested() {
        return totalIngested;
    }

    public void setTotalIngested(int totalIngested) {
        this.totalIngested = totalIngested;
    }

    public int getValidPoints() {
        return validPoints;
    }

    public void setValidPoints(int validPoints) {
        this.validPoints = validPoints;
    }

    public int getAnomalousPoints() {
        return anomalousPoints;
    }

    public void setAnomalousPoints(int anomalousPoints) {
        this.anomalousPoints = anomalousPoints;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<TelematicsPointResponse> getSamplePoints() {
        return samplePoints;
    }

    public void setSamplePoints(List<TelematicsPointResponse> samplePoints) {
        this.samplePoints = samplePoints;
    }
}
