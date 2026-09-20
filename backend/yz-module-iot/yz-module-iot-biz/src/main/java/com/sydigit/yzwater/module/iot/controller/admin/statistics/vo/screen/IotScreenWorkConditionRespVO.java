package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 工况统计 Response VO")
@Data
public class IotScreenWorkConditionRespVO {

    @Schema(description = "站点值", requiredMode = Schema.RequiredMode.REQUIRED, example = "320902102")
    private String stationId;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "沙河")
    private String stationName;

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1#主机")
    private String deviceName;

    @Schema(description = "设备类型", example = "7")
    private Integer deviceType;

    @Schema(description = "设备类型名称", example = "闸门")
    private String deviceTypeName;

    @Schema(description = "时间", example = "2026-03-26 14:20:00")
    private String time;

    @Schema(description = "有功功率(Kw)")
    private Object activePowerKw;

    @Schema(description = "无功功率(Kvar)")
    private Object reactivePowerKvar;

    @Schema(description = "功率因素")
    private Object powerFactor;

    @Schema(description = "频率")
    private Object frequency;

    @Schema(description = "AB相电压")
    private Object abVoltage;

    @Schema(description = "BC相电压")
    private Object bcVoltage;

    @Schema(description = "CA相电压")
    private Object caVoltage;

    @Schema(description = "A相电流")
    private Object aCurrent;

    @Schema(description = "B相电流")
    private Object bCurrent;

    @Schema(description = "C相电流")
    private Object cCurrent;

    @Schema(description = "主机定子U1温度")
    private Object aStatorTemp1;

    @Schema(description = "主机定子V1温度")
    private Object bStatorTemp1;

    @Schema(description = "主机定子W1温度")
    private Object cStatorTemp1;

    @Schema(description = "主机定子U2温度")
    private Object aStatorTemp2;

    @Schema(description = "主机定子V2温度")
    private Object bStatorTemp2;

    @Schema(description = "主机定子W2温度")
    private Object cStatorTemp2;

    @Schema(description = "全开状态")
    private Object isGateOpenAll;

    @Schema(description = "全关状态")
    private Object isGateCloseAll;

    @Schema(description = "上升状态")
    private Object isGateUp;

    @Schema(description = "下降状态")
    private Object isGateDown;

    @Schema(description = "故障状态")
    private Object isGateFailure;

    @Schema(description = "电源合闸")
    private Object isGatePowerOn;

    @Schema(description = "闸门开度")
    private Object floodgateOpening;

    @Schema(description = "水位")
    private Object waterLevel;

    @Schema(description = "扬压力")
    private Object pressValue;
}
