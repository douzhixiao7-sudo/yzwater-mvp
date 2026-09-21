package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏工程监测闸门状态汇总 Response VO")
@Data
public class IotScreenEngineeringGateSummaryRespVO {

    @Schema(description = "站点值（字典 iot_zd_sbzd）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String stationId;

    @Schema(description = "站点名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号站")
    private String stationName;

    @Schema(description = "闸门总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long gateTotalCount;

    @Schema(description = "全关总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Long gateCloseAllCount;

    @Schema(description = "全开总数", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    private Long gateOpenAllCount;
}

