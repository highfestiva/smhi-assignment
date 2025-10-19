package com.pixeldoctrine.smhi_assignment.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pixeldoctrine.smhi.MetObsSampleData;
import com.pixeldoctrine.smhi.MetObsSampleValueType;
import com.pixeldoctrine.smhi_assignment.dto.MeasurementDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationMeasurementsDTO;

/**
 * Maps data from SMHI's format into our internal data structure.
 */
@Service
public class TransformService {

    private static Logger log = LoggerFactory.getLogger(TransformService.class);
    private static Map<String, String> PARAM_NAME_TO_TYPE = Map.of("Byvind", "gustWind", "Lufttemperatur", "airTemp");
    private static Map<String, String> PERIOD_KEY_TO_INTERVAL = Map.of("latest-day", "1d", "latest-hour", "1h");
    private static Map<String, String> UNIT_TO_UNIT = Map.of("meter per sekund", "m/s", "celsius", "°C");

    public List<StationMeasurementsDTO> transform(Collection<MetObsSampleData> data) {

        Map<String, StationMeasurementsDTO> stationIdToMeasurement = new HashMap<>();
        for (var sample : data) {
            var measurement = transformToMeasurement(sample);
            if (measurement == null) {
                continue;
            }
            var stationId = sample.getStation().getKey();
            var stationMeasurements = stationIdToMeasurement.get(stationId);
            if (stationMeasurements == null) {
                var measurementList = new ArrayList<MeasurementDTO>();
                stationMeasurements = new StationMeasurementsDTO(
                    null,
                    sample.getStation().getName(),
                    stationId,
                    measurementList);
                stationIdToMeasurement.put(stationId, stationMeasurements);
            }
            stationMeasurements.measurements().add(measurement);
        }

        var stationList = new ArrayList<>(stationIdToMeasurement.values());
        return stationList;
    }

    public MeasurementDTO transformToMeasurement(MetObsSampleData sampleData) {

        var recentValue = getMostRecentValue(sampleData.getValue());
        if (recentValue == null) {
            // log.info(
            //     "Recent value missing: param {}, station {}, interval {}",
            //     sampleData.getParameter().getKey(),
            //     sampleData.getStation().getKey(),
            //     sampleData.getPeriod().getKey());
            return null;
        }
        return new MeasurementDTO(
                PARAM_NAME_TO_TYPE.get(sampleData.getParameter().getName()),
                PERIOD_KEY_TO_INTERVAL.get(sampleData.getPeriod().getKey()),
                recentValue.getValue(),
                UNIT_TO_UNIT.get(sampleData.getParameter().getUnit()),
                recentValue.getQuality(),
                recentValue.getDate().toGregorianCalendar().toZonedDateTime().toInstant(),
                List.of(sampleData.getParameter().getName(), sampleData.getParameter().getSummary()));
    }

    private MetObsSampleValueType getMostRecentValue(List<MetObsSampleValueType> values) {
        XMLGregorianCalendar latestDate = null;
        MetObsSampleValueType latestValue = null;
        for (var value : values) {
            if (latestDate == null || value.getDate().compare(latestDate) == DatatypeConstants.GREATER) {
                latestDate = value.getDate();
                latestValue = value;
            }
        }
        return latestValue;
    }
}
