package com.sydigit.yzwater.module.iot.dal.dataobject.device;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备 GENESIS64 点位配置 DO
 */
@TableName("iot_device_genesis_point")
@KeySequence("iot_device_genesis_point_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceGenesisPointDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;

    /**
     * 物模型属性编号
     *
     * 关联 {@link IotThingModelDO#getId()}
     */
    private Long thingModelId;

    /**
     * 属性标识符
     */
    private String identifier;

    /**
     * 属性名称
     */
    private String name;

    /**
     * GENESIS64 点位名称
     */
    private String pointName;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 状态
     */
    private Integer status;

}
