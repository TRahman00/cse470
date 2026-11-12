package com.bracu.fraudgraph.domain;

/**
 * F2: one problem found in the uploaded file.
 * Example: row 4, transaction T004, "Amount must be greater than 0".
 * Issues are stored inside their ImportBatch.
 */
public class ImportIssue {

    private int rowNumber;          // line number in the CSV file (header = line 1)
    private String transactionId;   // may be empty if the row had no ID
    private String reason;

    public ImportIssue() {
    }

    public ImportIssue(int rowNumber, String transactionId, String reason) {
        this.rowNumber = rowNumber;
        this.transactionId = transactionId;
        this.reason = reason;
    }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
