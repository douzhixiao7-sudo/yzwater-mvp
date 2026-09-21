package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 大屏工程监测运行统计 Response VO")
@Data
public class IotScreenEngineeringRuntimeStatRespVO {

    @Schema(description = "站点值（字典 iot_zd_sbzd）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String stationId;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号站")
    private String stationName;

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号主机")
    private String deviceName;

    @Schema(description = "开机次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long startCount;

    @Schema(description = "累计运行时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "3600")
    private BigDecimal totalStartDuration;

    @Schema(description = "本次运行时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
    private BigDecimal startDuration;

    @Schema(description = "本次运行时间采集时间（物模型属性最近一次上报时间）",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2026-03-26 14:20:00")
    private String startDurationCollectTime;
}
