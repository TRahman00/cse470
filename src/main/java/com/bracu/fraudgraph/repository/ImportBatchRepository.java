package com.bracu.fraudgraph.repository;

import com.bracu.fraudgraph.domain.ImportBatch;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ImportBatchRepository extends MongoRepository<ImportBatch, String> {

    // Spring Data writes the query from the method name.

    /** An earlier upload of the same file that was saved (status not REJECTED). */
    Optional<ImportBatch> findFirstByFileHashAndStatusNot(String fileHash, String status);

    List<ImportBatch> findAllByOrderByImportedAtDesc();
}
