package com.bracu.fraudgraph.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * One money transfer from the CSV file, saved inside one snapshot.
 *
 * The unique index means MongoDB will refuse to save the same
 * transactionId twice in the same snapshot ("stored once" in F1).
 */
@Document("transactions")
@CompoundIndex(def = "{'snapshotId': 1, 'transactionId': 1}", unique = true)
public class Transaction {

    @Id
    private String id;              // MongoDB's own id

    private String snapshotId;      // which snapshot this row belongs to
    private String transactionId;   // the ID written in the CSV, e.g. T001

    private String senderProvider;
    private String senderAccountId;
    private String receiverProvider;
    private String receiverAccountId;

    private BigDecimal amount;      // BDT
    private LocalDateTime occurredAt;
    private String status;

    public Transaction() {
    }

    /** True if both rows have the same details (used to spot exact repeats). */
    public boolean sameDetailsAs(Transaction other) {
        return Objects.equals(transactionId, other.transactionId)
                && Objects.equals(senderProvider, other.senderProvider)
                && Objects.equals(senderAccountId, other.senderAccountId)
                && Objects.equals(receiverProvider, other.receiverProvider)
                && Objects.equals(receiverAccountId, other.receiverAccountId)
                && amount.compareTo(other.amount) == 0      // so 100 and 100.00 count as equal
                && Objects.equals(occurredAt, other.occurredAt)
                && Objects.equals(status, other.status);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSnapshotId() { return snapshotId; }
    public void setSnapshotId(String snapshotId) { this.snapshotId = snapshotId; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getSenderProvider() { return senderProvider; }
    public void setSenderProvider(String senderProvider) { this.senderProvider = senderProvider; }
    public String getSenderAccountId() { return senderAccountId; }
    public void setSenderAccountId(String senderAccountId) { this.senderAccountId = senderAccountId; }
    public String getReceiverProvider() { return receiverProvider; }
    public void setReceiverProvider(String receiverProvider) { this.receiverProvider = receiverProvider; }
    public String getReceiverAccountId() { return receiverAccountId; }
    public void setReceiverAccountId(String receiverAccountId) { this.receiverAccountId = receiverAccountId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
