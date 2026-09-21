package com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备 MQTT 属性映射新增/修改 Request VO")
@Data
public class IotDeviceMqttMappingSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "物模型属性编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "物模型属性编号不能为空")
    private Long thingModelId;

    @Schema(description = "报文字段名", requiredMode = Schema.RequiredMode.REQUIRED, example = "ZMQBJ3_rise")
    @NotBlank(message = "报文字段名不能为空")
    private String payloadKey;

    @Schema(description = "排序号", example = "10")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
