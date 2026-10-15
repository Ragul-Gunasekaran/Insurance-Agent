package com.ubi.platform.service;

import com.ubi.platform.dto.VehicleVerificationRequest;
import com.ubi.platform.dto.VehicleVerificationResponse;
import com.ubi.platform.entity.VerificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service providing vehicle verification.
 *
 * IMPORTANT COMPLIANCE NOTICE:
 * This is an authorized prototype mock verification engine developed for Hackathon PS-008.
 * It simulates verification checks using deterministic underwriting rules and sample data.
 * It does NOT connect to NextGen mParivahan or any government API.
 */
@Service
public class VehicleVerificationService {

    private static final Logger log = LoggerFactory.getLogger(VehicleVerificationService.class);

    // Standard vehicle registration format (e.g., DL01AB1234, MH12CD5678, KA05EF9999)
    private static final Pattern STANDARD_REGISTRATION_PATTERN =
            Pattern.compile("^[A-Z]{2}[0-9]{1,2}[A-Z]{0,3}[0-9]{4}$");

    // Sample pre-approved benchmark vehicles in mock database
    private static final Set<String> KNOWN_MOCK_REGISTRY = Set.of(
            "MH02AB1234",
            "KA01CD5678",
            "DL03EF9012",
            "TN07GH3456",
            "TS09JK7890",
            "UP16LM4321",
            "GJ01NP8765"
    );

    // Known test blacklisted / rejected numbers
    private static final Set<String> BLACKLISTED_TEST_NUMBERS = Set.of(
            "XX00XX0000",
            "STOLEN123",
            "REJECT9999",
            "TEST0000"
    );

    /**
     * Executes mock verification against structured underwriting rules and sample records.
     */
    public VehicleVerificationResponse verify(VehicleVerificationRequest request) {
        String rawNumber = request.getVehicleNumber();
        String normalizedNumber = normalizeVehicleNumber(rawNumber);
        String make = request.getMake() != null ? request.getMake().trim() : "";
        String model = request.getModel() != null ? request.getModel().trim() : "";
        Integer year = request.getManufacturingYear();

        log.info("Running simulated vehicle verification for: {}", normalizedNumber);

        Map<String, String> details = new LinkedHashMap<>();
        details.put("normalizedRegistration", normalizedNumber);
        details.put("ruleEngine", "UBI-Underwriting-Mock-v1");

        // Rule 1: Check Blacklisted / Flagged test identifiers
        if (BLACKLISTED_TEST_NUMBERS.contains(normalizedNumber) || normalizedNumber.contains("TEST")) {
            details.put("blacklistCheck", "FLAGGED_IN_REGISTRY");
            return new VehicleVerificationResponse(
                    request.getVehicleId(),
                    normalizedNumber,
                    VerificationStatus.REJECTED,
                    false,
                    "Mock Verification: Vehicle registration number is flagged in the mock blacklist registry.",
                    details
            );
        }

        // Rule 2: Vehicle Age Validation
        int currentYear = Year.now().getValue();
        if (year != null) {
            int age = currentYear - year;
            details.put("vehicleAgeYears", String.valueOf(age));
            if (year < 1995) {
                details.put("ageCompliance", "OVERAGE_EXCEEDS_LIMIT");
                return new VehicleVerificationResponse(
                        request.getVehicleId(),
                        normalizedNumber,
                        VerificationStatus.FLAGGED,
                        false,
                        "Mock Verification: Vehicle age exceeds 30-year automated underwriting limit; requires manual inspection.",
                        details
                );
            } else if (year > currentYear + 1) {
                details.put("ageCompliance", "INVALID_FUTURE_YEAR");
                return new VehicleVerificationResponse(
                        request.getVehicleId(),
                        normalizedNumber,
                        VerificationStatus.REJECTED,
                        false,
                        "Mock Verification: Manufacturing year cannot be in the future.",
                        details
                );
            } else {
                details.put("ageCompliance", "COMPLIANT");
            }
        }

        // Rule 3: Known Sample Database or Pattern Match
        boolean isKnownMock = KNOWN_MOCK_REGISTRY.contains(normalizedNumber);
        boolean isStandardPattern = STANDARD_REGISTRATION_PATTERN.matcher(normalizedNumber).matches();

        if (isKnownMock) {
            details.put("registryLookup", "EXACT_MATCH_SAMPLE_REGISTRY");
            details.put("formatValidation", "VALID_STANDARD_SERIES");
            return new VehicleVerificationResponse(
                    request.getVehicleId(),
                    normalizedNumber,
                    VerificationStatus.VERIFIED,
                    true,
                    "Mock Verification: Vehicle record verified successfully against sample insurer registry.",
                    details
            );
        }

        if (isStandardPattern) {
            details.put("registryLookup", "SYNTHETIC_PATTERN_MATCH");
            details.put("formatValidation", "VALID_STANDARD_SERIES");
            return new VehicleVerificationResponse(
                    request.getVehicleId(),
                    normalizedNumber,
                    VerificationStatus.VERIFIED,
                    true,
                    "Mock Verification: Registration format and vehicle attributes conform to verified underwriting standards.",
                    details
            );
        }

        // Fallback for valid alphanumeric non-standard registrations (e.g. customized plates)
        if (normalizedNumber.length() >= 5) {
            details.put("registryLookup", "CONDITIONAL_APPROVAL");
            details.put("formatValidation", "NON_STANDARD_FORMAT");
            return new VehicleVerificationResponse(
                    request.getVehicleId(),
                    normalizedNumber,
                    VerificationStatus.VERIFIED,
                    true,
                    "Mock Verification: Non-standard registration accepted under simulated provisional verification.",
                    details
            );
        }

        details.put("formatValidation", "INVALID_CHARS_OR_LENGTH");
        return new VehicleVerificationResponse(
                request.getVehicleId(),
                normalizedNumber,
                VerificationStatus.REJECTED,
                false,
                "Mock Verification: Vehicle registration format could not be verified by rule engine.",
                details
        );
    }

    /**
     * Consistently normalizes vehicle registration numbers:
     * Trims leading/trailing whitespace, converts to uppercase, and removes hyphens and spaces.
     */
    public String normalizeVehicleNumber(String vehicleNumber) {
        if (vehicleNumber == null) {
            return "";
        }
        return vehicleNumber.trim()
                .toUpperCase()
                .replaceAll("[\\s-]+", "");
    }
}
