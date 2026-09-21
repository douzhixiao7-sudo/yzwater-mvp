package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 工况统计最新一条 Response VO")
@Data
public class IotScreenWorkConditionLatestRespVO {

    @Schema(description = "站点值", requiredMode = Schema.RequiredMode.REQUIRED, example = "320902102")
    private String stationId;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "沙河")
    private String stationName;

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1#主机")
    private String deviceName;

    @Schema(description = "时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2026-03-26 14:20:00")
    private String time;

    @Schema(description = "有功功率(Kw)")
    private IotScreenMetricValueRespVO activePowerKw;

    @Schema(description = "无功功率(Kvar)")
    private IotScreenMetricValueRespVO reactivePowerKvar;

    @Schema(description = "功率因素")
    private IotScreenMetricValueRespVO powerFactor;

    @Schema(description = "频率")
    private IotScreenMetricValueRespVO frequency;

    @Schema(description = "AB相电压")
    private IotScreenMetricValueRespVO abVoltage;

    @Schema(description = "BC相电压")
    private IotScreenMetricValueRespVO bcVoltage;

    @Schema(description = "CA相电压")
    private IotScreenMetricValueRespVO caVoltage;

    @Schema(description = "A相电流")
    private IotScreenMetricValueRespVO aCurrent;

    @Schema(description = "B相电流")
    private IotScreenMetricValueRespVO bCurrent;

    @Schema(description = "C相电流")
    private IotScreenMetricValueRespVO cCurrent;

    @Schema(description = "主机定子U1温度")
    private IotScreenMetricValueRespVO aStatorTemp1;

    @Schema(description = "主机定子V1温度")
    private IotScreenMetricValueRespVO bStatorTemp1;

    @Schema(description = "主机定子W1温度")
    private IotScreenMetricValueRespVO cStatorTemp1;

    @Schema(description = "主机定子U2温度")
    private IotScreenMetricValueRespVO aStatorTemp2;

    @Schema(description = "主机定子V2温度")
    private IotScreenMetricValueRespVO bStatorTemp2;

    @Schema(description = "主机定子W2温度")
    private IotScreenMetricValueRespVO cStatorTemp2;
}
