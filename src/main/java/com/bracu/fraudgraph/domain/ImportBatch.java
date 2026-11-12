package com.bracu.fraudgraph.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * F1: one upload of a CSV file, and what happened to it.
 *
 * status is one of:
 *   COMPLETED             - every row was saved
 *   COMPLETED_WITH_ISSUES - some rows saved, some rejected
 *   REJECTED              - nothing saved, no snapshot created
 */
@Document("import_batches")
public class ImportBatch {

    @Id
    private String id;

    private String fileName;
    private String fileHash;        // SHA-256 of the file, to notice the same file uploaded again
    private String status;
    private LocalDateTime importedAt;

    private int totalRows;          // data rows in the file
    private int savedRows;          // rows stored as transactions
    private int rejectedRows;       // rows with an issue
    private int skippedDuplicates;  // exact repeats of a row that was already saved

    private String snapshotId;      // the snapshot created by this upload (null if REJECTED)
    private List<ImportIssue> issues = new ArrayList<>();

    public ImportBatch() {
    }

    public ImportBatch(String fileName, String fileHash) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.importedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getImportedAt() { return importedAt; }
    public void setImportedAt(LocalDateTime importedAt) { this.importedAt = importedAt; }
    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getSavedRows() { return savedRows; }
    public void setSavedRows(int savedRows) { this.savedRows = savedRows; }
    public int getRejectedRows() { return rejectedRows; }
    public void setRejectedRows(int rejectedRows) { this.rejectedRows = rejectedRows; }
    public int getSkippedDuplicates() { return skippedDuplicates; }
    public void setSkippedDuplicates(int skippedDuplicates) { this.skippedDuplicates = skippedDuplicates; }
    public String getSnapshotId() { return snapshotId; }
    public void setSnapshotId(String snapshotId) { this.snapshotId = snapshotId; }
    public List<ImportIssue> getIssues() { return issues; }
    public void setIssues(List<ImportIssue> issues) { this.issues = issues; }
}
