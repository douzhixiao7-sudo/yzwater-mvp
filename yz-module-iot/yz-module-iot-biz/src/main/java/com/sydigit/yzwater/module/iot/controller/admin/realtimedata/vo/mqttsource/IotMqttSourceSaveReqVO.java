package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT MQTT 数据源新增/修改 Request VO")
@Data
public class IotMqttSourceSaveReqVO {

    @Schema(description = "主键", example = "1024")
    private Long id;

    @Schema(description = "数据源名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "闸站 MQTT 源")
    @NotEmpty(message = "数据源名称不能为空")
    private String name;

    @Schema(description = "数据源编码", example = "GATE_MQTT_SOURCE")
    private String code;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    @Schema(description = "Broker 主机", requiredMode = Schema.RequiredMode.REQUIRED, example = "192.168.6.102")
    @NotEmpty(message = "Broker 主机不能为空")
    private String brokerHost;

    @Schema(description = "Broker 端口", requiredMode = Schema.RequiredMode.REQUIRED, example = "11833")
    @NotNull(message = "Broker 端口不能为空")
    private Integer brokerPort;

    @Schema(description = "用户名", example = "mqtt")
    private String username;

    @Schema(description = "密码", example = "mqtt@123456")
    private String password;

    @Schema(description = "客户端标识", example = "yzwater-mqtt-source-01")
    private String clientId;

    @Schema(description = "默认 QoS", example = "0")
    private Integer qos;

    @Schema(description = "是否清理会话", example = "true")
    private Boolean cleanSession;

    @Schema(description = "心跳间隔，单位秒", example = "60")
    private Integer keepAliveIntervalSeconds;

    @Schema(description = "连接超时，单位秒", example = "10")
    private Integer connectTimeoutSeconds;

    @Schema(description = "重连延迟，单位毫秒", example = "5000")
    private Long reconnectDelayMs;

    @Schema(description = "是否启用 SSL", example = "false")
    private Boolean sslEnabled;

    @Schema(description = "备注")
    private String remark;

}
