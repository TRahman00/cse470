
package com.mfsfraudgraph.controller;

import com.mfsfraudgraph.model.AnalysisRun;
import com.mfsfraudgraph.repository.AnalysisRunRepository;
import com.mfsfraudgraph.service.AnalysisRunService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis-runs")
public class AnalysisRunController {

    private final AnalysisRunService analysisRunService;
    private final AnalysisRunRepository analysisRunRepository;

    public AnalysisRunController(
            AnalysisRunService analysisRunService,
            AnalysisRunRepository analysisRunRepository) {
        this.analysisRunService = analysisRunService;
        this.analysisRunRepository = analysisRunRepository;
    }

    // Start an analysis run
    @PostMapping("/start")
    public ResponseEntity<AnalysisRun> startRun(
            @RequestBody Map<String, Object> request) {

        String datasetId = (String) request.get("datasetId");

        @SuppressWarnings("unchecked")
        Map<String, Integer> ruleVersions = (Map<String, Integer>) request.get("ruleVersions");

        AnalysisRun run = analysisRunService.startRun(
                datasetId, ruleVersions);

        return ResponseEntity.ok(run);
    }

    // Complete an analysis run
    @PostMapping("/{id}/complete")
    public ResponseEntity<AnalysisRun> completeRun(
            @PathVariable String id,
            @RequestBody List<String> generatedFindings) {

        AnalysisRun run = analysisRunService.completeRun(
                id, generatedFindings);

        return ResponseEntity.ok(run);
    }

    // Get all analysis runs
    @GetMapping
    public ResponseEntity<List<AnalysisRun>> getAllRuns() {
        return ResponseEntity.ok(analysisRunRepository.findAll());
    }

    // Get one analysis run
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisRun> getRun(
            @PathVariable String id) {

        return analysisRunRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Handle invalid input
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidInput(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Bad Request",
                        "message", exception.getMessage()));
    }
}
