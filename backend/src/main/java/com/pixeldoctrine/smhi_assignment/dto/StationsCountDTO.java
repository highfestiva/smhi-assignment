package com.pixeldoctrine.smhi_assignment.dto;

import java.util.Collection;

public record StationsCountDTO(int totalStations, Collection<StationObservationsDTO> stations) {}
