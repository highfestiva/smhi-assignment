package com.pixeldoctrine.smhi_assignment.dto;

import java.time.Instant;
import java.util.List;

public record MeasurementDTO(String type, String interval, String value, String unit, String quality, Instant updatedAt, List<String> originalDescription) {}
