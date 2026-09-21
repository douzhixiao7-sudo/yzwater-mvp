package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 河道二维码响应
 */
@Schema(description = "仪征管理后台 - 河道二维码响应")
@Data
public class RiverChannelQrRespVO {

    @Schema(description = "二维码图片，data:image/png;base64 开头的 Base64 字符串")
    private String qrcodeImageBase64;

    @Schema(description = "二维码承载的 URL/数据内容")
    private String contentUrl;
}
