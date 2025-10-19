package com.pixeldoctrine.smhi_assignment.repository;

import java.util.Collection;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.pixeldoctrine.smhi_assignment.dto.StationMeasurementsDTO;

@Repository
public class StationMeasurementRepositoryCustomImpl implements StationMeasurementRepositoryCustom {

    private static Logger log = LoggerFactory.getLogger(StationMeasurementRepositoryCustomImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * We use bulk write for efficiency.
     */
    public BulkWriteResult saveAllStations(Collection<StationMeasurementsDTO> stationMeasurements) {

        log.info("Saving {} stations", stationMeasurements.size());

        var bulkOps = stationMeasurements.stream()
                .map(stationMeasurement -> 
                        new UpdateOneModel<Document>(
                            Filters.eq("stationId", stationMeasurement.stationId()),
                            new Document("$set", stationMeasurement),
                            new UpdateOptions().upsert(true)
                        )
                )
                .toList();

        MongoCollection<Document> collection = mongoTemplate.getCollection("station_measurements");
        return collection.bulkWrite(bulkOps);
    }
}
