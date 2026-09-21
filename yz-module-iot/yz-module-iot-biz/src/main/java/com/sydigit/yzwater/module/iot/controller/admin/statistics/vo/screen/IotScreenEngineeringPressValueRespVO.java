package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏工程监测压力值明细 Response VO")
@Data
public class IotScreenEngineeringPressValueRespVO {

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号压力设备")
    private String deviceName;

    @Schema(description = "物模型压力值（pressValue / pressureValue），无值时返回空", example = "1.23")
    private Object pressValue;

    @Schema(description = "采集时间（属性最新更新时间）", example = "2026-09-04 11:30:00")
    private String collectTime;
}

