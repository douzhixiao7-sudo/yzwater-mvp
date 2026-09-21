package com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 巡检计划 Response VO")
@Data
public class IotInspectionPlanRespVO {

    @Schema(description = "计划 ID")
    private Long id;

    @Schema(description = "计划名称")
    private String planName;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "巡检对象类型（1 设备，2 位置）")
    private Integer objectType;

    @Schema(description = "巡检标准 ID")
    private Long standardId;

    @Schema(description = "巡检标准名称")
    private String standardName;

    @Schema(description = "巡检线路 ID")
    private Long lineId;

    @Schema(description = "巡检线路名称")
    private String lineName;

    @Schema(description = "计划开始时间")
    private LocalDateTime planStartDate;

    @Schema(description = "计划结束时间")
    private LocalDateTime planEndDate;

    @Schema(description = "计划周期单位")
    private String cycleUnit;

    @Schema(description = "计划周期值")
    private Integer cycleValue;

    @Schema(description = "计划状态（0 未开始，1 未完成，2 已完成，3 已逾期）")
    private Integer planStatus;

    @Schema(description = "执行人用户 ID")
    private Long executorUserId;

    @Schema(description = "执行人姓名")
    private String executorName;

    @Schema(description = "执行班组 ID")
    private Long executeDeptId;

    @Schema(description = "执行班组名称")
    private String executeDeptName;

    @Schema(description = "巡检对象数量")
    private Integer targetCount;

    @Schema(description = "巡检对象名称列表")
    private List<String> targetNames;

    @Schema(description = "巡检对象 ID 列表")
    private List<Long> targetIds;

    @Schema(description = "巡检对象详情")
    private List<IotInspectionPlanTargetRespVO> targets;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
