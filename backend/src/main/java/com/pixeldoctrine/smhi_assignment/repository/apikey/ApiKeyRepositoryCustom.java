package com.pixeldoctrine.smhi_assignment.repository.apikey;

import com.mongodb.client.result.UpdateResult;
import com.pixeldoctrine.smhi_assignment.dto.ApiKeyDTO;

public interface ApiKeyRepositoryCustom {

    UpdateResult upsertKey(ApiKeyDTO apiKey);
}
