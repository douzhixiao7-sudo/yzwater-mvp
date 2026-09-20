package com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 设备 GENESIS64 连接配置新增/修改 Request VO")
@Data
public class IotDeviceGenesisConfigSaveReqVO {

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "GENESIS64 实时数据接口地址", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "http://127.0.0.1:9010/ODataConnector/rest/RealtimeData")
    @NotBlank(message = "GENESIS64 接口地址不能为空")
    private String baseUrl;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @NotBlank(message = "GENESIS64 用户名不能为空")
    private String username;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345678")
    @NotBlank(message = "GENESIS64 密码不能为空")
    private String password;

    @Schema(description = "请求超时时间（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    @NotNull(message = "请求超时时间不能为空")
    private Integer timeout;

    @Schema(description = "采集频率（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "15000")
    @NotNull(message = "采集频率不能为空")
    private Integer collectInterval;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "15 秒采集一次")
    private String remark;

}
