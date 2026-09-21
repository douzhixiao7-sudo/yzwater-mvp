package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 大屏工程监测设备状态列表 Response VO")
@Data
public class IotScreenEngineeringDeviceStatusListRespVO {

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号主机")
    private String deviceName;

    @Schema(description = "设备类型（中文）", requiredMode = Schema.RequiredMode.REQUIRED, example = "主机")
    private String deviceTypeName;

    @Schema(description = "设备经度", example = "119.123456")
    private BigDecimal longitude;

    @Schema(description = "设备纬度", example = "33.123456")
    private BigDecimal latitude;

    @Schema(description = "运行状态（仅设备类型3返回，1运行/0停止）", example = "1")
    private Integer isRunning;

    @Schema(description = "主机故障（仅设备类型3返回，按站点映射到对应物模型故障字段）", example = "0")
    private Object isFailure;

    @Schema(description = "有功功率（仅 stationId=3 且设备类型3返回，物模型 p）", example = "12.5")
    private Object p;

    @Schema(description = "闸门全开（仅设备类型7返回，物模型 isGateOpenAll）", example = "1")
    private Object isGateOpenAll;

    @Schema(description = "闸门全关（仅设备类型7返回，物模型 isGateCloseAll）", example = "0")
    private Object isGateCloseAll;

    @Schema(description = "闸门电源状态（仅设备类型7返回，物模型 isGatePowerOn）", example = "0")
    private Object isGatePowerOn;

    @Schema(description = "闸门上升（仅设备类型7返回，优先 isGateUp，缺失回退 isGateRising）", example = "0")
    private Object isGateUp;

    @Schema(description = "闸门故障（仅设备类型7返回，优先 isGateFailure，缺失回退 isGateStopped）", example = "0")
    private Object isGateFailure;

    @Schema(description = "闸门下降（仅设备类型7返回，优先 isGateDown，缺失回退 isGateLowering）", example = "0")
    private Object isGateDown;
}
