package com.pixeldoctrine.smhi_assignment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixeldoctrine.smhi_assignment.dto.PaginationDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationsPageDTO;
import com.pixeldoctrine.smhi_assignment.repository.station.StationObservationRepositoryCustom;
import com.pixeldoctrine.smhi_assignment.util.Sanitizer;

@RestController
@RequestMapping("/api/v1")
public class StationController {

    @Value("${app.default.page.size}")
    private int defaultPageSize;

    private StationObservationRepositoryCustom repo;

    public StationController(StationObservationRepositoryCustom repo) {
        this.repo = repo;
    }

    @GetMapping("/stations")
    public StationsPageDTO getStations(
        @RequestParam(value = "station", required = false) String stationId,
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "size", required = false) Integer pageSize
    ) {
        stationId = Sanitizer.sanitizeShort(stationId);
        String interval = null;
        pageSize = pageSize != null ? pageSize : defaultPageSize;
        var pagination = new PaginationDTO(page, pageSize);
        var stationsObservations = repo.findByStationIdAndDuration(stationId, interval, pagination);
        var stations = stationsObservations.stations().stream()
                .map(station -> new StationDTO(station.stationName(), station.stationId()))
                .toList();
        return new StationsPageDTO(
            stationsObservations.totalStations(),
            pagination.getPage(),
            pagination.getSize(),
            stations
        );
    }
}
