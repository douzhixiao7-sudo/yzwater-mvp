package com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 巡检任务 DO
 */
@TableName("yz_equipment_inspection_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionTaskDO extends TenantBaseDO {

    /**
     * 任务 ID
     */
    @TableId
    private Long id;

    /**
     * 任务编号
     */
    private String taskNo;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 所属闸站
     */
    private String stationId;

    /**
     * 巡检类型
     */
    private String inspectionType;

    /**
     * 巡检对象类型（1 设备，2 位置）
     */
    private Integer objectType;

    /**
     * 巡检标准 ID
     */
    private Long standardId;

    /**
     * 巡检标准名称快照
     */
    private String standardName;

    /**
     * 巡检线路 ID
     */
    private Long lineId;

    /**
     * 巡检线路名称快照
     */
    private String lineName;

    /**
     * 巡检计划 ID（人工任务为空）
     */
    private Long planId;

    /**
     * 任务来源（1计划生成 2人工创建）
     */
    private Integer sourceType;

    /**
     * 计划开始时间
     */
    private LocalDateTime planStartTime;

    /**
     * 计划完成时间
     */
    private LocalDateTime planEndTime;

    /**
     * 执行人用户 ID
     */
    private Long executorUserId;

    /**
     * 执行人姓名
     */
    private String executorName;

    /**
     * 执行班组 ID
     */
    private Long executeDeptId;

    /**
     * 执行班组名称
     */
    private String executeDeptName;

    /**
     * 任务状态（0未开始 1未完成 2已完成 3已逾期）
     */
    private Integer taskStatus;

    /**
     * 异常点数（不合格项数量）
     */
    private Integer abnormalCount;

    /**
     * 检查项总数
     */
    private Integer itemTotalCount;

    /**
     * 已检查项数量
     */
    private Integer itemFinishedCount;

    /**
     * 结果提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义KEY
     */
    private String processDefinitionKey;

    /**
     * 流程状态（0未发起 1进行中 2已结束）
     */
    private Integer workflowStatus;

    /**
     * 流程发起时间
     */
    private LocalDateTime processStartTime;

    /**
     * 流程结束时间
     */
    private LocalDateTime processEndTime;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 备注
     */
    private String remark;

    /**
     * 提交结果明细(JSON)
     */
    private String resultItemsJson;

}
