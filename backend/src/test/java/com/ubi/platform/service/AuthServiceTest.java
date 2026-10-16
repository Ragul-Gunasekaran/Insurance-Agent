package com.ubi.platform.service;

import com.ubi.platform.dto.LoginRequest;
import com.ubi.platform.dto.RegisterRequest;
import com.ubi.platform.dto.UserResponse;
import com.ubi.platform.entity.AccountStatus;
import com.ubi.platform.entity.User;
import com.ubi.platform.entity.UserRole;
import com.ubi.platform.exception.DuplicateResourceException;
import com.ubi.platform.repository.UserRepository;
import com.ubi.platform.security.CustomUserPrincipal;
import com.ubi.platform.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private SecurityUtils securityUtils;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, securityUtils);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Registration successfully creates a customer account with BCrypt password hash and normalized email")
    void registerCustomer_success() {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setEmail("  Driver.John@Example.COM  ");
        regReq.setPassword("SecurePass123!");
        regReq.setFullName("John Driver");

        when(userRepository.existsByEmail("driver.john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123!")).thenReturn("$2a$12$hashedPasswordExample");

        User savedUser = new User("driver.john@example.com", "$2a$12$hashedPasswordExample", "John Driver", UserRole.CUSTOMER);
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse userResponse = authService.registerCustomer(regReq);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(10L);
        assertThat(userResponse.getEmail()).isEqualTo("driver.john@example.com");
        assertThat(userResponse.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(userResponse.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User captured = captor.getValue();
        assertThat(captured.getEmail()).isEqualTo("driver.john@example.com");
        assertThat(captured.getPasswordHash()).isEqualTo("$2a$12$hashedPasswordExample");
        assertThat(captured.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("Registration throws DuplicateResourceException when normalized email is already in use")
    void registerCustomer_duplicateEmail() {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setEmail("existing@example.com");
        regReq.setPassword("SecurePass123!");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerCustomer(regReq))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("An account with email 'existing@example.com' already exists.");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login succeeds with valid credentials and sets session security context")
    void login_success() {
        LoginRequest loginReq = new LoginRequest("user@example.com", "Password123!");
        User user = new User("user@example.com", "$2a$12$hash", "Test User", UserRole.CUSTOMER);
        user.setId(5L);

        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(request.getSession(true)).thenReturn(session);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        UserResponse response = authService.login(loginReq, request, this.response);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(auth);
    }

    @Test
    @DisplayName("Login throws BadCredentialsException on authentication failure with generic message")
    void login_badCredentials() {
        LoginRequest loginReq = new LoginRequest("user@example.com", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginReq, request, response))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password.");
    }

    @Test
    @DisplayName("Logout clears SecurityContext and invalidates session")
    void logout_success() {
        when(request.getSession(false)).thenReturn(session);

        authService.logout(request, response);

        verify(session).invalidate();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("getCurrentUser returns authenticated profile without sensitive password hash")
    void getCurrentUser_authenticated() {
        User user = new User("customer@trustdrive.internal", "$2a$12$hashed", "Demo Customer", UserRole.CUSTOMER);
        user.setId(1L);

        CustomUserPrincipal principal = new CustomUserPrincipal(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(securityUtils.getCurrentUserId()).thenReturn(Optional.of(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse userResponse = authService.getCurrentUser();

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getId()).isEqualTo(1L);
        assertThat(userResponse.getEmail()).isEqualTo("customer@trustdrive.internal");
        assertThat(userResponse.getRole()).isEqualTo(UserRole.CUSTOMER);
    }
}
