package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏工程监测流量值明细 Response VO")
@Data
public class IotScreenEngineeringFlowValueRespVO {

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "流速仪")
    private String deviceName;

    @Schema(description = "设备类型", example = "11")
    private Integer deviceType;

    @Schema(description = "设备类型名称", example = "流量计")
    private String deviceTypeName;

    @Schema(description = "实时流量值（flowMeterWaterSpeedValue），单位 m³/s", example = "12.34")
    private Object flowValue;

    @Schema(description = "采集时间", example = "2026-09-04 10:30:00")
    private String time;

}
