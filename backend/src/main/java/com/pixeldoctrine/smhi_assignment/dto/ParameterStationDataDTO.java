package com.pixeldoctrine.smhi_assignment.dto;

import java.util.List;

import com.pixeldoctrine.smhi.MetObsParameter;
import com.pixeldoctrine.smhi.MetObsStationSetDataType;

public record ParameterStationDataDTO(MetObsParameter parameter, List<MetObsStationSetDataType> stationDataList) {}
