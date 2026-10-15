package com.ubi.platform.service;

import com.ubi.platform.dto.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service to assess backend operational health and verify real database connectivity.
 */
@Service
public class HealthService {

    private static final Logger log = LoggerFactory.getLogger(HealthService.class);

    private final DataSource dataSource;

    @Value("${spring.application.name:usage-based-insurance-backend}")
    private String applicationName;

    @Value("${app.version:1.0.0-SNAPSHOT}")
    private String applicationVersion;

    public HealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Inspects JVM runtime and verifies active database connectivity.
     * Crucial: Does not claim database is healthy unless connectivity is explicitly confirmed.
     */
    public HealthResponse checkHealth() {
        HealthResponse.DatabaseHealth dbHealth = testDatabaseConnection();

        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        int processors = Runtime.getRuntime().availableProcessors();
        String javaVersion = System.getProperty("java.version");
        String jvmVendor = System.getProperty("java.vendor");

        HealthResponse.RuntimeInfo runtime = new HealthResponse.RuntimeInfo(
                javaVersion,
                jvmVendor,
                uptimeSeconds,
                processors
        );

        String overallStatus = "UP";

        return new HealthResponse(
                overallStatus,
                applicationName,
                applicationVersion,
                dbHealth,
                runtime
        );
    }

    private HealthResponse.DatabaseHealth testDatabaseConnection() {
        if (dataSource == null) {
            return HealthResponse.DatabaseHealth.disconnected("MySQL", "DataSource bean is not configured.");
        }

        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(2);
            if (valid) {
                String catalog = connection.getCatalog();
                String product = connection.getMetaData().getDatabaseProductName();
                return HealthResponse.DatabaseHealth.connected(product, catalog);
            } else {
                return HealthResponse.DatabaseHealth.disconnected("MySQL", "Connection validation timed out after 2 seconds.");
            }
        } catch (SQLException ex) {
            log.warn("Database connectivity check failed: {}", ex.getMessage());
            return HealthResponse.DatabaseHealth.disconnected(
                    "MySQL",
                    "Database offline or unreachable: " + ex.getMessage()
            );
        } catch (Exception ex) {
            log.warn("Unexpected error checking database: {}", ex.getMessage());
            return HealthResponse.DatabaseHealth.disconnected(
                    "MySQL",
                    "Error during connection attempt: " + ex.getMessage()
            );
        }
    }
}
