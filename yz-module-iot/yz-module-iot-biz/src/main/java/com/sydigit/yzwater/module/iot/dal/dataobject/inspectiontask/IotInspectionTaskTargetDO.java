package com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 巡检任务对象明细 DO
 */
@TableName("yz_equipment_inspection_task_target")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionTaskTargetDO extends TenantBaseDO {

    /**
     * 对象明细 ID
     */
    @TableId
    private Long id;

    /**
     * 任务 ID
     */
    private Long taskId;

    /**
     * 对象排序
     */
    private Integer targetSort;

    /**
     * 对象类型（1 设备，2 位置）
     */
    private Integer targetType;

    /**
     * 设备 ID
     */
    private Long deviceId;

    /**
     * 位置 ID
     */
    private Long locationId;

    /**
     * 对象名称快照
     */
    private String targetName;

    /**
     * 所属闸站
     */
    private String stationId;

}
