package com.pixeldoctrine.smhi_assignment.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pixeldoctrine.smhi_assignment.dto.ProcessedData;

@Repository
public interface DataRepository extends MongoRepository<ProcessedData, String> {

    // Custom queries (optional)
    List<ProcessedData> findByStationId(String stationId);
}
