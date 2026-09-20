package com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调度管理 Response VO
 */
@Schema(description = "IoT - 调度管理 Response VO")
@Data
public class IotDispatchManageRespVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "调度编号")
    private String instructionNo;

    @Schema(description = "指令名称")
    private String instructionName;

    @Schema(description = "发令单位")
    private String issueOrgName;

    @Schema(description = "发令人用户 ID")
    private Long issueUserId;

    @Schema(description = "发令人")
    private String issueUserName;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "调度内容")
    private String instructionContent;

    @Schema(description = "调度方案 ID 列表")
    private List<Long> planIds;

    @Schema(description = "调度方案名称列表")
    private List<String> planNames;

    @Schema(description = "计划完成时间")
    private LocalDateTime plannedFinishTime;

    @Schema(description = "接收单位 ID")
    private Long receiverDeptId;

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

    @Schema(description = "执行状态")
    private Integer status;

    @Schema(description = "执行状态名称")
    private String statusName;

    @Schema(description = "运行日志数量")
    private Integer runLogCount;

    @Schema(description = "操作票链接")
    private String operationTicketUrl;

    @Schema(description = "反馈内容")
    private String feedbackContent;

    @Schema(description = "反馈备注")
    private String feedbackRemark;

    @Schema(description = "反馈提交人")
    private String feedbackSubmitUserName;

    @Schema(description = "反馈提交时间")
    private LocalDateTime feedbackSubmitTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
