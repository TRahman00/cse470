
package com.mfsfraudgraph;

import com.mfsfraudgraph.model.AnalysisRun;
import com.mfsfraudgraph.repository.AnalysisRunRepository;
import com.mfsfraudgraph.service.AnalysisRunService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnalysisRunServiceTest {

        private AnalysisRunRepository repository;
        private AnalysisRunService service;

        @BeforeEach
        void setUp() {
                repository = mock(AnalysisRunRepository.class);
                service = new AnalysisRunService(repository);
        }

        // TEST 1: Successfully start a new analysis run
        @Test
        void shouldStartNewAnalysisRun() {

                Map<String, Integer> versions = Map.of(
                                "RULE_01", 1,
                                "RULE_02", 2);

                when(repository.findByRunFingerprint(any()))
                                .thenReturn(Optional.empty());

                when(repository.save(any(AnalysisRun.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                AnalysisRun result = service.startRun("DATASET_001", versions);

                assertEquals("DATASET_001", result.getDatasetId());
                assertEquals("RUNNING", result.getStatus());
                assertNotNull(result.getStartedAt());
                assertNotNull(result.getRunFingerprint());
                assertTrue(result.getGeneratedFindings().isEmpty());

                verify(repository).save(any(AnalysisRun.class));
        }

        // TEST 2: Return existing run for identical dataset and rule versions
        @Test
        void shouldReturnExistingRunForDuplicateRequest() {

                Map<String, Integer> versions = Map.of(
                                "RULE_01", 1,
                                "RULE_02", 2);

                AnalysisRun existingRun = new AnalysisRun();
                existingRun.setDatasetId("DATASET_001");
                existingRun.setRuleVersions(versions);
                existingRun.setStatus("COMPLETED");
                existingRun.setRunFingerprint("existing-fingerprint");

                when(repository.findByRunFingerprint(any()))
                                .thenReturn(Optional.of(existingRun));

                AnalysisRun result = service.startRun("DATASET_001", versions);

                assertSame(existingRun, result);
                assertEquals("COMPLETED", result.getStatus());

                verify(repository, never()).save(any(AnalysisRun.class));
        }

        // TEST 3: Reject an empty dataset ID
        @Test
        void shouldRejectEmptyDatasetId() {

                Map<String, Integer> versions = Map.of("RULE_01", 1);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.startRun(" ", versions));

                verifyNoInteractions(repository);
        }

        // TEST 4: Reject empty rule versions
        @Test
        void shouldRejectEmptyRuleVersions() {

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.startRun("DATASET_001", Map.of()));

                verifyNoInteractions(repository);
        }

        // TEST 5: Reject invalid rule version
        @Test
        void shouldRejectInvalidRuleVersion() {

                Map<String, Integer> versions = Map.of("RULE_01", 0);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.startRun("DATASET_001", versions));

                verifyNoInteractions(repository);
        }

        // TEST 6: Complete an analysis run successfully
        @Test
        void shouldCompleteAnalysisRun() {

                AnalysisRun run = new AnalysisRun();
                run.setDatasetId("DATASET_001");
                run.setStatus("RUNNING");
                run.setStartedAt(Instant.now().minusSeconds(10));

                when(repository.findById("RUN_001"))
                                .thenReturn(Optional.of(run));

                when(repository.save(any(AnalysisRun.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                List<String> findings = List.of(
                                "FINDING_001",
                                "FINDING_002");

                AnalysisRun result = service.completeRun("RUN_001", findings);

                assertEquals("COMPLETED", result.getStatus());
                assertEquals(findings, result.getGeneratedFindings());
                assertNotNull(result.getCompletedAt());
                assertNotNull(result.getExecutionTimeMs());
                assertTrue(result.getExecutionTimeMs() >= 0);

                verify(repository).save(run);
        }

        // TEST 7: Reject completion of an already completed run
        @Test
        void shouldRejectAlreadyCompletedRun() {

                AnalysisRun run = new AnalysisRun();
                run.setStatus("COMPLETED");

                when(repository.findById("RUN_001"))
                                .thenReturn(Optional.of(run));

                assertThrows(
                                IllegalStateException.class,
                                () -> service.completeRun(
                                                "RUN_001",
                                                List.of("FINDING_001")));

                verify(repository, never()).save(any(AnalysisRun.class));
        }

        // TEST 8: Reject completion when run does not exist
        @Test
        void shouldRejectNonexistentRun() {

                when(repository.findById("UNKNOWN"))
                                .thenReturn(Optional.empty());

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.completeRun(
                                                "UNKNOWN",
                                                List.of("FINDING_001")));

                verify(repository, never()).save(any(AnalysisRun.class));
        }

        // TEST 9: Handle simultaneous duplicate insertion safely
        @Test
        void shouldHandleDuplicateKeyException() {

                Map<String, Integer> versions = Map.of(
                                "RULE_01", 1,
                                "RULE_02", 2);

                AnalysisRun existingRun = new AnalysisRun();
                existingRun.setDatasetId("DATASET_001");
                existingRun.setStatus("RUNNING");

                when(repository.findByRunFingerprint(any()))
                                .thenReturn(Optional.empty())
                                .thenReturn(Optional.of(existingRun));

                when(repository.save(any(AnalysisRun.class)))
                                .thenThrow(new DuplicateKeyException("Duplicate fingerprint"));

                AnalysisRun result = service.startRun("DATASET_001", versions);

                assertSame(existingRun, result);

                verify(repository, times(2))
                                .findByRunFingerprint(any());

                verify(repository).save(any(AnalysisRun.class));
        }
}
