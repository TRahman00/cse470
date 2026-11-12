
package com.mfsfraudgraph.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "analysis_runs")
public class AnalysisRun {

    @Id
    private String id;

    private String datasetId;

    private Map<String, Integer> ruleVersions;

    private Instant startedAt;

    private Instant completedAt;

    private Long executionTimeMs;

    private String status;

    private List<String> generatedFindings;

    @Indexed(unique = true)
    private String runFingerprint;

    // Default constructor
    public AnalysisRun() {
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public Map<String, Integer> getRuleVersions() {
        return ruleVersions;
    }

    public void setRuleVersions(Map<String, Integer> ruleVersions) {
        this.ruleVersions = ruleVersions;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getGeneratedFindings() {
        return generatedFindings;
    }

    public void setGeneratedFindings(List<String> generatedFindings) {
        this.generatedFindings = generatedFindings;
    }

    public String getRunFingerprint() {
        return runFingerprint;
    }

    public void setRunFingerprint(String runFingerprint) {
        this.runFingerprint = runFingerprint;
    }
}