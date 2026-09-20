package com.sydigit.yzwater.module.iot.dal.dataobject.maintenanceplan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 养护计划备件消耗 DO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotMaintenancePlanSpareUsageDO {

    /**
     * 备件 ID
     */
    private Long spareId;
    /**
     * 消耗数量
     */
    private Integer qty;
}
