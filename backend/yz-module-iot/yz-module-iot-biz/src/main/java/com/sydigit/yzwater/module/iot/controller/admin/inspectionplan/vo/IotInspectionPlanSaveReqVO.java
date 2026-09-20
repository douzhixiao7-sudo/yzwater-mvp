package com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 巡检计划新增/修改 Request VO")
@Data
public class IotInspectionPlanSaveReqVO {

    @Schema(description = "计划 ID", example = "1024")
    private Long id;

    @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "计划名称不能为空")
    private String planName;

    @Schema(description = "巡检类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "巡检类型不能为空")
    private String inspectionType;

    @Schema(description = "巡检对象类型（1 设备，2 位置）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "巡检对象类型不能为空")
    private Integer objectType;

    @Schema(description = "巡检标准 ID")
    private Long standardId;

    @Schema(description = "巡检线路 ID")
    private Long lineId;

    @Schema(description = "计划开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划开始时间不能为空")
    private LocalDateTime planStartDate;

    @Schema(description = "计划结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划结束时间不能为空")
    private LocalDateTime planEndDate;

    @Schema(description = "计划周期单位（DAY/WEEK/MONTH/QUARTER/YEAR）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "计划周期不能为空")
    private String cycleUnit;

    @Schema(description = "计划周期值")
    private Integer cycleValue;

    @Schema(description = "执行人用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "执行人不能为空")
    private Long executorUserId;

    @Schema(description = "执行人姓名")
    private String executorName;

    @Schema(description = "执行班组 ID")
    private Long executeDeptId;

    @Schema(description = "执行班组名称")
    private String executeDeptName;

    @Schema(description = "所属站点（可选，设备对象时可透传）")
    private String stationId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "巡检对象列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "巡检对象不能为空")
    @Valid
    private List<IotInspectionPlanTargetSaveReqVO> targets;

}
