package com.sydigit.yzwater.module.iot.service.realtimedata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 点位实时数据 DTO
 */
@Data
public class IotRealtimeDataPointDTO {

    @JsonProperty("PointName")
    private String pointName;

    @JsonProperty("Value")
    private Object value;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("Quality")
    private Long quality;

}
