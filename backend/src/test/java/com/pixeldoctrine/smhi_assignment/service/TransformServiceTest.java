package com.pixeldoctrine.smhi_assignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pixeldoctrine.smhi.MetObsDataType;
import com.pixeldoctrine.smhi.MetObsSampleData;
import com.pixeldoctrine.smhi.MetObsSampleValueType;
import com.pixeldoctrine.smhi_assignment.dto.ObservationsDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationObservationsDTO;

public class TransformServiceTest {

    private TransformService service;

    @BeforeEach
    void setUp() {
        service = new TransformService();
    }

    @Test
    void testTransform() throws DatatypeConfigurationException {
        var samples = List.of(
            createSample("55667", "Byvind", "meter per sekund", "latest-hour"),
            createSample("55667", "Lufttemperatur", "celsius", "latest-day"),
            createSample("11111", "Lufttemperatur", "celsius", "latest-day")
        );

        List<StationObservationsDTO> stationObservations = service.transform(samples);

        assertEquals(2, stationObservations.size());
        assertEquals("55667", stationObservations.get(0).stationId());
        assertEquals("11111", stationObservations.get(1).stationId());
        assertEquals(2, stationObservations.get(0).observations().size());
        assertEquals(1, stationObservations.get(1).observations().size());
        assertEquals("gustWind", stationObservations.get(0).observations().get(0).type());
        assertEquals("airTemp", stationObservations.get(0).observations().get(1).type());
        assertEquals("°C", stationObservations.get(1).observations().get(0).unit());
    }

    @Test
    void testTransformToObservation() throws DatatypeConfigurationException {

        var sample = createSample("12345", "Byvind", "meter per sekund", "latest-hour");

        ObservationsDTO observation = service.transformToObservation(sample);

        assertNotNull(observation);
        assertEquals("gustWind", observation.type());
        assertEquals("1h", observation.interval());
        assertEquals("3.0", observation.value());
        assertEquals("m/s", observation.unit());
        assertEquals("G", observation.quality());
        assertEquals(sample.getValue().get(1).getDate().toGregorianCalendar().toZonedDateTime().toInstant(), observation.updatedAt());
        assertEquals(List.of("Byvind", "max, 1 gång/tim"), observation.originalDescription());
    }

    private MetObsSampleData createSample(String stationId, String paramName, String unit, String periodKey) throws DatatypeConfigurationException {
        MetObsSampleData sampleData = new MetObsSampleData();

        MetObsDataType.Parameter parameter = new MetObsDataType.Parameter();
        parameter.setKey("21");
        parameter.setName(paramName);
        parameter.setSummary("max, 1 gång/tim");
        parameter.setUnit(unit);
        sampleData.setParameter(parameter);

        MetObsDataType.Station station = new MetObsDataType.Station();
        station.setKey(stationId);
        station.setName("Färgelanda");
        sampleData.setStation(station);

        MetObsDataType.Period period = new MetObsDataType.Period();
        period.setKey(periodKey);
        sampleData.setPeriod(period);

        var values = List.of(getValue("2025-10-19T12:20:00Z", "2.8"), getValue("2025-10-19T12:30:00Z", "3.0"));
        sampleData.getValue().addAll(values);

        return sampleData;
    }

    private MetObsSampleValueType getValue(String isoDate, String value) throws DatatypeConfigurationException {
        MetObsSampleValueType val = new MetObsSampleValueType();
        val.setDate(getCalendar(isoDate));
        val.setQuality("G");
        val.setValue(value);
        return val;
    }

    private XMLGregorianCalendar getCalendar(String isoDate) throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(isoDate);
    }
}
