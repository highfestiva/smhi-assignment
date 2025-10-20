package com.pixeldoctrine.smhi_assignment.dto;

import java.util.Collection;

public record StationsObservationsPageDTO(
    int totalStations,
    int page,
    int pageSize,
    Collection<StationObservationsDTO> stations
) {}
