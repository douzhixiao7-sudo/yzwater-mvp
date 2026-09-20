package com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 巡检标准适用对象 DO
 */
@TableName("yz_equipment_inspection_standard_target")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionStandardTargetDO extends TenantBaseDO {

    /**
     * 适用对象 ID
     */
    @TableId
    private Long id;

    /**
     * 标准 ID
     */
    private Long standardId;

    /**
     * 适用对象类型（device 或 zd_sslb.value）
     */
    private String targetType;

    /**
     * 适用对象业务 ID
     */
    private Long targetId;

    /**
     * 所属站点（仅 device 类型使用）
     */
    private String stationId;

    /**
     * 适用对象名称快照
     */
    private String targetName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 检查项数量（冗余）
     */
    private Integer itemCount;

    /**
     * 检查结果等级配置（JSON）
     */
    @TableField("check_result_configs")
    private String checkResultConfigsJson;

}
