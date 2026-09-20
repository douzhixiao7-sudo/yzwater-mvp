package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 实时数据采集源 Response VO")
@Data
public class IotRealtimeDataSourceRespVO {

    @Schema(description = "主键", example = "1024")
    private Long id;

    @Schema(description = "采集源名称", example = "厂站 A")
    private String name;

    @Schema(description = "采集源编码", example = "STATION_A")
    private String code;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "拉取地址", example = "http://127.0.0.1")
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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}