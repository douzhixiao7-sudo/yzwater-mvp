package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏站点主机状态统计 Response VO")
@Data
public class IotScreenStationHostStatusRespVO {

    @Schema(description = "站点值（字典 iot_zd_sbzd）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String stationId;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号站")
    private String stationName;

    @Schema(description = "运行主机数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long runningCount;

    @Schema(description = "停止主机数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long stopCount;
}
