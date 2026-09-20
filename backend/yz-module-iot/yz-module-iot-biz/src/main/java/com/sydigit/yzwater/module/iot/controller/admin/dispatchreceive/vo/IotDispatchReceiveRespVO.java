package com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调令接受 Response VO
 */
@Schema(description = "IoT - 调令接受 Response VO")
@Data
public class IotDispatchReceiveRespVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "调度编号")
    private String instructionNo;

    @Schema(description = "指令名称")
    private String instructionName;

    @Schema(description = "发令单位")
    private String issueOrgName;

    @Schema(description = "发令人")
    private String issueUserName;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "调度内容")
    private String instructionContent;

    @Schema(description = "调度方案名称列表")
    private List<String> planNames;

    @Schema(description = "下发时间")
    private LocalDateTime issueTime;

    @Schema(description = "计划完成时间")
    private LocalDateTime plannedFinishTime;

    @Schema(description = "接收单位")
    private String receiverDeptName;

    @Schema(description = "接收人 ID")
    private Long receiverUserId;

    @Schema(description = "接收人")
    private String receiverUserName;

    @Schema(description = "执行人 ID")
    private Long executorUserId;

    @Schema(description = "执行人")
    private String executorUserName;

    @Schema(description = "接收状态（0待接收 1已接收）")
    private Integer receiveStatus;

    @Schema(description = "接收时间")
    private LocalDateTime receiveTime;

    @Schema(description = "执行状态（0待接收 1待执行 2已执行 3已逾期）")
    private Integer executionStatus;

    @Schema(description = "执行状态名称")
    private String executionStatusName;

    @Schema(description = "执行情况（1已完成 0未完成）")
    private Integer executeFlag;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "附件 URL 列表")
    private List<String> attachments;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "提交人")
    private String submitUserName;

    @Schema(description = "运行日志 ID 列表")
    private List<Long> runLogIds;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
