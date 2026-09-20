package com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 巡检任务新增/修改 Request VO")
@Data
public class IotInspectionTaskSaveReqVO {

    @Schema(description = "任务 ID", example = "1024")
    private Long id;

    @Schema(description = "所属闸站", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "所属闸站不能为空")
    private String stationId;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "巡检类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "巡检类型不能为空")
    private String inspectionType;

    @Schema(description = "巡检对象类型（1 设备，2 位置）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "巡检对象类型不能为空")
    private Integer objectType;

    @Schema(description = "巡检标准 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "巡检标准不能为空")
    private Long standardId;

    @Schema(description = "巡检线路 ID")
    private Long lineId;

    @Schema(description = "计划开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划开始时间不能为空")
    private LocalDateTime planStartTime;

    @Schema(description = "计划完成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划完成时间不能为空")
    private LocalDateTime planEndTime;

    @Schema(description = "执行人用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "执行人不能为空")
    private Long executorUserId;

    @Schema(description = "执行人姓名")
    private String executorName;

    @Schema(description = "任务描述")
    private String taskDesc;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "巡检对象列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "巡检对象不能为空")
    @Valid
    private List<IotInspectionTaskTargetSaveReqVO> targets;

}
