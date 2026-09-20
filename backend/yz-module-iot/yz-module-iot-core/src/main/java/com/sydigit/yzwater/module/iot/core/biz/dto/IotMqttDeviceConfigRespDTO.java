package com.sydigit.yzwater.module.iot.core.biz.dto;

import lombok.Data;

import java.util.List;

/**
 * IoT MQTT Source 设备配置响应 DTO
 */
@Data
public class IotMqttDeviceConfigRespDTO {

    /**
     * 数据源编号
     */
    private Long sourceId;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * Broker 主机
     */
    private String brokerHost;

    /**
     * Broker 端口
     */
    private Integer brokerPort;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 客户端标识
     */
    private String clientId;

    /**
     * 默认 QoS
     */
    private Integer qos;

    /**
     * 是否清理会话
     */
    private Boolean cleanSession;

    /**
     * 心跳间隔，单位秒
     */
    private Integer keepAliveIntervalSeconds;

    /**
     * 连接超时，单位秒
     */
    private Integer connectTimeoutSeconds;

    /**
     * 重连延迟，单位毫秒
     */
    private Long reconnectDelayMs;

    /**
     * 是否启用 SSL
     */
    private Boolean sslEnabled;

    /**
     * 设备编号
     */
    private Long deviceId;

    /**
     * 产品标识
     */
    private String productKey;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 订阅主题
     */
    private String topic;

    /**
     * 负载模式
     */
    private String payloadMode;

    /**
     * 报文时间字段
     */
    private String reportTimeKey;

    /**
     * 报文时间格式
     */
    private String reportTimeFormat;

    /**
     * 属性映射列表
     */
    private List<IotMqttPointRespDTO> points;

}
