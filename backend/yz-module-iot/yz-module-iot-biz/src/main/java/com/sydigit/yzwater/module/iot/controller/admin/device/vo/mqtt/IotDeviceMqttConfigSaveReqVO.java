package com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备 MQTT 采集配置新增/修改 Request VO")
@Data
public class IotDeviceMqttConfigSaveReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "MQTT 数据源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "MQTT 数据源不能为空")
    private Long sourceId;

    @Schema(description = "订阅主题", requiredMode = Schema.RequiredMode.REQUIRED, example = "data/test/v1")
    @NotBlank(message = "订阅主题不能为空")
    private String topic;

    @Schema(description = "负载模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "flat_json")
    @NotBlank(message = "负载模式不能为空")
    private String payloadMode;

    @Schema(description = "报文时间字段", example = "report_time")
    private String reportTimeKey;

    @Schema(description = "报文时间格式", example = "yyyy-MM-dd HH:mm:ss")
    private String reportTimeFormat;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
