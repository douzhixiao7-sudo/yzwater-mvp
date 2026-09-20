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
 * IoT 设备 MQTT 采集配置 DO
 */
@TableName("iot_device_mqtt_config")
@KeySequence("iot_device_mqtt_config_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceMqttConfigDO extends TenantBaseDO {

    @TableId
    private Long id;

    private Long productId;

    private Long deviceId;

    private Long sourceId;

    private String topic;

    private String payloadMode;

    private String reportTimeKey;

    private String reportTimeFormat;

    private Integer status;

    private String remark;

}
