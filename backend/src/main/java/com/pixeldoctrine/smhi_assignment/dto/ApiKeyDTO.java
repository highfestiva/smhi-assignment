package com.pixeldoctrine.smhi_assignment.dto;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("api_keys")
public record ApiKeyDTO(
    @Id String id,
    String apiKey,
    String owner,
    List<String> roles,
    String auxiliaryData,
    List<String> permittedPathRegex
) {};
