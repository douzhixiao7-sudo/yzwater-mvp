package com.sydigit.yzwater.module.iot.dal.dataobject.dispatchmanage;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 调度指令 DO
 */
@TableName("yz_dispatch_instruction")
@KeySequence("yz_dispatch_instruction_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDispatchInstructionDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 调度编号
     */
    private String instructionNo;

    /**
     * 指令名称
     */
    private String instructionName;

    /**
     * 发令单位
     */
    private String issueOrgName;

    /**
     * 发令人用户 ID
     */
    private Long issueUserId;

    /**
     * 发令人姓名
     */
    private String issueUserName;

    /**
     * 所属站点
     */
    private String stationId;

    /**
     * 调度内容
     */
    private String instructionContent;

    /**
     * 调度方案 ID 列表（逗号分隔）
     */
    private String planIds;

    /**
     * 调度方案快照 JSON
     */
    private String planSnapshotJson;

    /**
     * 计划完成时间
     */
    private LocalDateTime plannedFinishTime;

    /**
     * 接收单位 ID
     */
    private Long receiverDeptId;

    /**
     * 接收单位名称
     */
    private String receiverDeptName;

    /**
     * 接收人 ID
     */
    private Long receiverUserId;

    /**
     * 接收人姓名
     */
    private String receiverUserName;

    /**
     * 执行人 ID
     */
    private Long executorUserId;

    /**
     * 执行人姓名
     */
    private String executorUserName;

    /**
     * 接收状态（0待接收 1已接收）
     */
    private Integer receiveStatus;

    /**
     * 接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 执行状态（1待执行 2已执行 3已逾期）
     */
    private Integer status;

    /**
     * 反馈内容
     */
    private String feedbackContent;

    /**
     * 反馈备注
     */
    private String feedbackRemark;

    /**
     * 反馈提交人用户 ID
     */
    private Long feedbackSubmitUserId;

    /**
     * 反馈提交人姓名
     */
    private String feedbackSubmitUserName;

    /**
     * 反馈提交时间
     */
    private LocalDateTime feedbackSubmitTime;

    /**
     * 操作票链接
     */
    private String operationTicketUrl;

    /**
     * 备注
     */
    private String remark;
}
