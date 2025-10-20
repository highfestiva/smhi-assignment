package com.pixeldoctrine.smhi_assignment.repository.station;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.pixeldoctrine.smhi_assignment.dto.PaginationDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationObservationsDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationsCountDTO;

@Repository
public class StationObservationRepositoryCustomImpl implements StationObservationRepositoryCustom {

    private static String COLL_STATION_OBSERVATIONS = "station_observations";

    private MongoTemplate mongoTemplate;

    public StationObservationRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * We use bulk write for efficiency.
     */
    public BulkWriteResult saveAllStations(Collection<StationObservationsDTO> stationObservations) {

        var bulkOps = stationObservations.stream()
                .map(stationObservation -> 
                        new UpdateOneModel<Document>(
                            Filters.eq("stationId", stationObservation.stationId()),
                            new Document("$set", stationObservation),
                            new UpdateOptions().upsert(true)
                        )
                )
                .toList();

        MongoCollection<Document> collection = mongoTemplate.getCollection(COLL_STATION_OBSERVATIONS);
        return collection.bulkWrite(bulkOps);
    }

    public StationsCountDTO findByStationIdAndDuration(String stationId, String interval, PaginationDTO pagination) {
        var coll = mongoTemplate.getCollection(COLL_STATION_OBSERVATIONS);

        // create the filter
        List<Bson> filterList = new ArrayList<>();
        if (!StringUtils.isBlank(stationId)) {
            filterList.add(Filters.eq("stationId", stationId));
        }
        if (!StringUtils.isBlank(interval)) {
            filterList.add(Filters.eq("observations.interval", interval));
        }
        Bson filters = filterList.isEmpty() ? new Document() : Filters.and(filterList);

        int totalCount = (int) coll.countDocuments(filters);
        var stations = coll.find(filters, StationObservationsDTO.class)
                .skip(pagination.getOffset())
                .limit(pagination.getSize());
        return new StationsCountDTO(totalCount, iterToList(stations));
    }

    private Collection<StationObservationsDTO> iterToList(FindIterable<StationObservationsDTO> stations) {
        List<StationObservationsDTO> list = new ArrayList<>();
        for (StationObservationsDTO doc : stations) {
            list.add(doc);
        }
        return list;
    }
}
