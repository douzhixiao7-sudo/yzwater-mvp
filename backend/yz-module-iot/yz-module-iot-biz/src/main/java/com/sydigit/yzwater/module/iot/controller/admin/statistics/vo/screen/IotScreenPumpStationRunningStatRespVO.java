package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏泵站开机台数统计 Response VO")
@Data
public class IotScreenPumpStationRunningStatRespVO {

    @Schema(description = "泵站名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "金斗河闸站")
    private String pumpStationName;

    @Schema(description = "设备总台数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long totalCount;

    @Schema(description = "开机台数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long runningCount;
}
