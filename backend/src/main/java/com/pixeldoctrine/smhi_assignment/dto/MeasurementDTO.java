package com.pixeldoctrine.smhi_assignment.dto;

import java.time.Instant;

public record MeasurementDTO(String type, String interval, String value, String unit, String quality, Instant updatedAt, String originalDescription) {}
