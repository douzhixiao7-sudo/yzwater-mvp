package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 幸福河湖平台-水位监测数据响应
 */
@Data
@Schema(description = "幸福河湖平台-水位监测数据响应")
public class XfhhWaterLevelDataRespVO {

    @Schema(description = "数据编号", example = "1711929600000")
    private Long dataId;

    @Schema(description = "水位计编号", example = "101")
    private Long gaugeId;

    @Schema(description = "水位值", example = "2.35")
    private BigDecimal waterLevel;

    @Schema(description = "记录时间", example = "2026-04-01 08:00:00")
    private String recordTime;

    @Schema(description = "数据来源", example = "IOT")
    private String dataSource;

    @Schema(description = "是否告警", example = "true")
    private Boolean isAlert;
}
