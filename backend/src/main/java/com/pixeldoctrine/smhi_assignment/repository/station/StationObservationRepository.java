package com.pixeldoctrine.smhi_assignment.repository.station;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pixeldoctrine.smhi_assignment.dto.StationObservationsDTO;

public interface StationObservationRepository extends MongoRepository<StationObservationsDTO, String> {
}
