package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 泵站闸门详情 Response VO")
@Data
public class IotScreenPumpStationGateDetailRespVO {

    @Schema(description = "闸门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1#闸门")
    private String gateName;

    @Schema(description = "全开状态（物模型 isGateOpenAll）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Object isGateOpenAll;

    @Schema(description = "全关状态（物模型 isGateCloseAll）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0")
    private Object isGateCloseAll;

    @Schema(description = "上升状态（物模型 isGateUp）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0")
    private Object isGateUp;

    @Schema(description = "下降状态（物模型 isGateDown）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0")
    private Object isGateDown;

    @Schema(description = "有功功率（物模型 p）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "12.5")
    private Object activePower;

    @Schema(description = "采集时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2026-03-26 14:20:00")
    private String collectTime;
}
