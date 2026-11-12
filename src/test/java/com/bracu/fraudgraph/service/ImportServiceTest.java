package com.bracu.fraudgraph.service;

import com.bracu.fraudgraph.domain.DatasetSnapshot;
import com.bracu.fraudgraph.domain.ImportBatch;
import com.bracu.fraudgraph.domain.Transaction;
import com.bracu.fraudgraph.repository.DatasetSnapshotRepository;
import com.bracu.fraudgraph.repository.ImportBatchRepository;
import com.bracu.fraudgraph.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * F1 and F2 tests for a whole file.
 * The repositories are Mockito "mocks" (fakes), so MongoDB does not need to be running.
 */
@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    private static final String HEADER =
            "transactionId,senderProvider,senderAccountId,receiverProvider,receiverAccountId,amount,timestamp,status\n";

    @Mock ImportBatchRepository batchRepository;
    @Mock DatasetSnapshotRepository snapshotRepository;
    @Mock TransactionRepository transactionRepository;

    @Captor ArgumentCaptor<List<Transaction>> savedTransactions;

    private ImportService service;

    @BeforeEach
    void setUp() {
        service = new ImportService(batchRepository, snapshotRepository, transactionRepository, new CsvRowValidator());

        // When the service saves something, give it an id like MongoDB would.
        lenient().when(batchRepository.save(any(ImportBatch.class))).thenAnswer(call -> {
            ImportBatch batch = call.getArgument(0);
            batch.setId("batch-1");
            return batch;
        });
        lenient().when(snapshotRepository.save(any(DatasetSnapshot.class))).thenAnswer(call -> {
            DatasetSnapshot snapshot = call.getArgument(0);
            snapshot.setId("snapshot-1");
            return snapshot;
        });
    }

    private ImportBatch importText(String csv) {
        return service.importCsv("test.csv", csv.getBytes(StandardCharsets.UTF_8));
    }

    private static String row(String id, String sender, String amount) {
        return id + ",BKASH," + sender + ",NAGAD,B1," + amount + ",2026-11-01T10:00:00,Completed\n";
    }

    // ---------- F1 ----------

    @Test
    void tenValidRowsAreStoredOnceAndLinkedToOneSnapshot() {
        StringBuilder csv = new StringBuilder(HEADER);
        for (int i = 1; i <= 10; i++) {
            csv.append(row("T" + i, "A" + i, "100"));
        }

        ImportBatch batch = importText(csv.toString());

        assertEquals("COMPLETED", batch.getStatus());
        assertEquals(10, batch.getSavedRows());
        assertEquals("snapshot-1", batch.getSnapshotId());

        verify(transactionRepository).saveAll(savedTransactions.capture());
        List<Transaction> saved = savedTransactions.getValue();
        assertEquals(10, saved.size());
        for (Transaction t : saved) {
            assertEquals("snapshot-1", t.getSnapshotId());
        }
    }

    @Test
    void firstSnapshotIsVersion1() {
        ArgumentCaptor<DatasetSnapshot> snapshot = ArgumentCaptor.forClass(DatasetSnapshot.class);

        importText(HEADER + row("T1", "A1", "100"));

        verify(snapshotRepository).save(snapshot.capture());
        assertEquals(1, snapshot.getValue().getVersion());
    }

    @Test
    void nextSnapshotIsLatestVersionPlusOne() {
        DatasetSnapshot version1 = new DatasetSnapshot(1, "old.csv", "batch-0", 5);
        when(snapshotRepository.findTopByOrderByVersionDesc()).thenReturn(Optional.of(version1));
        ArgumentCaptor<DatasetSnapshot> snapshot = ArgumentCaptor.forClass(DatasetSnapshot.class);

        importText(HEADER + row("T1", "A1", "100"));

        verify(snapshotRepository).save(snapshot.capture());
        assertEquals(2, snapshot.getValue().getVersion());
    }

    @Test
    void sameFileIsFoundByItsHash() {
        byte[] file = (HEADER + row("T1", "A1", "100")).getBytes(StandardCharsets.UTF_8);
        ImportBatch earlier = new ImportBatch("test.csv", ImportService.sha256(file));
        when(batchRepository.findFirstByFileHashAndStatusNot(ImportService.sha256(file), "REJECTED"))
                .thenReturn(Optional.of(earlier));

        assertTrue(service.findSameFile(file).isPresent());
    }

    // ---------- F2 ----------

    @Test
    void rowWithMissingSenderIsRejectedButOtherRowsAreSaved() {
        ImportBatch batch = importText(HEADER + row("T1", "A1", "100") + row("T2", "", "100"));

        assertEquals("COMPLETED_WITH_ISSUES", batch.getStatus());
        assertEquals(1, batch.getSavedRows());
        assertEquals(1, batch.getRejectedRows());
        assertEquals(3, batch.getIssues().get(0).getRowNumber());
        assertEquals("Missing senderAccountId", batch.getIssues().get(0).getReason());
    }

    @Test
    void exactRepeatOfARowIsIgnored() {
        ImportBatch batch = importText(HEADER + row("T1", "A1", "100") + row("T1", "A1", "100.00"));

        assertEquals("COMPLETED", batch.getStatus());
        assertEquals(1, batch.getSavedRows());
        assertEquals(1, batch.getSkippedDuplicates());
        assertTrue(batch.getIssues().isEmpty());
    }

    @Test
    void sameIdWithDifferentDetailsIsReported() {
        ImportBatch batch = importText(HEADER + row("T1", "A1", "100") + row("T1", "A1", "999"));

        assertEquals(1, batch.getSavedRows());
        assertEquals(1, batch.getRejectedRows());
        assertTrue(batch.getIssues().get(0).getReason().startsWith("Conflicting ID"));
    }

    @Test
    void headerWithMissingColumnRejectsTheWholeFile() {
        ImportBatch batch = importText("id,from,to\nT1,A1,B1\n");

        assertEquals("REJECTED", batch.getStatus());
        assertNull(batch.getSnapshotId());
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void fileWithNoValidRowsCreatesNoSnapshot() {
        ImportBatch batch = importText(HEADER + row("T1", "A1", "-5"));

        assertEquals("REJECTED", batch.getStatus());
        verify(snapshotRepository, never()).save(any());
        verify(transactionRepository, never()).saveAll(any());
    }
}
