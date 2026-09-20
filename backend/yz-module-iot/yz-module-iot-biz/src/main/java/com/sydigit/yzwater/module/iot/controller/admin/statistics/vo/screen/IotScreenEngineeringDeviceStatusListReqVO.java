package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏工程监测设备状态列表 Request VO")
@Data
public class IotScreenEngineeringDeviceStatusListReqVO {

    @Schema(description = "站点值（字典 iot_zd_sbzd）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotBlank(message = "所属站点不能为空")
    private String stationId;

    @Schema(description = "设备类型（仅支持 7 或 3）", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;

    @Schema(description = "闸门全开筛选（仅设备类型7生效）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Integer isGateOpenAll;

    @Schema(description = "闸门全关筛选（仅设备类型7生效）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Integer isGateCloseAll;

    @Schema(description = "闸门电源状态筛选（仅设备类型7生效，0代表停止）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0")
    private Integer isGatePowerOn;

    @Schema(description = "运行状态筛选（仅设备类型3生效，1运行/0停止）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private Integer isRunning;
}

