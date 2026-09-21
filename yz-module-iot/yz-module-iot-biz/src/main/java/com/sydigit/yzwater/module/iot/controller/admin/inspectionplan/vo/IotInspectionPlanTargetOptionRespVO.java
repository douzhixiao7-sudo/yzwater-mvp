package com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "IoT - 巡检计划对象下拉 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionPlanTargetOptionRespVO {

    @Schema(description = "对象 ID")
    private Long id;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象类型（1 设备，2 位置）")
    private Integer objectType;

    @Schema(description = "所属站点（设备时返回）")
    private String stationId;

}
