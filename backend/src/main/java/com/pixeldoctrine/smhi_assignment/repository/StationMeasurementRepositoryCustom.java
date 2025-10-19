package com.pixeldoctrine.smhi_assignment.repository;

import java.util.Collection;

import com.mongodb.bulk.BulkWriteResult;
import com.pixeldoctrine.smhi_assignment.dto.StationMeasurementsDTO;

public interface StationMeasurementRepositoryCustom {

    BulkWriteResult saveAllStations(Collection<StationMeasurementsDTO> stationsData);
}
