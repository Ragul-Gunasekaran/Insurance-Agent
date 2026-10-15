package com.ubi.platform.dto;

import com.ubi.platform.entity.VerificationStatus;

import java.time.Instant;
import java.util.Map;

/**
 * Diagnostic response from the Vehicle Verification Engine.
 * Clearly demarcates simulated/mock verification to prevent false claims of government integration.
 */
public class VehicleVerificationResponse {

    private Long vehicleId;
    private String vehicleNumber;
    private VerificationStatus verificationStatus;
    private boolean verified;
    private String provider;
    private Instant verifiedAt;
    private String explanation;
    private boolean simulated;
    private Map<String, String> verificationDetails;

    public VehicleVerificationResponse() {
        this.simulated = true;
        this.provider = "MOCK_VERIFICATION_SERVICE (Demo Rules Engine)";
        this.verifiedAt = Instant.now();
    }

    public VehicleVerificationResponse(Long vehicleId, String vehicleNumber, VerificationStatus verificationStatus,
                                       boolean verified, String explanation, Map<String, String> verificationDetails) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.verificationStatus = verificationStatus;
        this.verified = verified;
        this.provider = "MOCK_VERIFICATION_SERVICE (Demo Rules Engine)";
        this.verifiedAt = Instant.now();
        this.explanation = explanation;
        this.simulated = true;
        this.verificationDetails = verificationDetails;
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

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public boolean isSimulated() {
        return simulated;
    }

    public void setSimulated(boolean simulated) {
        this.simulated = simulated;
    }

    public Map<String, String> getVerificationDetails() {
        return verificationDetails;
    }

    public void setVerificationDetails(Map<String, String> verificationDetails) {
        this.verificationDetails = verificationDetails;
    }
}
