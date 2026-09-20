package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.wechat.WechatSignRespVO;
import com.sydigit.yzwater.module.service.wechat.WechatSignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 微信 JS-SDK 签名
 */
@Tag(name = "管理后台 - 微信 JS-SDK 签名")
@RestController
@Validated
public class WechatSignController {

    private final WechatSignService wechatSignService;

    public WechatSignController(WechatSignService wechatSignService) {
        this.wechatSignService = wechatSignService;
    }

    @GetMapping({"/wechat/sign", "/api/sign"})
    @Operation(summary = "获取微信 JS-SDK 签名")
    @PermitAll
    public CommonResult<WechatSignRespVO> sign(@RequestParam("url") @NotBlank String url) {
        return success(wechatSignService.sign(url));
    }
}
