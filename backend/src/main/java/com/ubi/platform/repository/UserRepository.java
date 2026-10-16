package com.ubi.platform.repository;

import com.ubi.platform.entity.User;
import com.ubi.platform.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for User entities (Step 8).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by normalized email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user already exists with the given email address.
     */
    boolean existsByEmail(String email);

    /**
     * Count users by role (used for bootstrap verification).
     */
    long countByRole(UserRole role);
}
