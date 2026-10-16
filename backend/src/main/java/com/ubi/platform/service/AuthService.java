package com.ubi.platform.service;

import com.ubi.platform.dto.LoginRequest;
import com.ubi.platform.dto.RegisterRequest;
import com.ubi.platform.dto.UserResponse;
import com.ubi.platform.entity.AccountStatus;
import com.ubi.platform.entity.User;
import com.ubi.platform.entity.UserRole;
import com.ubi.platform.exception.DuplicateResourceException;
import com.ubi.platform.exception.ResourceNotFoundException;
import com.ubi.platform.repository.UserRepository;
import com.ubi.platform.security.CustomUserPrincipal;
import com.ubi.platform.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user account lifecycle, authentication, and session persistence (Step 8).
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityUtils securityUtils;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityUtils = securityUtils;
    }

    /**
     * Registers a new customer account.
     * Always assigns the CUSTOMER role; never assigns INSURER role to public requests.
     */
    @Transactional
    public UserResponse registerCustomer(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        log.info("Attempting customer registration for normalized email: {}", normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email '" + normalizedEmail + "' already exists.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        String fullName = request.getFullName() != null ? request.getFullName().trim() : null;

        User user = new User(normalizedEmail, passwordHash, fullName, UserRole.CUSTOMER);
        user.setStatus(AccountStatus.ACTIVE);

        User saved = userRepository.save(user);
        log.info("Successfully registered customer ID: {} [{}]", saved.getId(), saved.getEmail());

        return UserResponse.fromEntity(saved);
    }

    /**
     * Authenticates user credentials and establishes an HTTP session.
     */
    public UserResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authentication);
            securityContextHolderStrategy.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            log.info("Successful login for user ID: {} [{}] with role {}", principal.getId(), principal.getEmail(), principal.getRole());

            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + principal.getId()));

            return UserResponse.fromEntity(user);
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for email {}: {}", normalizedEmail, ex.getMessage());
            // Generic message to avoid email enumeration
            throw new BadCredentialsException("Invalid email or password.");
        }
    }

    /**
     * Terminates the active HTTP session and clears the security context.
     */
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        log.info("Active session successfully cleared upon logout.");
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long userId = securityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("No active authenticated session found."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return UserResponse.fromEntity(user);
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }
}
