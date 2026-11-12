package com.bracu.fraudgraph.repository;

import com.bracu.fraudgraph.domain.DatasetSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DatasetSnapshotRepository extends MongoRepository<DatasetSnapshot, String> {

    /** The snapshot with the highest version number. */
    Optional<DatasetSnapshot> findTopByOrderByVersionDesc();

    List<DatasetSnapshot> findAllByOrderByVersionDesc();
}
