package com.sydigit.yzwater.module.iot.service.device.dto;

import lombok.Data;

import java.util.List;

/**
 * IoT 设备位置树节点
 */
@Data
public class IotDeviceLocationNode {

    private Long id;

    private String name;

    private Integer sort;

    /**
     * 子节点
     */
    private List<IotDeviceLocationNode> children;

}
