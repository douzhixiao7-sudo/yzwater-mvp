package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏设备下拉 Response VO")
@Data
public class IotScreenDeviceOptionRespVO {

    @Schema(description = "展示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "1号水位计")
    private String label;

    @Schema(description = "设备名称值", requiredMode = Schema.RequiredMode.REQUIRED, example = "device-water-level-01")
    private String value;

    @Schema(description = "设备类型值（字典 iot_deivce_type）", example = "6")
    private Integer deviceType;

    @Schema(description = "设备类型名称", example = "水位计")
    private String deviceTypeName;

}

