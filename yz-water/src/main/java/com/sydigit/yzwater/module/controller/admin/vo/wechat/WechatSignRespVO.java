package com.sydigit.yzwater.module.controller.admin.vo.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信 JS-SDK 签名响应
 */
@Data
@Schema(description = "微信 JS-SDK 签名响应")
public class WechatSignRespVO {

    @Schema(description = "微信公众号 AppId")
    private String appId;

    @Schema(description = "时间戳（秒）")
    private Long timestamp;

    @Schema(description = "随机字符串")
    private String nonceStr;

    @Schema(description = "签名")
    private String signature;
}
