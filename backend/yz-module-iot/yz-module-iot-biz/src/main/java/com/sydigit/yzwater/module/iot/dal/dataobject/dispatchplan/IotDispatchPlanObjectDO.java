package com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调度方案操作对象 DO
 */
@TableName("yz_dispatch_plan_object")
@KeySequence("yz_dispatch_plan_object_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDispatchPlanObjectDO extends TenantBaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 调度方案 ID
     */
    private Long planId;

    /**
     * 对象排序
     */
    private Integer objectSort;

    /**
     * 对象类型（1设备 2自定义）
     */
    private Integer objectType;

    /**
     * 操作对象名称快照
     */
    private String objectName;

    /**
     * 设备 ID（对象类型=设备时使用）
     */
    private Long deviceId;

    /**
     * 安装位置 ID（可选）
     */
    private Long locationId;

    /**
     * 备注
     */
    private String remark;
}

