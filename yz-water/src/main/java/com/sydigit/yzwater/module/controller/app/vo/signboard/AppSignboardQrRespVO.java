package com.sydigit.yzwater.module.controller.app.vo.signboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 手机端 - 公示牌二维码生成响应
 */
@Schema(description = "手机端 - 公示牌二维码生成响应")
@Data
public class AppSignboardQrRespVO {

    @Schema(description = "二维码图片，data:image/png;base64 开头的 Base64 字符串")
    private String qrcodeImageBase64;

    @Schema(description = "二维码承载的 URL/数据内容")
    private String contentUrl;
}

