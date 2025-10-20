package com.pixeldoctrine.smhi_assignment.repository.apikey;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pixeldoctrine.smhi_assignment.dto.ApiKeyDTO;

public interface ApiKeyRepository extends MongoRepository<ApiKeyDTO, String> {

    ApiKeyDTO findByApiKey(String apiKey);
}
