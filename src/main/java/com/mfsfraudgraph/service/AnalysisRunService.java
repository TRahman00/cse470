
package com.mfsfraudgraph.service;

import com.mfsfraudgraph.model.AnalysisRun;
import com.mfsfraudgraph.repository.AnalysisRunRepository;

import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public class AnalysisRunService {

    private final AnalysisRunRepository analysisRunRepository;

    public AnalysisRunService(
            AnalysisRunRepository analysisRunRepository) {
        this.analysisRunRepository = analysisRunRepository;
    }

    // Start an analysis run or return an existing identical run
    public AnalysisRun startRun(
            String datasetId,
            Map<String, Integer> ruleVersions) {

        if (datasetId == null || datasetId.isBlank()) {
            throw new IllegalArgumentException(
                    "Dataset ID cannot be empty");
        }

        if (ruleVersions == null || ruleVersions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Rule versions cannot be empty");
        }

        for (Map.Entry<String, Integer> entry : ruleVersions.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null || entry.getValue() < 1) {
                throw new IllegalArgumentException(
                        "Rule names and versions must be valid");
            }
        }

        String fingerprint = generateFingerprint(
                datasetId, ruleVersions);

        Optional<AnalysisRun> existingRun = analysisRunRepository.findByRunFingerprint(fingerprint);

        if (existingRun.isPresent()) {
            return existingRun.get();
        }

        AnalysisRun run = new AnalysisRun();

        run.setDatasetId(datasetId);
        run.setRuleVersions(new TreeMap<>(ruleVersions));
        run.setRunFingerprint(fingerprint);
        run.setStartedAt(Instant.now());
        run.setStatus("RUNNING");
        run.setGeneratedFindings(List.of());

        try {
            return analysisRunRepository.save(run);
        } catch (DuplicateKeyException e) {
            return analysisRunRepository.findByRunFingerprint(fingerprint)
                    .orElseThrow(() -> new IllegalStateException(
                            "Duplicate run detected, but existing run could not be retrieved", e));
        }
    }

    // Complete an existing analysis run
    public AnalysisRun completeRun(
            String runId,
            List<String> generatedFindings) {

        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException(
                    "Run ID cannot be empty");
        }

        if (generatedFindings == null) {
            throw new IllegalArgumentException(
                    "Generated findings cannot be null");
        }

        AnalysisRun run = analysisRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Analysis run not found: " + runId));

        if (!"RUNNING".equals(run.getStatus())) {
            throw new IllegalStateException(
                    "Only RUNNING analysis runs can be completed");
        }

        Instant completedAt = Instant.now();

        run.setGeneratedFindings(generatedFindings);
        run.setCompletedAt(completedAt);
        run.setExecutionTimeMs(
                completedAt.toEpochMilli()
                        - run.getStartedAt().toEpochMilli());
        run.setStatus("COMPLETED");

        return analysisRunRepository.save(run);
    }

    // Generate a deterministic fingerprint
    private String generateFingerprint(
            String datasetId,
            Map<String, Integer> ruleVersions) {

        try {
            String canonicalInput = datasetId + "|"
                    + new TreeMap<>(ruleVersions).toString();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    canonicalInput.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm unavailable", e);
        }
    }
}
