package com.sydigit.yzwater.module.iot.core.biz.dto;

import lombok.Data;

import java.util.List;

/**
 * IoT GENESIS64 设备配置响应 DTO
 */
@Data
public class IotGenesisDeviceConfigRespDTO {

    private Long deviceId;

    private String productKey;

    private String deviceName;

    private String baseUrl;

    private String username;

    private String password;

    /**
     * 请求超时时间，单位毫秒
     */
    private Integer timeout;

    /**
     * 采集间隔，单位毫秒
     */
    private Integer collectInterval;

    private List<IotGenesisPointRespDTO> points;

}
