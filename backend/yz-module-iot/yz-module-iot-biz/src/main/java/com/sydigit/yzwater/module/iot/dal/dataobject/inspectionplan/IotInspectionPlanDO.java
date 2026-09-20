package com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 巡检计划 DO
 */
@TableName("yz_equipment_inspection_plan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionPlanDO extends TenantBaseDO {

    /**
     * 计划 ID
     */
    @TableId
    private Long id;

    /**
     * 计划名称
     */
    private String planName;

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
     * 巡检线路 ID
     */
    private Long lineId;

    /**
     * 计划周期单位（DAY/WEEK/MONTH/QUARTER/YEAR）
     */
    private String cycleUnit;

    /**
     * 计划周期值
     */
    private Integer cycleValue;

    /**
     * 计划开始时间
     */
    private LocalDateTime planStartTime;

    /**
     * 计划结束时间
     */
    private LocalDateTime planEndTime;

    /**
     * 执行人用户 ID
     */
    private Long executorUserId;

    /**
     * 执行人名称
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
     * 启用状态（0 启用，1 停用）
     */
    private Integer enableStatus;

    /**
     * 计划状态（0 未开始，1 未完成，2 已完成，3 已逾期）
     */
    private Integer planStatus;

    /**
     * 下次任务生成时间
     */
    private LocalDateTime nextGenerateTime;

    /**
     * 最近任务生成时间
     */
    private LocalDateTime lastGenerateTime;

    /**
     * 累计生成任务数
     */
    private Integer generatedTaskCount;

    /**
     * 巡检对象数量
     */
    private Integer targetCount;

    /**
     * 备注
     */
    private String remark;

}
