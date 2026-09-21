package com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 巡检标准记录项模板 DO
 */
@TableName("yz_equipment_inspection_standard_item_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionStandardItemRecordDO extends TenantBaseDO {

    /**
     * 记录项 ID
     */
    @TableId
    private Long id;

    /**
     * 检查项 ID
     */
    private Long standardItemId;

    /**
     * 属性名称
     */
    private String attrName;

    /**
     * 属性单位
     */
    private String attrUnit;

    /**
     * 值类型（TEXT/NUMBER/SELECT）
     */
    private String valueType;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否必填（0 否，1 是）
     */
    private Integer requiredFlag;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 排序
     */
    private Integer sort;

}
