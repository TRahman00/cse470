package com.bracu.fraudgraph.service;

import com.bracu.fraudgraph.domain.DatasetSnapshot;
import com.bracu.fraudgraph.domain.ImportBatch;
import com.bracu.fraudgraph.domain.ImportIssue;
import com.bracu.fraudgraph.domain.Transaction;
import com.bracu.fraudgraph.repository.DatasetSnapshotRepository;
import com.bracu.fraudgraph.repository.ImportBatchRepository;
import com.bracu.fraudgraph.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * F1: import a CSV file and save it as a new versioned snapshot.
 * F2: every row is checked before it is saved.
 */
@Service
public class ImportService {

    private final ImportBatchRepository batchRepository;
    private final DatasetSnapshotRepository snapshotRepository;
    private final TransactionRepository transactionRepository;
    private final RowValidator rowValidator;

    public ImportService(ImportBatchRepository batchRepository,
                         DatasetSnapshotRepository snapshotRepository,
                         TransactionRepository transactionRepository,
                         RowValidator rowValidator) {
        this.batchRepository = batchRepository;
        this.snapshotRepository = snapshotRepository;
        this.transactionRepository = transactionRepository;
        this.rowValidator = rowValidator;
    }

    /** F2 "identical retries are ignored": was this exact file already imported? */
    public Optional<ImportBatch> findSameFile(byte[] content) {
        return batchRepository.findFirstByFileHashAndStatusNot(sha256(content), "REJECTED");
    }

    public ImportBatch importCsv(String fileName, byte[] content) {
        ImportBatch batch = new ImportBatch(fileName, sha256(content));
        List<String> lines = readLines(content);

        // Step 1: the file needs a header line
        if (lines.isEmpty()) {
            return reject(batch, new ImportIssue(1, null, "The file is empty"));
        }
        String[] header = lines.get(0).split(",", -1);
        for (int i = 0; i < header.length; i++) {
            header[i] = header[i].trim();
        }
        for (String column : CsvRowValidator.REQUIRED_COLUMNS) {
            if (!List.of(header).contains(column)) {
                return reject(batch, new ImportIssue(1, null, "Header is missing the column " + column));
            }
        }

        // Step 2: check every row and collect the good ones
        Map<String, Transaction> goodRows = new LinkedHashMap<>(); // transactionId -> transaction
        int totalRows = 0;
        int rejectedRows = 0;
        int skippedDuplicates = 0;

        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;                               // ignore empty lines
            }
            totalRows++;
            int rowNumber = i + 1;                      // same number you see in a text editor
            Map<String, String> row = toMap(header, lines.get(i));

            // 2a. field checks (F2)
            List<ImportIssue> problems = rowValidator.validate(rowNumber, row);
            if (!problems.isEmpty()) {
                batch.getIssues().addAll(problems);
                rejectedRows++;
                continue;
            }

            // 2b. duplicate ID checks (F2)
            Transaction transaction = toTransaction(row);
            Transaction earlier = goodRows.get(transaction.getTransactionId());
            if (earlier == null) {
                goodRows.put(transaction.getTransactionId(), transaction);   // first time we see this ID
            } else if (earlier.sameDetailsAs(transaction)) {
                skippedDuplicates++;                                          // exact repeat: ignore it
            } else {
                batch.getIssues().add(new ImportIssue(rowNumber, transaction.getTransactionId(),
                        "Conflicting ID: " + transaction.getTransactionId()
                                + " was already used with different details"));
                rejectedRows++;
            }
        }

        batch.setTotalRows(totalRows);
        batch.setRejectedRows(rejectedRows);
        batch.setSkippedDuplicates(skippedDuplicates);
        batch.setSavedRows(goodRows.size());

        // Step 3: nothing valid means no snapshot
        if (goodRows.isEmpty()) {
            batch.setStatus("REJECTED");
            return batchRepository.save(batch);
        }

        // Step 4: save the batch, then a new snapshot with the next version number
        batch.setStatus(batch.getIssues().isEmpty() ? "COMPLETED" : "COMPLETED_WITH_ISSUES");
        batch = batchRepository.save(batch);

        int nextVersion = snapshotRepository.findTopByOrderByVersionDesc()
                .map(latest -> latest.getVersion() + 1)
                .orElse(1);
        DatasetSnapshot snapshot = snapshotRepository.save(
                new DatasetSnapshot(nextVersion, fileName, batch.getId(), goodRows.size()));

        // Step 5: link every good row to the snapshot and save them
        List<Transaction> transactions = new ArrayList<>(goodRows.values());
        for (Transaction t : transactions) {
            t.setSnapshotId(snapshot.getId());
        }
        transactionRepository.saveAll(transactions);

        batch.setSnapshotId(snapshot.getId());
        return batchRepository.save(batch);
    }

    public List<DatasetSnapshot> allSnapshots() {
        return snapshotRepository.findAllByOrderByVersionDesc();
    }

    public List<ImportBatch> allBatches() {
        return batchRepository.findAllByOrderByImportedAtDesc();
    }

    public Optional<ImportBatch> findBatch(String id) {
        return batchRepository.findById(id);
    }

    // ---------- small helpers ----------

    private ImportBatch reject(ImportBatch batch, ImportIssue issue) {
        batch.getIssues().add(issue);
        batch.setStatus("REJECTED");
        return batchRepository.save(batch);
    }

    /** Splits the file into lines. Removes the invisible BOM character Excel adds. */
    private static List<String> readLines(byte[] content) {
        String text = new String(content, StandardCharsets.UTF_8).replace("﻿", "");
        List<String> lines = new ArrayList<>(List.of(text.split("\\r?\\n")));
        while (!lines.isEmpty() && lines.get(0).isBlank()) {
            lines.remove(0);
        }
        return lines;
    }

    /** Turns "T001,BKASH,A001,..." into {transactionId=T001, senderProvider=BKASH, ...}. */
    private static Map<String, String> toMap(String[] header, String line) {
        String[] cells = line.split(",", -1);
        Map<String, String> row = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            row.put(header[i], i < cells.length ? cells[i].trim() : "");
        }
        return row;
    }

    /** Only called after the row passed validation, so parsing cannot fail. */
    private static Transaction toTransaction(Map<String, String> row) {
        Transaction t = new Transaction();
        t.setTransactionId(row.get("transactionId"));
        t.setSenderProvider(row.get("senderProvider"));
        t.setSenderAccountId(row.get("senderAccountId"));
        t.setReceiverProvider(row.get("receiverProvider"));
        t.setReceiverAccountId(row.get("receiverAccountId"));
        t.setAmount(new BigDecimal(row.get("amount")));
        t.setOccurredAt(LocalDateTime.parse(row.get("timestamp")));
        t.setStatus(row.get("status"));
        return t;
    }

    /** A fingerprint of the file: the same file always gives the same text. */
    static String sha256(byte[] content) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(content);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
