package com.sydigit.yzwater.module.iot.controller.admin.device.vo.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备联调 Response VO")
@Data
public class IotDeviceHttpProtocolTestRespVO {

    @Schema(description = "点位名称", example = "modbus:3号闸.开入量06")
    private String pointName;

    @Schema(description = "物模型属性标识符", example = "krl06")
    private String identifier;

    @Schema(description = "点位值")
    private Object pointValue;

    @Schema(description = "网关 token")
    private String token;

    @Schema(description = "外部数据源响应原文")
    private String dataSourceResponse;

    @Schema(description = "属性上报响应原文")
    private String propertyPostResponse;

}
