package com.sydigit.yzwater.module.iot.dal.dataobject.device;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备 MQTT 属性映射 DO
 */
@TableName("iot_device_mqtt_mapping")
@KeySequence("iot_device_mqtt_mapping_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceMqttMappingDO extends TenantBaseDO {

    @TableId
    private Long id;

    private Long deviceId;

    private Long thingModelId;

    private String identifier;

    private String name;

    private String payloadKey;

    private String valueType;

    private Integer sort;

    private Integer status;

    private String remark;

}
