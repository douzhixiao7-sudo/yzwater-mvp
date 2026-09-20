package com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "IoT - 巡检计划分页 Request VO")
@Data
public class IotInspectionPlanPageReqVO extends PageParam {

    @Schema(description = "计划名称")
    private String planName;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "巡检对象类型（1 设备，2 位置）")
    private Integer objectType;

    @Schema(description = "计划周期")
    private String cycleUnit;

    @Schema(description = "计划状态")
    private Integer planStatus;

    @Schema(description = "时间周期（yyyy-MM）")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "时间周期格式应为 yyyy-MM")
    private String cycleMonth;

}
