package com.sydigit.yzwater.module.iot.dal.dataobject.faultrepair;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 故障维修备件消耗项 DO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotFaultRepairSpareUsageDO {

    /**
     * 备件 ID
     */
    private Long spareId;
    /**
     * 消耗数量
     */
    private Integer qty;
}
