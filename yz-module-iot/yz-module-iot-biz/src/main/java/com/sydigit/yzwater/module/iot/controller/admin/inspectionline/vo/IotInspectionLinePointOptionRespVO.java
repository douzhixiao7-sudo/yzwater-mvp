package com.sydigit.yzwater.module.iot.controller.admin.inspectionline.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "IoT - 巡检线路点位候选 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionLinePointOptionRespVO {

    @Schema(description = "设备 ID")
    private Long deviceId;

    @Schema(description = "点位名称")
    private String pointName;

    @Schema(description = "设备标识")
    private String deviceName;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "地址")
    private String address;

}
