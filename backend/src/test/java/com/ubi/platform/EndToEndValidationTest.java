package com.ubi.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.LoginRequest;
import com.ubi.platform.dto.RegisterRequest;
import com.ubi.platform.dto.SimulateTelemetryRequest;
import com.ubi.platform.dto.VehicleRegistrationRequest;
import com.ubi.platform.dto.VehicleVerificationRequest;
import com.ubi.platform.entity.*;
import com.ubi.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Step 9: Comprehensive End-to-End Integration & Regression Validation Suite.
 * Validates the full 12-step Customer Journey, the complete Insurer Analytics Workflow,
 * Security & Ownership Isolation Boundaries, CSRF enforcement, and Data Quality Safeguards.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EndToEndValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TelematicsRepository telematicsRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private DrivingFeatureRepository featureRepository;

    @Autowired
    private RiskScoreRepository riskScoreRepository;

    @Autowired
    private PremiumQuoteRepository quoteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        quoteRepository.deleteAll();
        riskScoreRepository.deleteAll();
        featureRepository.deleteAll();
        telematicsRepository.deleteAll();
        tripRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Complete 12-Step Customer Workflow: Registration to Scorecard & Logout")
    void testCompleteCustomerWorkflow() throws Exception {
        // Step 1: Register customer account
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("neha.customer@trustdrive.internal");
        registerReq.setPassword("SecurePass2026!");
        registerReq.setFullName("Neha Sharma");

        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("neha.customer@trustdrive.internal"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"))
                .andReturn();

        // Step 2: Login and verify session creation & restoration via /me
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("neha.customer@trustdrive.internal");
        loginReq.setPassword("SecurePass2026!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"))
                .andReturn();

        MockHttpSession customerSession = (MockHttpSession) loginResult.getRequest().getSession();
        assertThat(customerSession).isNotNull();

        // Restore authenticated session via /auth/me
        mockMvc.perform(get("/api/v1/auth/me").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("neha.customer@trustdrive.internal"));

        // Step 3: Register and verify a vehicle
        VehicleRegistrationRequest vehReq = new VehicleRegistrationRequest(
                "MH12DE3456", "Maruti Suzuki", "Baleno", 2023, 1197, FuelType.PETROL, VehicleType.CAR
        );

        MvcResult vehResult = mockMvc.perform(post("/api/v1/vehicles")
                        .session(customerSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicleNumber").value("MH12DE3456"))
                .andReturn();

        JsonNode vehNode = objectMapper.readTree(vehResult.getResponse().getContentAsString());
        Long vehicleId = vehNode.get("data").get("id").asLong();
        assertThat(vehicleId).isPositive();

        // Mock verification
        VehicleVerificationRequest verifyReq = new VehicleVerificationRequest(
                vehicleId, "MH12DE3456", "Maruti Suzuki", "Baleno", 2023
        );
        mockMvc.perform(post("/api/v1/vehicles/verify")
                        .session(customerSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"));

        // Step 4: Simulate telematics route (SAFE_COMMUTE profile with 100 points, >0.5km and >60s)
        SimulateTelemetryRequest simReq = new SimulateTelemetryRequest();
        simReq.setVehicleId(vehicleId);
        simReq.setProfile("SAFE_COMMUTE");
        simReq.setPointCount(100);

        mockMvc.perform(post("/api/v1/telemetry/simulate")
                        .session(customerSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalIngested").value(100))
                .andExpect(jsonPath("$.data.validPoints").value(100));

        // Step 5: Query telemetry and summary
        mockMvc.perform(get("/api/v1/telemetry/vehicles/" + vehicleId)
                        .session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(100));

        mockMvc.perform(get("/api/v1/telemetry/vehicles/" + vehicleId + "/summary")
                        .session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.validPoints").value(100));

        // Step 6: Stitch telemetry into trips
        MvcResult stitchResult = mockMvc.perform(post("/api/v1/trips/stitch/" + vehicleId)
                        .session(customerSession)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        JsonNode stitchNode = objectMapper.readTree(stitchResult.getResponse().getContentAsString());
        assertThat(stitchNode.get("data").size()).isGreaterThanOrEqualTo(1);
        Long tripId = stitchNode.get("data").get(0).get("id").asLong();

        // Finalize trip as COMPLETED with observed distance > 10 km to test full scoring and pricing calculation
        Trip trip = tripRepository.findById(tripId).orElseThrow();
        trip.setStatus(TripStatus.COMPLETED);
        trip.setEndTime(trip.getStartTime() != null ? trip.getStartTime().plusSeconds(900) : java.time.Instant.now());
        trip.setDistanceKm(15.0);
        tripRepository.save(trip);

        // Step 7: Calculate driving features
        mockMvc.perform(post("/api/v1/features/vehicles/" + vehicleId + "/calculate")
                        .session(customerSession)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/features/trips/" + tripId)
                        .session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(tripId));

        // Step 8: Calculate behavioral risk scores
        mockMvc.perform(post("/api/v1/risk-scores/vehicles/" + vehicleId + "/calculate")
                        .session(customerSession)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 9: Verify trip score retrieval
        mockMvc.perform(get("/api/v1/risk-scores/trips/" + tripId)
                        .session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").isNumber())
                .andExpect(jsonPath("$.data.isScorable").value(true));

        // Step 10: Calculate illustrative premium quote with annual mileage assumption
        com.ubi.platform.dto.PricingCalculationRequestDto priceReq =
                new com.ubi.platform.dto.PricingCalculationRequestDto(10000.0);
        mockMvc.perform(post("/api/v1/pricing/vehicles/" + vehicleId + "/quotes")
                        .session(customerSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priceReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quoteReference").isString())
                .andExpect(jsonPath("$.data.status").value("READY"))
                .andExpect(jsonPath("$.data.finalAnnualPremiumInr").isNumber())
                .andExpect(jsonPath("$.data.currency").value("INR"));

        // Step 11: View Customer Scorecard and Trip drill-down
        mockMvc.perform(get("/api/v1/dashboard/customer/vehicles/" + vehicleId)
                        .session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicleId").value(vehicleId))
                .andExpect(jsonPath("$.data.vehicleNumber").value("MH12DE3456"));

        mockMvc.perform(get("/api/v1/dashboard/customer/vehicles/" + vehicleId + "/trips")
                        .session(customerSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // Step 12: Logout and confirm protected endpoints require authentication again
        mockMvc.perform(post("/api/v1/auth/logout")
                        .session(customerSession)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/auth/me").session(customerSession))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/v1/vehicles/" + vehicleId).session(customerSession))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Insurer Workflow: Full Fleet Analytics Access & Customer 403 Forbidden Restriction")
    void testInsurerWorkflowAndRoleBoundaries() throws Exception {
        // Setup customer with a vehicle
        User customer = new User("cust@trustdrive.internal", passwordEncoder.encode("Pass1234!"), "Customer", UserRole.CUSTOMER);
        customer = userRepository.save(customer);

        Vehicle custVehicle = new Vehicle("DL01AB9999", "Tata", "Nexon", 2023, 1199, FuelType.PETROL, VehicleType.CAR);
        custVehicle.setOwner(customer);
        custVehicle.setVerificationStatus(VerificationStatus.VERIFIED);
        vehicleRepository.save(custVehicle);

        // Setup insurer user
        User insurer = new User("actuary@trustdrive.internal", passwordEncoder.encode("InsurePass1234!"), "Fleet Actuary", UserRole.INSURER);
        insurer = userRepository.save(insurer);

        // Customer Session
        LoginRequest custLogin = new LoginRequest();
        custLogin.setEmail("cust@trustdrive.internal");
        custLogin.setPassword("Pass1234!");
        MvcResult custLoginRes = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(custLogin)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession custSession = (MockHttpSession) custLoginRes.getRequest().getSession();

        // Insurer Session
        LoginRequest insLogin = new LoginRequest();
        insLogin.setEmail("actuary@trustdrive.internal");
        insLogin.setPassword("InsurePass1234!");
        MvcResult insLoginRes = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(insLogin)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession insSession = (MockHttpSession) insLoginRes.getRequest().getSession();

        // 1. Customer receives 403 Forbidden on all Insurer endpoints
        mockMvc.perform(get("/api/v1/dashboard/insurer/overview").session(custSession))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        mockMvc.perform(get("/api/v1/dashboard/insurer/risk-distribution").session(custSession))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/dashboard/insurer/behavior").session(custSession))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/dashboard/insurer/usage").session(custSession))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/dashboard/insurer/pricing").session(custSession))
                .andExpect(status().isForbidden());

        // 2. Insurer receives 200 OK on all Insurer Analytics endpoints
        mockMvc.perform(get("/api/v1/dashboard/insurer/overview").session(insSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRegisteredVehicles").value(1));

        mockMvc.perform(get("/api/v1/dashboard/insurer/risk-distribution").session(insSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/dashboard/insurer/behavior").session(insSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/dashboard/insurer/usage").session(insSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/dashboard/insurer/pricing").session(insSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. Insurer can audit customer vehicle details for authorized underwriting
        mockMvc.perform(get("/api/v1/vehicles/" + custVehicle.getId()).session(insSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicleNumber").value("DL01AB9999"));
    }

    @Test
    @DisplayName("Data Quality Safeguards: Zero Telemetry & Insufficient Data Handling")
    void testDataQualityAndPricingEdgeCases() throws Exception {
        // Register customer & vehicle with zero telemetry
        User customer = new User("data.quality@trustdrive.internal", passwordEncoder.encode("Pass1234!"), "DQ User", UserRole.CUSTOMER);
        customer = userRepository.save(customer);

        Vehicle vehicle = new Vehicle("KA05CD7777", "Toyota", "Fortuner", 2022, 2755, FuelType.DIESEL, VehicleType.SUV);
        vehicle.setOwner(customer);
        vehicle.setVerificationStatus(VerificationStatus.VERIFIED);
        vehicle = vehicleRepository.save(vehicle);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("data.quality@trustdrive.internal");
        loginReq.setPassword("Pass1234!");
        MvcResult loginRes = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginRes.getRequest().getSession();

        // Mutating request without CSRF is rejected with 403 Forbidden
        mockMvc.perform(post("/api/v1/pricing/vehicles/" + vehicle.getId() + "/quotes").session(session))
                .andExpect(status().isForbidden());

        // Calculating quote with valid CSRF token but zero telemetry results in INSUFFICIENT_DATA
        mockMvc.perform(post("/api/v1/pricing/vehicles/" + vehicle.getId() + "/quotes")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INSUFFICIENT_DATA"))
                .andExpect(jsonPath("$.data.isEligible").value(false))
                .andExpect(jsonPath("$.data.finalAnnualPremiumInr").doesNotExist());

        // Repeated quote generation produces distinct immutable quote records
        mockMvc.perform(post("/api/v1/pricing/vehicles/" + vehicle.getId() + "/quotes")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INSUFFICIENT_DATA"));

        mockMvc.perform(get("/api/v1/pricing/vehicles/" + vehicle.getId() + "/quotes").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }
}
