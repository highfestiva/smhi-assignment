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
import com.pixeldoctrine.smhi_assignment.dto.MeasurementDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationMeasurementsDTO;

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

        List<StationMeasurementsDTO> stationMeasurements = service.transform(samples);

        assertEquals(2, stationMeasurements.size());
        assertEquals("55667", stationMeasurements.get(0).stationId());
        assertEquals("11111", stationMeasurements.get(1).stationId());
        assertEquals(2, stationMeasurements.get(0).measurements().size());
        assertEquals(1, stationMeasurements.get(1).measurements().size());
        assertEquals("gustWind", stationMeasurements.get(0).measurements().get(0).type());
        assertEquals("airTemp", stationMeasurements.get(0).measurements().get(1).type());
        assertEquals("°C", stationMeasurements.get(1).measurements().get(0).unit());
    }

    @Test
    void testTransformToMeasurement() throws DatatypeConfigurationException {

        var sample = createSample("12345", "Byvind", "meter per sekund", "latest-hour");

        MeasurementDTO measurement = service.transformToMeasurement(sample);

        assertNotNull(measurement);
        assertEquals("gustWind", measurement.type());
        assertEquals("1h", measurement.interval());
        assertEquals("3.0", measurement.value());
        assertEquals("m/s", measurement.unit());
        assertEquals("G", measurement.quality());
        assertEquals(sample.getValue().get(1).getDate().toGregorianCalendar().toZonedDateTime().toInstant(), measurement.updatedAt());
        assertEquals(List.of("Byvind", "max, 1 gång/tim"), measurement.originalDescription());
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
