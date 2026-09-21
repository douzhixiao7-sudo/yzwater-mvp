package com.sydigit.yzwater.module.iot.gateway.protocol.genesis.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GENESIS64 实时数据项
 */
@Data
public class IotGenesisRealtimeDataItem {

    @JsonProperty("PointName")
    private String pointName;

    @JsonProperty("Value")
    private Object value;

    @JsonProperty("TimeStamp")
    private String timeStamp;

    @JsonProperty("Quality")
    private String quality;

}
