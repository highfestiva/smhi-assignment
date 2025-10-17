package com.pixeldoctrine.smhi_assignment.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.pixeldoctrine.smhi_assignment.service.MetrologyIngestService;

/**
 * Runs data ingestion at startup (to ensure we always have data).
 */
@Component
public class Startup {

    private static Logger log = LoggerFactory.getLogger(Startup.class);

    private final MetrologyIngestService ingester;

    public Startup(MetrologyIngestService ingester) {
        this.ingester = ingester;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("ApplicationReadyEvent fired — starting data ingestion");
        ingester.ingest();
    }
}
