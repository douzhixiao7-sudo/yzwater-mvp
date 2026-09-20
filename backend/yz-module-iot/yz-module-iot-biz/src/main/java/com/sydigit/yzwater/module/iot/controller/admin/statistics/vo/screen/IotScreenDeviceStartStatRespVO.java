package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 大屏设备启停统计 Response VO")
@Data
public class IotScreenDeviceStartStatRespVO {

    @Schema(description = "设备 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long deviceId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "device-pump-01")
    private String deviceName;

    @Schema(description = "开机次数（物模型属性 startCount）", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Object startCount;

    @Schema(description = "累计运行时间（物模型属性 totalStartDuration）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "3600")
    private Object totalStartDuration;

}

