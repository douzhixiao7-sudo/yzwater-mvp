package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏工程监测设备状态统计 Response VO")
@Data
public class IotScreenEngineeringDeviceStatusRespVO {

    @Schema(description = "站点值（字典 iot_zd_sbzd）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String stationId;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号站")
    private String stationName;

    @Schema(description = "设备总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long totalCount;

    @Schema(description = "关闭状态总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Long closedCount;

    @Schema(description = "运行状态总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    private Long runningCount;
}
