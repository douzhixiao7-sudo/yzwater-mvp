package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.controller.admin.vo.shortlink.ShortLinkResolveRespVO;
import com.sydigit.yzwater.module.service.shortlink.ShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "H5 - 短链")
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @GetMapping("/s/{code}")
    @Operation(summary = "短链重定向（302 跳转到 H5 页面）")
    @PermitAll
    @TenantIgnore
    public void redirect(@PathVariable("code") String code, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        ShortLinkResolveRespVO resolve = shortLinkService.resolve(code, clientIp, userAgent, true);
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", resolve.getRedirectUrl());
    }

    @GetMapping("/h5/short-link/resolve")
    @Operation(summary = "短链解析（返回场景/业务ID/最终跳转URL）")
    @PermitAll
    @TenantIgnore
    public CommonResult<ShortLinkResolveRespVO> resolve(@RequestParam("code") String code, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return success(shortLinkService.resolve(code, clientIp, userAgent, false));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int idx = xff.indexOf(',');
            return idx >= 0 ? xff.substring(0, idx).trim() : xff.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

}
