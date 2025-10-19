package com.pixeldoctrine.smhi_assignment.service;

import java.util.Collection;

import org.springframework.stereotype.Service;

import com.pixeldoctrine.smhi_assignment.dto.ParameterStationDataDTO;
import com.pixeldoctrine.smhi_assignment.dto.StationMeasurementsDTO;

/**
 * Uses MapStruct to transform the data into something we can use.
 */
@Service
public class TransformService {
    
    public Collection<StationMeasurementsDTO> transform(Collection<ParameterStationDataDTO> data) {
        return null;
    }
}
