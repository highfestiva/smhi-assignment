package com.pixeldoctrine.smhi_assignment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pixeldoctrine.smhi_assignment.repository.station.StationObservationRepositoryCustom;

import jakarta.xml.bind.JAXBException;

/**
 * Downloads, transforms and stores the SMHI data. A simple data pipeline.
 */
@Service
public class MetrologyIngestService {

    private static Logger log = LoggerFactory.getLogger(MetrologyIngestService.class);

    private final SmhiDownloadService downloader;
    private final TransformService transformer;
    private final StationObservationRepositoryCustom repo;

    public MetrologyIngestService(SmhiDownloadService downloader,
            TransformService transformer,
            StationObservationRepositoryCustom repo) {
        this.downloader = downloader;
        this.transformer = transformer;
        this.repo = repo;
    }

    @Scheduled(cron = "${smhi.download.cron}")
    public void ingest() {
        log.info("Starting the data pipeline");
        try {
            var data = downloader.download();

            var stationsData = transformer.transform(data);

            log.info("Saving {} stations' data", stationsData.size());
            var result = repo.saveAllStations(stationsData);

            var upsertCount = result.getInsertedCount() + result.getMatchedCount();
            if (upsertCount == stationsData.size()) {
                log.info("Saved {} stations' data", stationsData.size());
            } else {
                log.error("Was not able to save all stations' data, only {}/{}",
                        upsertCount,
                        stationsData.size());
            }
        } catch (JAXBException e) {
            log.error("Error parsing SMHI's XML", e);
        }
    }
}
