package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 大屏闸门电参快照 Response VO")
@Data
public class IotScreenGateMetricSnapshotRespVO {

    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1#闸门")
    private String deviceName;

    @JsonProperty("iA")
    @Schema(description = "A相电流")
    private IotScreenMetricValueRespVO ia;

    @JsonProperty("iB")
    @Schema(description = "B相电流")
    private IotScreenMetricValueRespVO ib;

    @JsonProperty("iC")
    @Schema(description = "C相电流")
    private IotScreenMetricValueRespVO ic;

    @Schema(description = "有功功率")
    private IotScreenMetricValueRespVO p;

    @Schema(description = "无功功率")
    private IotScreenMetricValueRespVO q;

    @JsonProperty("uAb")
    @Schema(description = "AB相电压")
    private IotScreenMetricValueRespVO uab;

    @JsonProperty("uBc")
    @Schema(description = "BC相电压")
    private IotScreenMetricValueRespVO ubc;

    @JsonProperty("uCa")
    @Schema(description = "CA相电压")
    private IotScreenMetricValueRespVO uca;

}
