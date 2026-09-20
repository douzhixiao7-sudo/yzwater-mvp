package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏站点下拉 Response VO")
@Data
public class IotScreenStationOptionRespVO {

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "陈集镇")
    private String label;

    @Schema(description = "站点值", requiredMode = Schema.RequiredMode.REQUIRED, example = "320902102")
    private String value;

}

