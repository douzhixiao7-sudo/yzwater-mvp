package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - IoT 实时数据采集源新增/修改 Request VO")
@Data
public class IotRealtimeDataSourceSaveReqVO {

    @Schema(description = "主键", example = "1024")
    private Long id;

    @Schema(description = "采集源名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "厂站 A")
    @NotEmpty(message = "采集源名称不能为空")
    private String name;

    @Schema(description = "采集源编码", example = "STATION_A")
    private String code;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用不能为空")
    private Boolean enabled;

    @Schema(description = "拉取地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://127.0.0.1")
    @NotEmpty(message = "拉取地址不能为空")
    private String url;

    @Schema(description = "Basic Auth 用户名", example = "super")
    private String username;

    @Schema(description = "Basic Auth 密码", example = "123456")
    private String password;

    @Schema(description = "请求体 JSON")
    private String requestBody;

    @Schema(description = "调度 Cron", example = "0 */5 * * * ?")
    private String cron;

    @Schema(description = "备注")
    private String remark;

}
