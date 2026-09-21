package com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备 MQTT 属性映射 Response VO")
@Data
public class IotDeviceMqttMappingRespVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "物模型属性编号", example = "2048")
    private Long thingModelId;

    @Schema(description = "属性标识符", example = "rise")
    private String identifier;

    @Schema(description = "属性名称", example = "上升信号")
    private String name;

    @Schema(description = "报文字段名", example = "ZMQBJ3_rise")
    private String payloadKey;

    @Schema(description = "数据类型", example = "bool")
    private String valueType;

    @Schema(description = "排序号", example = "10")
    private Integer sort;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
