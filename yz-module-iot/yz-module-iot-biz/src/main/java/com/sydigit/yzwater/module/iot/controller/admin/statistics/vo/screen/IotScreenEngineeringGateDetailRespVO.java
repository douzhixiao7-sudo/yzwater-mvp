package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏工程监测闸门状态明细 Response VO")
@Data
public class IotScreenEngineeringGateDetailRespVO {

    @Schema(description = "站点值（字典 iot_zd_sbzd）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String stationId;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号站")
    private String stationName;

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号闸门")
    private String deviceName;

    @Schema(description = "故障状态（物模型 isGateFailure）", example = "0")
    private Object isGateFailure;

    @Schema(description = "全关位（物模型 isGateCloseAll）", example = "1")
    private Object isGateCloseAll;

    @Schema(description = "全开位（物模型 isGateOpenAll）", example = "0")
    private Object isGateOpenAll;

    @Schema(description = "上升状态（物模型 isGateUp）", example = "0")
    private Object isGateUp;

    @Schema(description = "下降状态（物模型 isGateDown）", example = "0")
    private Object isGateDown;

    @Schema(description = "停止状态（物模型 isGateStop / isGateStopped）", example = "0")
    private Object isGateStop;

    @JsonProperty("iA")
    @Schema(description = "A 相电流（物模型 iA/ia 统一）", example = "12.5")
    private Object ia;

    @JsonProperty("iB")
    @Schema(description = "B 相电流（物模型 iB/ib 统一）", example = "12.5")
    private Object ib;

    @JsonProperty("iC")
    @Schema(description = "C 相电流（物模型 iC/ic 统一）", example = "12.5")
    private Object ic;

    @JsonProperty("uAb")
    @Schema(description = "AB 相电压（物模型 uAb/uab 统一）", example = "380")
    private Object uab;

    @JsonProperty("uBc")
    @Schema(description = "BC 相电压（物模型 uBc/ubc 统一）", example = "380")
    private Object ubc;

    @JsonProperty("uCa")
    @Schema(description = "CA 相电压（物模型 uCa/uAc 统一）", example = "380")
    private Object uca;

    @JsonProperty("P")
    @Schema(description = "有功功率（物模型 P/p）", example = "10.5")
    private Object p;

    @JsonProperty("Q")
    @Schema(description = "无功功率（物模型 Q/q）", example = "1.2")
    private Object q;

    @JsonProperty("PF")
    @Schema(description = "功率因数（物模型 PF/pf）", example = "0.95")
    private Object pf;

    @Schema(description = "闸门启闭机右荷重（物模型 isGateLoadWeightR / rightLoad）", example = "0")
    private Object isGateLoadWeightR;

    @Schema(description = "闸门启闭机左荷重（物模型 isGateLoadWeightL / leftLoad）", example = "0")
    private Object isGateLoadWeightL;

    @Schema(description = "DHZM挡洪闸门左荷重（物模型 floodgateLeftLoad）", example = "0")
    private Object floodgateLeftLoad;

    @Schema(description = "节制闸闸门荷重B（物模型 isGateLoadWeightB）", example = "0")
    private Object isGateLoadWeightB;

    @Schema(description = "闸门荷重（物模型 isGateLoadWeight）", example = "0")
    private Object isGateLoadWeight;

    @Schema(description = "DHZM挡洪闸门开度（物模型 floodgateOpening）", example = "0")
    private Object floodgateOpening;

    @Schema(description = "采集时间（相关属性最新更新时间）", example = "2026-09-04 11:30:00")
    private String collectTime;
}
