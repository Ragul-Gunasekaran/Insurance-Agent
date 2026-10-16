package com.ubi.platform.security;

import com.ubi.platform.entity.User;
import com.ubi.platform.entity.UserRole;
import com.ubi.platform.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility helper for inspecting the current Spring Security context and authenticated principal (Step 8).
 */
@Component
public class SecurityUtils {

    /**
     * Retrieves the active authentication object from SecurityContextHolder.
     */
    public Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(auth);
    }

    /**
     * Retrieves the custom principal of the currently authenticated user.
     */
    public Optional<CustomUserPrincipal> getCurrentPrincipal() {
        return getAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof CustomUserPrincipal)
                .map(auth -> (CustomUserPrincipal) auth.getPrincipal());
    }

    /**
     * Retrieves the user ID of the currently authenticated user.
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentPrincipal().map(CustomUserPrincipal::getId);
    }

    /**
     * Retrieves the role of the currently authenticated user.
     */
    public Optional<UserRole> getCurrentUserRole() {
        return getCurrentPrincipal().map(CustomUserPrincipal::getRole);
    }

    /**
     * Checks if the currently authenticated user is an INSURER.
     */
    public boolean isInsurer() {
        return getCurrentUserRole().map(r -> r == UserRole.INSURER).orElse(false);
    }

    /**
     * Checks if the currently authenticated user is a CUSTOMER.
     */
    public boolean isCustomer() {
        return getCurrentUserRole().map(r -> r == UserRole.CUSTOMER).orElse(false);
    }

    /**
     * Checks if there is an active authenticated user.
     */
    public boolean isAuthenticated() {
        return getAuthentication().isPresent();
    }

    /**
     * Resolves the full JPA User entity for the current authenticated user.
     */
    public Optional<User> getCurrentUserEntity(UserRepository userRepository) {
        return getCurrentUserId().flatMap(userRepository::findById);
    }
}
