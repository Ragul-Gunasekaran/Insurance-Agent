package com.ubi.platform.dto;

import com.ubi.platform.entity.AccountStatus;
import com.ubi.platform.entity.User;
import com.ubi.platform.entity.UserRole;

import java.time.Instant;

/**
 * Outbound DTO representing an authenticated user account (Step 8).
 * Never exposes password hashes or security credentials.
 */
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    private AccountStatus status;
    private Instant createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String fullName, UserRole role, AccountStatus status, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
