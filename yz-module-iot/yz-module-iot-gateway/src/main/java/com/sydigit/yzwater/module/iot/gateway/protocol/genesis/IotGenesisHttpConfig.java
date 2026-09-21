package com.sydigit.yzwater.module.iot.gateway.protocol.genesis;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * IoT GENESIS64 HTTP 主动采集协议配置
 */
@Data
public class IotGenesisHttpConfig {

    /**
     * 是否启用
     */
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled = false;

    /**
     * 配置刷新间隔，单位秒
     */
    @NotNull(message = "配置刷新间隔不能为空")
    @Min(value = 1, message = "配置刷新间隔不能小于 1 秒")
    private Integer configRefreshInterval = 30;

}
