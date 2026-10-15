package com.ubi.platform.controller;

import com.ubi.platform.dto.ApiResponse;
import com.ubi.platform.dto.HealthResponse;
import com.ubi.platform.service.HealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing system and database operational health.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponse>> getHealth() {
        HealthResponse health = healthService.checkHealth();
        return ResponseEntity.ok(ApiResponse.ok(health));
    }
}
