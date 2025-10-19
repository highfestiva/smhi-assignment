package com.pixeldoctrine.smhi_assignment.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

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

    private static Map<String, String> PARAM_NAME_TO_TYPE = Map.of("Byvind", "gustWind", "Lufttemperatur", "airTemp");
    private static Map<String, String> PERIOD_KEY_TO_INTERVAL = Map.of("latest-day", "1d", "latest-hour", "1h");
    private static Map<String, String> UNIT_TO_UNIT = Map.of("meter per sekund", "m/s", "celsius", "°C");

    public Collection<StationMeasurementsDTO> transform(Collection<MetObsSampleData> data) {
        return null;
    }

    public MeasurementDTO transformToMeasurement(MetObsSampleData sampleData) {
        var recentValue = getMostRecentValue(sampleData.getValue());
        return new MeasurementDTO(
            PARAM_NAME_TO_TYPE.get(sampleData.getParameter().getName()),
            PERIOD_KEY_TO_INTERVAL.get(sampleData.getPeriod().getKey()),
            recentValue.getValue(),
            UNIT_TO_UNIT.get(sampleData.getParameter().getUnit()),
            recentValue.getQuality(),
            recentValue.getDate().toGregorianCalendar().toZonedDateTime().toInstant(),
            List.of(sampleData.getParameter().getName(), sampleData.getParameter().getSummary())
        );
    }

    private MetObsSampleValueType getMostRecentValue(List<MetObsSampleValueType> values) {
        XMLGregorianCalendar latestDate = null;
        MetObsSampleValueType latestValue = null;
        for (var value : values) {
            if (latestDate == null || value.getDate().compare(latestDate) == DatatypeConstants.GREATER) {
                latestValue = value;
            }
            latestDate = value.getDate();
        }
        return latestValue;
    }
}
