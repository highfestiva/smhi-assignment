package com.pixeldoctrine.smhi_assignment.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.glassfish.jaxb.runtime.v2.ContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pixeldoctrine.smhi.Category;
import com.pixeldoctrine.smhi.LinkType;
import com.pixeldoctrine.smhi.MetObsDataType;
import com.pixeldoctrine.smhi.MetObsIntervalData;
import com.pixeldoctrine.smhi.MetObsParameter;
import com.pixeldoctrine.smhi.MetObsPeriod;
import com.pixeldoctrine.smhi.MetObsSampleData;
import com.pixeldoctrine.smhi.MetObsSampleValueType;
import com.pixeldoctrine.smhi.MetObsStation;
import com.pixeldoctrine.smhi.Version;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * Downloads and returns relevant SMHI data.
 */
@Service
public class SmhiDownloadService {

    private static final Logger log = LoggerFactory.getLogger(SmhiDownloadService.class);

    @Value("${smhi.download.root.url}")
    private String rootUrl;

    @Value("${smhi.api.version}")
    private String apiVersion;

    @Value("${smhi.api.properties}")
    private Set<String> apiProperties;

    @Value("${smhi.periods}")
    private Set<String> periods;

    private final RestTemplate restTemplate = new RestTemplate();

    public Collection<MetObsSampleData> download() throws JAXBException {
        log.info("Starting SMHI data download from {}", rootUrl);

        var root = read(rootUrl, Category.class);

        String versionUrl = root.getVersion().stream()
                .filter(link -> link.getKey().equals(apiVersion))
                .map(link -> getXmlUrl(link.getLink()))
                .findFirst()
                .get(); // if NoSuchElementException, there's no fallback anyway

        var version = read(versionUrl, Version.class);

        var resources = version.getResource().stream()
                .filter(resource -> apiProperties.contains(resource.getKey()))
                .toList();

        List<MetObsSampleData> params = new ArrayList<>();
        for (var resource : resources) {
            String parameterUrl = getXmlUrl(resource.getLink());

            var parameter = read(parameterUrl, MetObsParameter.class);

            log.info("downloading stations for parameter {}", resource.getKey());

            var stationDataList = downloadStationsData(parameter);

            params.addAll(stationDataList);
        }

        log.info("Finished SMHI data download");

        return params;
    }

    /**
     * Go through all active stations, and download their data
     * @param parameter A specific parameter, such as "21"="Byvind".
     * @return The list of all the relevant sample data ("latest-day" and "latest-hour")
     */
    private Collection<MetObsSampleData> downloadStationsData(MetObsParameter parameter) {
        // use parallelization to speed up many HTTP queries
        return parameter.getStation().parallelStream()
                .filter(station -> station.isActive())
                .map(station -> getXmlUrl(station.getLink()))
                .map(stationUrl -> readOptional(stationUrl, MetObsStation.class))
                .filter(stationResult -> stationResult.isPresent())
                .map(stationResult -> stationResult.get())
                .map(stationResult -> downloadStationData(stationResult))
                .flatMap(stationData -> stationData.stream())
                .toList();
    }

    /**
     * Download the latest-day and latest-hour data for the given station.
     */
    private Collection<MetObsSampleData> downloadStationData(MetObsStation station) {
        return station.getPeriod().stream()
                .filter(period -> periods.contains(period.getKey()))
                .map(period -> getXmlUrl(period.getLink()))
                .map(periodUrl -> readOptional(periodUrl, MetObsPeriod.class))
                .filter(periodResult -> periodResult.isPresent())
                .map(periodResult -> periodResult.get())
                .map(periodResult -> getXmlUrl(periodResult.getData().get(0).getLink()))
                .map(dataUrl -> readOptional(dataUrl, MetObsDataType.class))
                .filter(dataResult -> dataResult.isPresent())
                .map(dataResult -> dataResult.get())
                .map(data -> convertToSampleData(data))
                .toList();
    }

    /**
     * Some observations are stored in an "interval", others in a "sample." The former has "from" and "to" timestamps, the latter
     * only one specific timestamp. Since the sample data works well for our case, we just move the data over if it's an interval.
     */
    private MetObsSampleData convertToSampleData(MetObsDataType data) {
        if (data instanceof MetObsSampleData) {
            return (MetObsSampleData) data;
        }
        var sampleData = new MetObsSampleData();
        sampleData.setParameter(data.getParameter());
        sampleData.setPeriod(data.getPeriod());
        sampleData.setStation(data.getStation());
        sampleData.setUpdated(data.getUpdated());
        if (data instanceof MetObsIntervalData) {
            var intervalData = (MetObsIntervalData) data;
            var sampleValues = intervalData.getValue().stream()
                .map(intervalValue -> {
                    var sampleValue = new MetObsSampleValueType();
                    sampleValue.setDate(intervalValue.getTo());
                    sampleValue.setQuality(intervalValue.getQuality());
                    sampleValue.setValue(intervalValue.getValue());
                    return sampleValue;
                })
                .toList();
            sampleData.getValue().addAll(sampleValues);
        }
        return sampleData;
    }

    private <T> Optional<T> readOptional(String url, Class<T> clazz) {
        try {
            T r = read(url, clazz);
            return Optional.of(r);
        } catch (JAXBException ex) {
            log.error("XML parsing error", ex);
            return Optional.empty();
        }
    }

    private <T> T read(String url, Class<T> clazz) throws JAXBException {

        // log.info("Downloading from {}", url);

        String xml = restTemplate.getForObject(url, String.class);

        // fix for JAXB class path problem when running without maven
        JAXBContext jaxbContext = ContextFactory.createContext(new Class[]{clazz}, null);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        @SuppressWarnings("unchecked")
        T data = (T) unmarshaller.unmarshal(new StringReader(xml));
        return data;
    }

    private String getXmlUrl(Collection<LinkType> links) {
        return links.stream()
                .filter(this::isXmlLink)
                .map(link -> link.getHref())
                .findFirst()
                .get(); // if no XML link, we can't parse anyway
    }

    private boolean isXmlLink(LinkType link) {
        return link.getType().equals("application/xml");
    }
}
