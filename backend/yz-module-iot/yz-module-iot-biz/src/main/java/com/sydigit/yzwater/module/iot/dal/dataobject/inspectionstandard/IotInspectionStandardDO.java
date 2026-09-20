package com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 巡检标准 DO
 */
@TableName("yz_equipment_inspection_standard")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionStandardDO extends TenantBaseDO {

    /**
     * 标准 ID
     */
    @TableId
    private Long id;

    /**
     * 标准名称
     */
    private String standardName;

    /**
     * 巡检类型
     */
    private String inspectionType;

    /**
     * 建议周期（字典 iot_inspection_period.value）
     */
    private String suggestCycleUnit;

    /**
     * 建议周期值（策略A固定为1）
     */
    private Integer suggestCycleValue;

    /**
     * 状态（0 启用，1 停用）
     */
    private Integer status;

    /**
     * 适用对象数量（冗余）
     */
    private Integer targetCount;

    /**
     * 检查项数量（冗余）
     */
    private Integer itemCount;

    /**
     * 备注
     */
    private String remark;

}
