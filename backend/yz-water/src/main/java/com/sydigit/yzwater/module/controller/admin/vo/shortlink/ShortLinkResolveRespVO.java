package com.sydigit.yzwater.module.controller.admin.vo.shortlink;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "H5 - 短链解析 Response VO")
@Data
public class ShortLinkResolveRespVO {

    @Schema(description = "短链码")
    private String code;

    @Schema(description = "是否有效（存在、启用、未过期）")
    private Boolean valid;

    @Schema(description = "无效原因（invalid/disabled/expired/not_found）")
    private String invalidReason;

    @Schema(description = "跳转场景")
    private String scene;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务主键ID")
    private Long bizId;

    @Schema(description = "过期时间")
    private LocalDateTime expiresTime;

    @Schema(description = "后端计算出的最终跳转 URL")
    private String redirectUrl;

}

