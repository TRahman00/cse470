package com.bracu.fraudgraph.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * F1: a saved version of an imported dataset (version 1, 2, 3 ...).
 * Every saved Transaction points to the snapshot it belongs to,
 * so a new import never changes an older version.
 */
@Document("dataset_snapshots")
public class DatasetSnapshot {

    @Id
    private String id;

    @Indexed(unique = true)
    private int version;

    private LocalDateTime importedAt;
    private String fileName;
    private String batchId;
    private int transactionCount;

    public DatasetSnapshot() {
    }

    public DatasetSnapshot(int version, String fileName, String batchId, int transactionCount) {
        this.version = version;
        this.fileName = fileName;
        this.batchId = batchId;
        this.transactionCount = transactionCount;
        this.importedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public LocalDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(LocalDateTime importedAt) { this.importedAt = importedAt; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}
