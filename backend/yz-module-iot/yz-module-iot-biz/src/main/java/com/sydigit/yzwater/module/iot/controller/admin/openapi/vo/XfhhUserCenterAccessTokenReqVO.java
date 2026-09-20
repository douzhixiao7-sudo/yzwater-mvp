package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 幸福河湖平台-用户中心获取 accessToken 请求
 */
@Data
@Schema(description = "幸福河湖平台-用户中心获取 accessToken 请求")
public class XfhhUserCenterAccessTokenReqVO {

    @NotBlank(message = "code 不能为空")
    @Schema(description = "授权码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "redirectUri 不能为空")
    @Schema(description = "回调地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String redirectUri;
}
