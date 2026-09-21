package com.sydigit.yzwater.module.iot.controller.admin.device.vo.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备二维码 Response VO")
@Data
public class IotDeviceQrCodeRespVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "177")
    private Long id;

    @Schema(description = "设备二维码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String qrCode;
}
