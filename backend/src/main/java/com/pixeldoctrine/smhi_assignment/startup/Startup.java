package com.pixeldoctrine.smhi_assignment.startup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.pixeldoctrine.smhi_assignment.dto.ApiKeyDTO;
import com.pixeldoctrine.smhi_assignment.repository.apikey.ApiKeyRepositoryCustom;
import com.pixeldoctrine.smhi_assignment.service.MetrologyIngestService;

/**
 * Runs data ingestion at startup (to ensure we always have data).
 */
@Component
public class Startup {

    private static final Logger log = LoggerFactory.getLogger(Startup.class);

    private final MetrologyIngestService ingester;

    private ApiKeyRepositoryCustom apiKeyRepo;

    public Startup(
        MetrologyIngestService ingester,
        ApiKeyRepositoryCustom apiKeyRepo
    ) {
        this.ingester = ingester;
        this.apiKeyRepo = apiKeyRepo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("ApplicationReadyEvent fired");

        String apiKey = "ABCDH";
        log.info("Creating dummy API key: {}", apiKey);
        apiKeyRepo.upsertKey(new ApiKeyDTO(null, apiKey, "", List.of(), "", List.of("^/api/v1/.*$")));

        log.info("Ingesting meterology data");
        ingester.ingest();
    }
}
