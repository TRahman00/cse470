package com.bracu.fraudgraph.domain;

import java.time.LocalDateTime;
import java.util.List;

public class GraphSnapshot {
    private final Long graphId;
    private final LocalDateTime builtAt;
    private final List<Transaction> transactions;

    public GraphSnapshot(Long graphId, LocalDateTime builtAt, List<Transaction> transactions) {
        this.graphId = graphId;
        this.builtAt = builtAt;
        this.transactions = List.copyOf(transactions);
    }

    public Long getGraphId() { return graphId; }
    public LocalDateTime getBuiltAt() { return builtAt; }

    public List<Transaction> getCompletedTransactions() {
        return transactions.stream().filter(Transaction::isCompleted).toList();
    }
}
