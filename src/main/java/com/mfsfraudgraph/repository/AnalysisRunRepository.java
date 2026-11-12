
package com.mfsfraudgraph.repository;

import com.mfsfraudgraph.model.AnalysisRun;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisRunRepository
        extends MongoRepository<AnalysisRun, String> {

    // Find an analysis run using its unique fingerprint
    Optional<AnalysisRun> findByRunFingerprint(String runFingerprint);

    // Find all runs belonging to a particular dataset
    List<AnalysisRun> findByDatasetId(String datasetId);

    // Find runs by their status
    List<AnalysisRun> findByStatus(String status);
}
