package com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "IoT - 巡检计划线路下拉 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionPlanLineOptionRespVO {

    @Schema(description = "线路 ID")
    private Long id;

    @Schema(description = "线路名称")
    private String name;

    @Schema(description = "所属闸站")
    private String stationId;

    @Schema(description = "巡检类型")
    private String inspectionType;

}
