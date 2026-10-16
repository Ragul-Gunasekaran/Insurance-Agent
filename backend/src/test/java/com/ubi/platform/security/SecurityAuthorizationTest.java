package com.ubi.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.RegisterRequest;
import com.ubi.platform.entity.AccountStatus;
import com.ubi.platform.entity.FuelType;
import com.ubi.platform.entity.User;
import com.ubi.platform.entity.UserRole;
import com.ubi.platform.entity.Vehicle;
import com.ubi.platform.entity.VehicleType;
import com.ubi.platform.entity.VerificationStatus;
import com.ubi.platform.repository.UserRepository;
import com.ubi.platform.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private com.ubi.platform.repository.PremiumQuoteRepository quoteRepository;

    @Autowired
    private com.ubi.platform.repository.RiskScoreRepository riskScoreRepository;

    @Autowired
    private com.ubi.platform.repository.DrivingFeatureRepository featureRepository;

    @Autowired
    private com.ubi.platform.repository.TelematicsRepository telematicsRepository;

    @Autowired
    private com.ubi.platform.repository.TripRepository tripRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User customer1;
    private User customer2;
    private User insurerUser;
    private Vehicle customer1Vehicle;
    private Vehicle customer2Vehicle;

    @BeforeEach
    void setUp() {
        quoteRepository.deleteAll();
        riskScoreRepository.deleteAll();
        featureRepository.deleteAll();
        telematicsRepository.deleteAll();
        tripRepository.deleteAll();
        vehicleRepository.deleteAll();
        userRepository.deleteAll();

        customer1 = new User("alice@example.com", passwordEncoder.encode("Pass1234!"), "Alice Customer", UserRole.CUSTOMER);
        customer1 = userRepository.save(customer1);

        customer2 = new User("bob@example.com", passwordEncoder.encode("Pass1234!"), "Bob Customer", UserRole.CUSTOMER);
        customer2 = userRepository.save(customer2);

        insurerUser = new User("underwriter@example.com", passwordEncoder.encode("InsurePass1234!"), "Fleet Underwriter", UserRole.INSURER);
        insurerUser = userRepository.save(insurerUser);

        customer1Vehicle = new Vehicle("MH01AA1111", "Honda", "City", 2022, 1498, FuelType.PETROL, VehicleType.CAR);
        customer1Vehicle.setOwner(customer1);
        customer1Vehicle.setVerificationStatus(VerificationStatus.VERIFIED);
        customer1Vehicle = vehicleRepository.save(customer1Vehicle);

        customer2Vehicle = new Vehicle("KA01BB2222", "Hyundai", "Creta", 2023, 1493, FuelType.DIESEL, VehicleType.CAR);
        customer2Vehicle.setOwner(customer2);
        customer2Vehicle.setVerificationStatus(VerificationStatus.VERIFIED);
        customer2Vehicle = vehicleRepository.save(customer2Vehicle);
    }

    @Test
    @DisplayName("Anonymous request to protected /api/v1/vehicles is rejected with 401 Unauthorized")
    void anonymous_vehicles_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Anonymous request to /api/v1/dashboard/insurer/overview is rejected with 401 Unauthorized")
    void anonymous_insurerDashboard_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/insurer/overview"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Anonymous request to /api/v1/auth/me is rejected with 401 Unauthorized")
    void anonymous_authMe_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Customer accessing their own vehicle receives 200 OK")
    void customer_accessOwnVehicle_returns200() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(customer1);

        mockMvc.perform(get("/api/v1/vehicles/" + customer1Vehicle.getId())
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicleNumber").value("MH01AA1111"));
    }

    @Test
    @DisplayName("Customer accessing another customer's vehicle receives 404 Not Found to prevent enumeration")
    void customer_accessOtherCustomerVehicle_returns404() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(customer1);

        mockMvc.perform(get("/api/v1/vehicles/" + customer2Vehicle.getId())
                        .with(user(principal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Customer attempting to access insurer dashboard receives 403 Forbidden")
    void customer_accessInsurerDashboard_returns403() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(customer1);

        mockMvc.perform(get("/api/v1/dashboard/insurer/overview")
                        .with(user(principal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("Insurer accessing insurer dashboard receives 200 OK")
    void insurer_accessInsurerDashboard_returns200() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(insurerUser);

        mockMvc.perform(get("/api/v1/dashboard/insurer/overview")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRegisteredVehicles").isNumber());
    }

    @Test
    @DisplayName("Insurer accessing vehicle fleet receives 200 OK with cross-fleet visibility")
    void insurer_accessVehicles_returns200() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(insurerUser);

        mockMvc.perform(get("/api/v1/vehicles")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Customer accessing customer driver scorecard for own vehicle receives 200 OK")
    void customer_accessScorecard_returns200() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(customer1);

        mockMvc.perform(get("/api/v1/dashboard/customer/vehicles/" + customer1Vehicle.getId())
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vehicleNumber").value("MH01AA1111"));
    }

    @Test
    @DisplayName("Customer accessing customer driver scorecard for other customer's vehicle receives 404 Not Found")
    void customer_accessOtherScorecard_returns404() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(customer1);

        mockMvc.perform(get("/api/v1/dashboard/customer/vehicles/" + customer2Vehicle.getId())
                        .with(user(principal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("Public registration creates CUSTOMER role only and stores password securely")
    void register_createsCustomerOnly() throws Exception {
        RegisterRequest request = new RegisterRequest("new.user@example.com", "MySecurePass123!", "New User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        User created = userRepository.findByEmail("new.user@example.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(passwordEncoder.matches("MySecurePass123!", created.getPasswordHash())).isTrue();
    }
}
