package com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IoT MQTT Source 固定主题采集配置
 */
@Data
public class IotMqttSourceConfig {

    /**
     * 是否启用
     */
    @NotNull(message = "MQTT Source 是否启用不能为空")
    private Boolean enabled = false;

    /**
     * 配置刷新间隔，单位秒
     */
    @NotNull(message = "MQTT Source 配置刷新间隔不能为空")
    private Integer configRefreshInterval = 30;

    /**
     * MQTT Source 客户端允许接收的最大消息大小，单位字节
     */
    @NotNull(message = "MQTT Source 最大消息大小不能为空")
    private Integer maxMessageSize = 65536;

}
