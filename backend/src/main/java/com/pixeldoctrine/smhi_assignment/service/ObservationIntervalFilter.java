package com.pixeldoctrine.smhi_assignment.service;

import java.util.Collection;

import org.springframework.stereotype.Service;

import com.pixeldoctrine.smhi_assignment.dto.StationObservationsDTO;

/**
 * Filters out the observations with the wrong intervals from already loaded stations' data. For example 1h filter
 * will remove all observations that are not 1h. This filtering is applied after the database load. (Another option
 * is to store data for each interval separately. They both have their pros and cons.)
 */
@Service
public class ObservationIntervalFilter {

    public void filterObservationsWithoutInterval(String interval, Collection<StationObservationsDTO> stations) {
        for (var station : stations) {
            station.observations().removeIf(observation -> !observation.interval().equals(interval));
        }
    }
}
