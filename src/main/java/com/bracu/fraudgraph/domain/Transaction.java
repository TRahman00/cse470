package com.bracu.fraudgraph.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction (Figure 2). STUB for Sprint 1 so F13 compiles on its own.
 * When Tashfin's real Transaction is merged, delete this file and keep the same getters.
 * senderAccountKey / receiverAccountKey = "providerCode:sampleAccountId" (SRS 2.6).
 */
public class Transaction {
    private final String transactionId;
    private final String senderAccountKey;
    private final String receiverAccountKey;
    private final BigDecimal amount;
    private final LocalDateTime occurredAt;
    private final String status;

    public Transaction(String transactionId, String senderAccountKey, String receiverAccountKey,
                       BigDecimal amount, LocalDateTime occurredAt, String status) {
        this.transactionId = transactionId;
        this.senderAccountKey = senderAccountKey;
        this.receiverAccountKey = receiverAccountKey;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.status = status;
    }

    public String getTransactionId() { return transactionId; }
    public String getSenderAccountKey() { return senderAccountKey; }
    public String getReceiverAccountKey() { return receiverAccountKey; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getStatus() { return status; }

    public boolean isCompleted() { return "Completed".equalsIgnoreCase(status); }
}
