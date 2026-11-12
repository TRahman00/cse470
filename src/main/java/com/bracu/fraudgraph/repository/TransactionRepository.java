package com.bracu.fraudgraph.repository;

import com.bracu.fraudgraph.domain.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
}
