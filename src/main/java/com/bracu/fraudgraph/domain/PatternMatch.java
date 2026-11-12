package com.bracu.fraudgraph.domain;

import java.util.List;


public class PatternMatch {
    private final Long matchId;
    private final String patternType;
    private final List<String> orderedTransactionIds;
    private final List<Transaction> evidence;
    private final Long ruleId;
    private final int ruleVersion;
    private final String explanation;

    public PatternMatch(Long matchId, String patternType, List<Transaction> orderedEvidence,
                        Long ruleId, int ruleVersion, String explanation) {
        this.matchId = matchId;
        this.patternType = patternType;
        this.evidence = List.copyOf(orderedEvidence);
        this.orderedTransactionIds = orderedEvidence.stream().map(Transaction::getTransactionId).toList();
        this.ruleId = ruleId;
        this.ruleVersion = ruleVersion;
        this.explanation = explanation;
    }

    public Long getMatchId() { return matchId; }
    public String getPatternType() { return patternType; }
    public List<String> getOrderedTransactionIds() { return orderedTransactionIds; }
    public Long getRuleId() { return ruleId; }
    public int getRuleVersion() { return ruleVersion; }
    public String getExplanation() { return explanation; }

    public List<Transaction> getEvidenceInOrder() { return evidence; }

    public String getFingerprint() {
        return patternType + "|r" + ruleId + "v" + ruleVersion + "|" + String.join(">", orderedTransactionIds);
    }
}
