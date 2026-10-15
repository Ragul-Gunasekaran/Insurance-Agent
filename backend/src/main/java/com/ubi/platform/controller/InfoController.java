package com.ubi.platform.controller;

import com.ubi.platform.dto.ApiResponse;
import com.ubi.platform.dto.InfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller exposing application descriptor, version, and architecture roadmap.
 */
@RestController
@RequestMapping("/api/v1")
public class InfoController {

    @Value("${app.name:Usage-Based Insurance Platform}")
    private String appName;

    @Value("${app.version:1.0.0-SNAPSHOT}")
    private String appVersion;

    @Value("${app.description:Production-oriented prototype for telematics-driven dynamic insurance underwriting and actuarial risk scoring.}")
    private String appDescription;

    @Value("${spring.profiles.active:development}")
    private String environment;

    private static final List<String> PLANNED_MODULES = List.of(
            "vehicle-registration",
            "vehicle-verification",
            "telemetry-ingestion",
            "data-validation",
            "trip-stitching",
            "feature-engineering",
            "risk-scoring",
            "actuarial-pricing",
            "explanation-service",
            "customer-dashboard"
    );

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<InfoResponse>> getInfo() {
        InfoResponse info = new InfoResponse(
                appName,
                "PS-008: Usage-Based Insurance (InsurTech & IoT Telematics)",
                appVersion,
                appDescription,
                environment,
                PLANNED_MODULES
        );
        return ResponseEntity.ok(ApiResponse.ok(info));
    }
}
