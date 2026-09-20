package com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "IoT - 巡检计划标准下拉 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionPlanStandardOptionRespVO {

    @Schema(description = "标准 ID")
    private Long id;

    @Schema(description = "标准名称")
    private String name;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "建议周期")
    private String suggestCycleUnit;

}
