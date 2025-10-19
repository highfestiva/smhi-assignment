package com.pixeldoctrine.smhi_assignment.dto;

import java.util.Collection;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document("station_measurements")
public record StationMeasurementsDTO(
    @Id @JsonIgnore String id, // pragmatic, don't show to consumer
    String stationName,
    String stationId,
    Collection<MeasurementDTO> measurements
) {}
