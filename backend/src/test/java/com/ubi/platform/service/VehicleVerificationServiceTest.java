package com.ubi.platform.service;

import com.ubi.platform.dto.VehicleVerificationRequest;
import com.ubi.platform.dto.VehicleVerificationResponse;
import com.ubi.platform.entity.VerificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleVerificationServiceTest {

    private VehicleVerificationService verificationService;

    @BeforeEach
    void setUp() {
        verificationService = new VehicleVerificationService();
    }

    @Test
    @DisplayName("Verify known sample registry vehicle returns VERIFIED")
    void verify_KnownMockVehicle_ReturnsVerified() {
        VehicleVerificationRequest request = new VehicleVerificationRequest(
                null, "MH02AB1234", "Honda", "City", 2021
        );

        VehicleVerificationResponse response = verificationService.verify(request);

        assertNotNull(response);
        assertEquals(VerificationStatus.VERIFIED, response.getVerificationStatus());
        assertTrue(response.isVerified());
        assertTrue(response.isSimulated());
        assertEquals("MH02AB1234", response.getVehicleNumber());
    }

    @Test
    @DisplayName("Verify standard pattern registration returns VERIFIED")
    void verify_StandardPattern_ReturnsVerified() {
        VehicleVerificationRequest request = new VehicleVerificationRequest(
                null, "DL-01-XY-9876", "Hyundai", "Creta", 2022
        );

        VehicleVerificationResponse response = verificationService.verify(request);

        assertNotNull(response);
        assertEquals(VerificationStatus.VERIFIED, response.getVerificationStatus());
        assertTrue(response.isVerified());
        assertEquals("DL01XY9876", response.getVehicleNumber());
    }

    @Test
    @DisplayName("Verify blacklisted or test vehicle number returns REJECTED")
    void verify_BlacklistedVehicle_ReturnsRejected() {
        VehicleVerificationRequest request = new VehicleVerificationRequest(
                null, "STOLEN123", "Toyota", "Corolla", 2018
        );

        VehicleVerificationResponse response = verificationService.verify(request);

        assertNotNull(response);
        assertEquals(VerificationStatus.REJECTED, response.getVerificationStatus());
        assertFalse(response.isVerified());
        assertTrue(response.getExplanation().contains("flagged"));
    }

    @Test
    @DisplayName("Verify vehicle older than 1995 returns FLAGGED for inspection")
    void verify_OverageVehicle_ReturnsFlagged() {
        VehicleVerificationRequest request = new VehicleVerificationRequest(
                null, "KA01CD5678", "Maruti", "800", 1992
        );

        VehicleVerificationResponse response = verificationService.verify(request);

        assertNotNull(response);
        assertEquals(VerificationStatus.FLAGGED, response.getVerificationStatus());
        assertFalse(response.isVerified());
        assertTrue(response.getExplanation().contains("30-year"));
    }

    @Test
    @DisplayName("Normalization removes spaces and hyphens consistently")
    void normalizeVehicleNumber_FormatsCorrectly() {
        assertEquals("MH02AB1234", verificationService.normalizeVehicleNumber("  mh-02-ab-1234 "));
        assertEquals("DL01C9999", verificationService.normalizeVehicleNumber(" dl 01 c 9999 "));
    }
}
