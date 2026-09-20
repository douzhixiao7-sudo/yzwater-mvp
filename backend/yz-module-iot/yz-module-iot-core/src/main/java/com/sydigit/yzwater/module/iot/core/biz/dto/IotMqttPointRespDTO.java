package com.sydigit.yzwater.module.iot.core.biz.dto;

import lombok.Data;

/**
 * IoT MQTT Source 属性映射响应 DTO
 */
@Data
public class IotMqttPointRespDTO {

    /**
     * 映射编号
     */
    private Long id;

    /**
     * 物模型属性编号
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
     * 负载字段
     */
    private String payloadKey;

    /**
     * 数据类型
     */
    private String valueType;

    /**
     * 排序号
     */
    private Integer sort;

}
