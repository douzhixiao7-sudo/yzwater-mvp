package com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 巡检标准-设备关联 DO
 */
@TableName("yz_equipment_inspection_standard_device")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionStandardDeviceDO extends TenantBaseDO {

    /**
     * 关联 ID
     */
    @TableId
    private Long id;

    /**
     * 巡检标准 ID
     */
    private Long standardId;

    /**
     * 设备 ID
     */
    private Long deviceId;

    /**
     * 排序
     */
    private Integer sort;

}
