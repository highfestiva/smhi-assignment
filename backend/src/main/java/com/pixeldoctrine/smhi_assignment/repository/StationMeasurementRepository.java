package com.pixeldoctrine.smhi_assignment.repository;

import java.util.Collection;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pixeldoctrine.smhi_assignment.dto.StationMeasurementsDTO;

public interface StationMeasurementRepository extends MongoRepository<StationMeasurementsDTO, String> {

    Collection<StationMeasurementsDTO> findByStationId(String stationId);

    void saveAllStations(Collection<StationMeasurementsDTO> stationMeasurements);
}
