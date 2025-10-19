package com.pixeldoctrine.smhi_assignment.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pixeldoctrine.smhi.Category;
import com.pixeldoctrine.smhi.LinkType;
import com.pixeldoctrine.smhi.MetObsParameter;
import com.pixeldoctrine.smhi.MetObsStationSetDataType;
import com.pixeldoctrine.smhi.Version;
import com.pixeldoctrine.smhi_assignment.dto.ParameterStationDataDTO;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * Downloads and returns relevant SMHI data.
 */
@Service
public class SmhiDownloadService {

    private static final Logger log = LoggerFactory.getLogger(SmhiDownloadService.class);

    @Value("${smhi.download.root_url}")
    private String rootUrl;

    @Value("${smhi.api_version}")
    private String apiVersion;

    @Value("${smhi.api_properties}")
    private Set<String> apiProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    public Collection<ParameterStationDataDTO> download() throws JAXBException {
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

        List<ParameterStationDataDTO> params = new ArrayList<>();
        for (var resource : resources) {
            String parameterUrl = getXmlUrl(resource.getLink());

            var parameter = read(parameterUrl, MetObsParameter.class);

            // use parallelization for many HTTP queries
            var stationDataList = parameter.getStation().parallelStream()
                    .filter(station -> station.isActive())
                    .map(station -> getXmlUrl(station.getLink()))
                    .map(stationUrl -> readOptional(stationUrl, MetObsStationSetDataType.class))
                    .filter(readResult -> readResult.isPresent())
                    .map(readResult -> readResult.get())
                    .toList();

            params.add(new ParameterStationDataDTO(parameter, stationDataList));
        }

        log.info("Finished SMHI data download");

        return params;
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
        String xml = restTemplate.getForObject(url, String.class);
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        @SuppressWarnings("unchecked")
        T data = (T) unmarshaller.unmarshal(new StringReader(xml));
        return data;
    }

    private String getXmlUrl(List<LinkType> links) {
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
