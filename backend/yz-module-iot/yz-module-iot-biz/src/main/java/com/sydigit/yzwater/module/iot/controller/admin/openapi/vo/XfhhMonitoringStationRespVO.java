package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 幸福河湖平台-水位站信息响应
 */
@Data
@Schema(description = "幸福河湖平台-水位站信息响应")
public class XfhhMonitoringStationRespVO {

    @Schema(description = "站点编号", example = "station-1")
    private String stationId;

    @Schema(description = "站点名称", example = "一号水位站")
    private String stationName;

    @Schema(description = "站点位置描述", example = "仪征市真州镇")
    private String location;

    @Schema(description = "纬度", example = "32.2711")
    private BigDecimal latitude;

    @Schema(description = "经度", example = "119.1844")
    private BigDecimal longitude;
}
