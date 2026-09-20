package com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sydigit.yzwater.framework.tenant.core.db.TenantBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT MQTT 数据源 DO
 */
@TableName("iot_mqtt_source")
@KeySequence("iot_mqtt_source_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotMqttSourceDO extends TenantBaseDO {

    @TableId
    private Long id;

    private String name;

    private String code;

    private Boolean enabled;

    private String brokerHost;

    private Integer brokerPort;

    private String username;

    private String password;

    private String clientId;

    private Integer qos;

    private Boolean cleanSession;

    private Integer keepAliveIntervalSeconds;

    private Integer connectTimeoutSeconds;

    private Long reconnectDelayMs;

    private Boolean sslEnabled;

    private String remark;

}
