package com.pixeldoctrine.smhi_assignment.dto;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

@Document("api_keys")
public record ApiKeyDTO(
    @Id String id,
    String apiKey,
    String owner,
    List<GrantedAuthority> roles,
    String auxillaryData,
    List<String> permittedPathRegex
) {};
