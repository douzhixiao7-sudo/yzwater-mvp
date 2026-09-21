package com.sydigit.yzwater.module.iot.controller.admin.device.vo.http;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备联调 Request VO")
@Data
public class IotDeviceHttpProtocolTestReqVO {

    @Schema(description = "外部数据源地址", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "http://221.178.149.154:9017/ODataConnector/rest/RealtimeData")
    @NotEmpty(message = "外部数据源地址不能为空")
    private String dataSourceUrl;

    @Schema(description = "外部数据源用户名", example = "super")
    private String dataSourceUsername;

    @Schema(description = "外部数据源密码", example = "12345678")
    private String dataSourcePassword;

    @Schema(description = "点位名称", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "modbus:3号闸.开入量06")
    @NotEmpty(message = "点位名称不能为空")
    private String pointName;

    @Schema(description = "物模型属性标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "krl06")
    @NotEmpty(message = "物模型属性标识符不能为空")
    private String identifier;

    @Schema(description = "网关地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://127.0.0.1:8092")
    @NotEmpty(message = "网关地址不能为空")
    private String gatewayUrl;

    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "4aymZgOTOOCrDKRT")
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "small")
    @NotEmpty(message = "设备名称不能为空")
    private String deviceName;

    @Schema(description = "设备密钥", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设备密钥不能为空")
    private String deviceSecret;

}
