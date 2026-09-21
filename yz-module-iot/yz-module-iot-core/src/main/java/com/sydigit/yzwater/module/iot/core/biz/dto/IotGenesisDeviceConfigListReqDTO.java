package com.sydigit.yzwater.module.iot.core.biz.dto;

import lombok.Data;

import java.util.Set;

/**
 * IoT GENESIS64 设备配置列表查询 DTO
 */
@Data
public class IotGenesisDeviceConfigListReqDTO {

    private Integer status;

    private String protocolType;

    private Set<Long> deviceIds;

}
