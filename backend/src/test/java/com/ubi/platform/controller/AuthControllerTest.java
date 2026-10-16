package com.ubi.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.LoginRequest;
import com.ubi.platform.dto.RegisterRequest;
import com.ubi.platform.dto.UserResponse;
import com.ubi.platform.entity.AccountStatus;
import com.ubi.platform.entity.UserRole;
import com.ubi.platform.exception.DuplicateResourceException;
import com.ubi.platform.exception.GlobalExceptionHandler;
import com.ubi.platform.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/register - Successfully registers customer with 201 Created")
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest("driver@example.com", "Password123!", "Driver Dan");
        UserResponse res = new UserResponse(1L, "driver@example.com", "Driver Dan", UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now());

        when(authService.registerCustomer(any(RegisterRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("driver@example.com"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Fails validation on invalid email or short password with 400 Bad Request")
    void register_invalidInput() throws Exception {
        RegisterRequest req = new RegisterRequest("invalid-email", "short", "");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Returns 409 Conflict on duplicate email")
    void register_duplicate() throws Exception {
        RegisterRequest req = new RegisterRequest("exists@example.com", "Password123!", "Existing User");

        when(authService.registerCustomer(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("User already exists with email: exists@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Returns 200 OK on successful authentication")
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest("driver@example.com", "Password123!");
        UserResponse res = new UserResponse(1L, "driver@example.com", "Driver Dan", UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now());

        when(authService.login(any(LoginRequest.class), any(), any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Returns 401 Unauthorized on invalid credentials")
    void login_invalidCredentials() throws Exception {
        LoginRequest req = new LoginRequest("driver@example.com", "WrongPassword");

        when(authService.login(any(LoginRequest.class), any(), any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Returns 200 OK")
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me - Returns 200 OK with authenticated profile")
    void me_authenticated() throws Exception {
        UserResponse res = new UserResponse(1L, "driver@example.com", "Driver Dan", UserRole.CUSTOMER, AccountStatus.ACTIVE, Instant.now());
        when(authService.getCurrentUser()).thenReturn(res);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("driver@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/csrf - Returns 200 OK with CSRF message")
    void csrf_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
