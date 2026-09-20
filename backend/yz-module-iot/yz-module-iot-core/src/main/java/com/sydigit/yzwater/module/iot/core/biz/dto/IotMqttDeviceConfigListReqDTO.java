package com.sydigit.yzwater.module.iot.core.biz.dto;

import lombok.Data;

import java.util.Set;

/**
 * IoT MQTT Source 设备配置列表查询 DTO
 */
@Data
public class IotMqttDeviceConfigListReqDTO {

    /**
     * 配置状态
     */
    private Integer status;

    /**
     * 产品协议类型
     */
    private String protocolType;

    /**
     * 数据源是否启用
     */
    private Boolean sourceEnabled;

    /**
     * 数据源编号集合
     */
    private Set<Long> sourceIds;

    /**
     * 设备编号集合
     */
    private Set<Long> deviceIds;

}
