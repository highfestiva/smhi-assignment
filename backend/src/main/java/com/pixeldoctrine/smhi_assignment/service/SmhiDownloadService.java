package com.pixeldoctrine.smhi_assignment.service;

import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pixeldoctrine.smhi.Category;
import com.pixeldoctrine.smhi.LinkType;

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
    private String url;

    private final RestTemplate restTemplate = new RestTemplate();

    public Object download() throws JAXBException {
        log.info("Starting SMHI data download from {}", url);

        String xml = restTemplate.getForObject(url, String.class);
        JAXBContext jaxbContext = JAXBContext.newInstance(Category.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Category data = (Category) unmarshaller.unmarshal(new StringReader(xml));

        for (LinkType link: data.getLink()) {
            log.info("Link: {}", link);
        }

        log.info("Finished SMHI data download");

        return data;
    }
}
