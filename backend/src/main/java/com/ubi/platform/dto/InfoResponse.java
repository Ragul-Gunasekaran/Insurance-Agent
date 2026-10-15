package com.ubi.platform.dto;

import java.util.List;

/**
 * Metadata descriptor detailing application identification and planned architecture modules.
 */
public class InfoResponse {

    private String name;
    private String problemStatement;
    private String version;
    private String description;
    private String environment;
    private List<String> plannedModules;

    public InfoResponse() {
    }

    public InfoResponse(String name, String problemStatement, String version, String description, String environment, List<String> plannedModules) {
        this.name = name;
        this.problemStatement = problemStatement;
        this.version = version;
        this.description = description;
        this.environment = environment;
        this.plannedModules = plannedModules;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProblemStatement() {
        return problemStatement;
    }

    public void setProblemStatement(String problemStatement) {
        this.problemStatement = problemStatement;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public List<String> getPlannedModules() {
        return plannedModules;
    }

    public void setPlannedModules(List<String> plannedModules) {
        this.plannedModules = plannedModules;
    }
}
