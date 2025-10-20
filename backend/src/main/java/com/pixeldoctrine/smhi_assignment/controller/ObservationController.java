package com.pixeldoctrine.smhi_assignment.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixeldoctrine.smhi_assignment.dto.PaginationDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationsObservationsPageDTO;
import com.pixeldoctrine.smhi_assignment.repository.station.StationObservationRepositoryCustom;
import com.pixeldoctrine.smhi_assignment.service.ObservationIntervalFilter;
import com.pixeldoctrine.smhi_assignment.util.Sanitizer;

@RestController
@RequestMapping("/api/v1")
public class ObservationController {

    @Value("${app.default.interval}")
    private String defaultInterval;

    @Value("${app.default.page.size}")
    private int defaultPageSize;

    private StationObservationRepositoryCustom repo;
    private ObservationIntervalFilter filterer;

    public ObservationController(
        StationObservationRepositoryCustom repo,
        ObservationIntervalFilter obsIntFilter
    ) {
        this.repo = repo;
        this.filterer = obsIntFilter;
    }

    @GetMapping("/observations")
    public StationsObservationsPageDTO getObservations(
        @RequestParam(value = "station", required = false) String stationId,
        @RequestParam(value = "interval", required = false) String interval,
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "size", required = false) Integer pageSize
    ) {
        stationId = Sanitizer.sanitizeShort(stationId);
        interval = Sanitizer.sanitizeShort(interval);
        if (StringUtils.isBlank(interval)) {
            interval = defaultInterval;
        }
        pageSize = pageSize != null ? pageSize : defaultPageSize;
        var pagination = new PaginationDTO(page, pageSize);
        var stations = repo.findByStationIdAndDuration(stationId, interval, pagination);
        if (stations == null) {
            return new StationsObservationsPageDTO(
                0,
                pagination.getPage(),
                pagination.getSize(),
                List.of()
            );
        }
        filterer.filterObservationsWithoutInterval(interval, stations.stations());
        return new StationsObservationsPageDTO(
            stations.totalStations(),
            pagination.getPage(),
            pagination.getSize(),
            stations.stations()
        );
    }
}
