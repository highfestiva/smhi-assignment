package com.pixeldoctrine.smhi_assignment.repository.apikey;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.client.result.UpdateResult;
import com.pixeldoctrine.smhi_assignment.dto.ApiKeyDTO;

@Repository
public class ApiKeyRepositoryCustomImpl implements ApiKeyRepositoryCustom {

    private MongoTemplate mongoTemplate;

    public ApiKeyRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public UpdateResult upsertKey(ApiKeyDTO apiKey) {
        Update update = new Update();
        Document updateDoc = new Document();
        mongoTemplate.getConverter().write(apiKey, updateDoc);
        updateDoc.forEach(update::set);

        return mongoTemplate.upsert(
            new Query(Criteria.where("apiKey").is(apiKey.apiKey())),
            update,
            apiKey.getClass()
        );
    }
}
