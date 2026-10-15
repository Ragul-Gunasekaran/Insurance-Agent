package com.ubi.platform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Diagnostic payload detailing application status, runtime info, and verified database connectivity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthResponse {

    private String status;
    private String application;
    private String version;
    private DatabaseHealth database;
    private RuntimeInfo runtime;

    public HealthResponse() {
    }

    public HealthResponse(String status, String application, String version, DatabaseHealth database, RuntimeInfo runtime) {
        this.status = status;
        this.application = application;
        this.version = version;
        this.database = database;
        this.runtime = runtime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DatabaseHealth getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseHealth database) {
        this.database = database;
    }

    public RuntimeInfo getRuntime() {
        return runtime;
    }

    public void setRuntime(RuntimeInfo runtime) {
        this.runtime = runtime;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DatabaseHealth {
        private String status;
        private String engine;
        private String catalog;
        private String message;

        public DatabaseHealth() {
        }

        public DatabaseHealth(String status, String engine, String catalog, String message) {
            this.status = status;
            this.engine = engine;
            this.catalog = catalog;
            this.message = message;
        }

        public static DatabaseHealth connected(String engine, String catalog) {
            return new DatabaseHealth("CONNECTED", engine, catalog, "Connection active and responsive.");
        }

        public static DatabaseHealth disconnected(String engine, String message) {
            return new DatabaseHealth("DISCONNECTED", engine, null, message);
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }

        public String getCatalog() {
            return catalog;
        }

        public void setCatalog(String catalog) {
            this.catalog = catalog;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RuntimeInfo {
        private String javaVersion;
        private String jvmVendor;
        private long uptimeSeconds;
        private int availableProcessors;

        public RuntimeInfo() {
        }

        public RuntimeInfo(String javaVersion, String jvmVendor, long uptimeSeconds, int availableProcessors) {
            this.javaVersion = javaVersion;
            this.jvmVendor = jvmVendor;
            this.uptimeSeconds = uptimeSeconds;
            this.availableProcessors = availableProcessors;
        }

        public String getJavaVersion() {
            return javaVersion;
        }

        public void setJavaVersion(String javaVersion) {
            this.javaVersion = javaVersion;
        }

        public String getJvmVendor() {
            return jvmVendor;
        }

        public void setJvmVendor(String jvmVendor) {
            this.jvmVendor = jvmVendor;
        }

        public long getUptimeSeconds() {
            return uptimeSeconds;
        }

        public void setUptimeSeconds(long uptimeSeconds) {
            this.uptimeSeconds = uptimeSeconds;
        }

        public int getAvailableProcessors() {
            return availableProcessors;
        }

        public void setAvailableProcessors(int availableProcessors) {
            this.availableProcessors = availableProcessors;
        }
    }
}
