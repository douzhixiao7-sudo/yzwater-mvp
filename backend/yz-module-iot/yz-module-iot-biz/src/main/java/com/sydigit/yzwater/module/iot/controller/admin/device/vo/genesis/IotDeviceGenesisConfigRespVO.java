package com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 设备 GENESIS64 连接配置 Response VO")
@Data
public class IotDeviceGenesisConfigRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long deviceId;

    @Schema(description = "GENESIS64 实时数据接口地址")
    private String baseUrl;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "请求超时时间（毫秒）")
    private Integer timeout;

    @Schema(description = "采集频率（毫秒）")
    private Integer collectInterval;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
