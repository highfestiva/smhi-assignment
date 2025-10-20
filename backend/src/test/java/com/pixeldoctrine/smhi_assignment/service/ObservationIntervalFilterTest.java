package com.pixeldoctrine.smhi_assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pixeldoctrine.smhi_assignment.dto.ObservationsDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationObservationsDTO;

public class ObservationIntervalFilterTest {

    private ObservationIntervalFilter service;

    @BeforeEach
    void setUp() {
        service = new ObservationIntervalFilter();
    }

    @Test
    void testFilterObservationsWithoutInterval() {
        var stationObservations = new StationObservationsDTO(
            null, null, null,
            new ArrayList<>(
                List.of(
                    new ObservationsDTO("", "1d", "", "", "", null, null),
                    new ObservationsDTO("", "1h", "", "", "", null, null),
                    new ObservationsDTO("", "1d", "", "", "", null, null),
                    new ObservationsDTO("", "1d", "", "", "", null, null),
                    new ObservationsDTO("", "1h", "", "", "", null, null)
                )
            )
        );

        service.filterObservationsWithoutInterval("1h", List.of(stationObservations));

        assertEquals(2, stationObservations.observations().size());
        assertEquals("1h", stationObservations.observations().get(1).interval());
    }
}
