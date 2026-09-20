package com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备 MQTT 采集配置 Response VO")
@Data
public class IotDeviceMqttConfigRespVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "MQTT 数据源编号", example = "2048")
    private Long sourceId;

    @Schema(description = "订阅主题", example = "data/test/v1")
    private String topic;

    @Schema(description = "负载模式", example = "flat_json")
    private String payloadMode;

    @Schema(description = "报文时间字段", example = "report_time")
    private String reportTimeKey;

    @Schema(description = "报文时间格式", example = "yyyy-MM-dd HH:mm:ss")
    private String reportTimeFormat;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
