package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏总览数量 Response VO")
@Data
public class IotScreenOverviewCountRespVO {

    @Schema(description = "水库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Long reservoirCount;

    @Schema(description = "闸泵站数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "38")
    private Long pumpStationCount;

    @Schema(description = "省级骨干河道数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    private Long provincialBackboneRiverCount;
}
