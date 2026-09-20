package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT MQTT 数据源精简 Response VO")
@Data
public class IotMqttSourceSimpleRespVO {

    @Schema(description = "主键", example = "1024")
    private Long id;

    @Schema(description = "数据源名称", example = "闸站 MQTT 源")
    private String name;

    @Schema(description = "数据源编码", example = "GATE_MQTT_SOURCE")
    private String code;

    @Schema(description = "Broker 主机", example = "127.0.0.1")
    private String brokerHost;

    @Schema(description = "Broker 端口", example = "11833")
    private Integer brokerPort;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

}
