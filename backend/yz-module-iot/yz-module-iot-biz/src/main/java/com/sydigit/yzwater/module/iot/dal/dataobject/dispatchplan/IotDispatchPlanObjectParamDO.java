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
 * 调度方案操作对象参数 DO
 */
@TableName("yz_dispatch_plan_object_param")
@KeySequence("yz_dispatch_plan_object_param_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDispatchPlanObjectParamDO extends TenantBaseDO {

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
     * 操作对象 ID
     */
    private Long objectId;

    /**
     * 参数排序
     */
    private Integer paramSort;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数值
     */
    private String paramValue;

    /**
     * 参数单位
     */
    private String paramUnit;

    /**
     * 备注
     */
    private String remark;
}

