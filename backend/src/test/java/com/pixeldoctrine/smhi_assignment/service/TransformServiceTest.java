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

public class TransformServiceTest {

    private TransformService service;

    @BeforeEach
    void setUp() {
        service = new TransformService();
    }

    @Test
    void testTransform() {
    }

    @Test
    void testTransformToMeasurement() throws DatatypeConfigurationException {

        MetObsSampleData sampleData = new MetObsSampleData();

        MetObsDataType.Parameter parameter = new MetObsDataType.Parameter();
        parameter.setKey("21");
        parameter.setName("Byvind");
        parameter.setSummary("max, 1 gång/tim");
        parameter.setUnit("meter per sekund");
        sampleData.setParameter(parameter);

        MetObsDataType.Station station = new MetObsDataType.Station();
        station.setKey("12345");
        station.setName("Färgelanda");
        sampleData.setStation(station);

        MetObsDataType.Period period = new MetObsDataType.Period();
        period.setKey("latest-hour");
        sampleData.setPeriod(period);

        var values = List.of(getValue("2025-10-19T12:20:00Z", "2.8"), getValue("2025-10-19T12:30:00Z", "3.0"));
        sampleData.getValue().addAll(values);

        MeasurementDTO measurement = service.transformToMeasurement(sampleData);
        assertNotNull(measurement);
        assertEquals(measurement.type(), "gustWind");
        assertEquals(measurement.interval(), "1h");
        assertEquals(measurement.value(), "3.0");
        assertEquals(measurement.unit(), "m/s");
        assertEquals(measurement.quality(), "G");
        assertEquals(measurement.updatedAt(), values.get(1).getDate().toGregorianCalendar().toZonedDateTime().toInstant());
        assertEquals(measurement.originalDescription(), List.of("Byvind", "max, 1 gång/tim"));
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
