package com.bracu.fraudgraph.domain;

import java.util.Map;

/** DetectionRule (Figure 3). STUB. Labib's F17 owns versioning; F13 only reads these fields. */
public class DetectionRule {
    private final Long ruleId;
    private final int version;
    private final String ruleType;
    private final Map<String, String> parameters;
    private final boolean enabled;

    public DetectionRule(Long ruleId, int version, String ruleType, Map<String, String> parameters, boolean enabled) {
        this.ruleId = ruleId;
        this.version = version;
        this.ruleType = ruleType;
        this.parameters = Map.copyOf(parameters);
        this.enabled = enabled;
    }

    public Long getRuleId() { return ruleId; }
    public int getVersion() { return version; }
    public String getRuleType() { return ruleType; }
    public Map<String, String> getParameters() { return parameters; }
    public boolean isEnabled() { return enabled; }

    public boolean isApplicable() { return enabled; }
}
