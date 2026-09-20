package com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备 GENESIS64 点位配置新增/修改 Request VO")
@Data
public class IotDeviceGenesisPointSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "物模型属性编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "物模型属性编号不能为空")
    private Long thingModelId;

    @Schema(description = "GENESIS64 点位名称", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "modbus:3号闸.开入量06")
    @NotBlank(message = "GENESIS64 点位名称不能为空")
    private String pointName;

    @Schema(description = "排序号", example = "10")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
