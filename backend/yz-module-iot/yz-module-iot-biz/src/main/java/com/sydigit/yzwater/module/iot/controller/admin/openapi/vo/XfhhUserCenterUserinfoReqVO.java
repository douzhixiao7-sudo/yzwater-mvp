package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 幸福河湖平台-用户中心获取用户信息请求
 */
@Data
@Schema(description = "幸福河湖平台-用户中心获取用户信息请求")
public class XfhhUserCenterUserinfoReqVO {

    @NotBlank(message = "accessToken 不能为空")
    @Schema(description = "访问令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accessToken;
}
