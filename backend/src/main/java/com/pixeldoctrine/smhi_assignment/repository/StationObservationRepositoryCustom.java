package com.pixeldoctrine.smhi_assignment.repository;

import java.util.Collection;

import com.mongodb.bulk.BulkWriteResult;
import com.pixeldoctrine.smhi_assignment.dto.PaginationDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationObservationsDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationsCountDTO;

public interface StationObservationRepositoryCustom {

    BulkWriteResult saveAllStations(Collection<StationObservationsDTO> stationsData);

    StationsCountDTO findByStationIdAndDuration(String stationId, String interval, PaginationDTO pagination);
}
