package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 泵站主机详情 Response VO")
@Data
public class IotScreenPumpStationHostDetailRespVO {

    @Schema(description = "主机名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1#主机")
    private String hostName;

    @Schema(description = "主机状态（物模型原始值）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Object hostStatus;

    @Schema(description = "有功功率（物模型属性 p）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "12.5")
    private Object activePower;

    @Schema(description = "采集时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2026-03-26 14:20:00")
    private String collectTime;
}
