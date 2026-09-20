package com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 巡检任务 Response VO")
@Data
public class IotInspectionTaskRespVO {

    @Schema(description = "任务 ID")
    private Long id;

    @Schema(description = "任务编号")
    private String taskNo;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "所属闸站")
    private String stationId;

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

    @Schema(description = "巡检计划 ID")
    private Long planId;

    @Schema(description = "任务来源（1计划生成 2人工创建）")
    private Integer sourceType;

    @Schema(description = "计划开始时间")
    private LocalDateTime planStartTime;

    @Schema(description = "计划完成时间")
    private LocalDateTime planEndTime;

    @Schema(description = "执行人用户 ID")
    private Long executorUserId;

    @Schema(description = "执行人姓名")
    private String executorName;

    @Schema(description = "任务状态（0未开始 1未完成 2已完成 3已逾期）")
    private Integer taskStatus;

    @Schema(description = "异常点数")
    private Integer abnormalCount;

    @Schema(description = "结果提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "流程定义KEY")
    private String processDefinitionKey;

    @Schema(description = "流程状态（0未发起 1进行中 2已结束）")
    private Integer workflowStatus;

    @Schema(description = "流程发起时间")
    private LocalDateTime processStartTime;

    @Schema(description = "流程结束时间")
    private LocalDateTime processEndTime;

    @Schema(description = "任务描述")
    private String taskDesc;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "提交结果检查项目明细")
    private List<IotInspectionTaskSubmitResultItemVO> items;

    @Schema(description = "巡检对象数量")
    private Integer targetCount;

    @Schema(description = "巡检对象名称列表")
    private List<String> targetNames;

    @Schema(description = "巡检对象 ID 列表")
    private List<Long> targetIds;

    @Schema(description = "巡检对象详情")
    private List<IotInspectionTaskTargetRespVO> targets;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
