package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏水位列表 Response VO")
@Data
public class IotScreenStreamWaterRespVO {

    @Schema(description = "时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-18 16:45:00")
    private String time;

    @Schema(description = "水位（streamWater）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2.15")
    private Object waterLevel;

    @Schema(description = "站点值", requiredMode = Schema.RequiredMode.REQUIRED, example = "320902102")
    private String stationId;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "陈集镇")
    private String stationName;

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "device-water-level-01")
    private String deviceName;

}
