package com.ubi.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Usage-Based Insurance (UBI) Platform Application Entry Point.
 * Problem Statement: PS-008 (InsurTech & IoT Telematics)
 */
@SpringBootApplication
public class UsageBasedInsuranceApplication {

    private static final Logger log = LoggerFactory.getLogger(UsageBasedInsuranceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UsageBasedInsuranceApplication.class, args);
        log.info("========================================================================");
        log.info(" Usage-Based Insurance Platform backend started successfully.");
        log.info(" Health endpoint: http://localhost:8080/api/v1/health");
        log.info(" Info endpoint:   http://localhost:8080/api/v1/info");
        log.info("========================================================================");
    }
}
