package com.sydigit.yzwater.module.system.controller.admin.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.enums.UserTypeEnum;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.util.http.HttpUtils;
import com.sydigit.yzwater.framework.datapermission.core.annotation.DataPermission;
import com.sydigit.yzwater.framework.security.config.SecurityProperties;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.framework.web.config.WebProperties;
import com.sydigit.yzwater.module.system.framework.sso.config.UserCenterSsoProperties;
import com.sydigit.yzwater.module.system.controller.admin.auth.vo.*;
import com.sydigit.yzwater.module.system.convert.auth.AuthConvert;
import com.sydigit.yzwater.module.system.dal.dataobject.permission.MenuDO;
import com.sydigit.yzwater.module.system.dal.dataobject.permission.RoleDO;
import com.sydigit.yzwater.module.system.dal.dataobject.user.AdminUserDO;
import com.sydigit.yzwater.module.system.enums.logger.LoginLogTypeEnum;
import com.sydigit.yzwater.module.system.service.auth.AdminAuthService;
import com.sydigit.yzwater.module.system.service.permission.MenuService;
import com.sydigit.yzwater.module.system.service.permission.PermissionService;
import com.sydigit.yzwater.module.system.service.permission.RoleService;
import com.sydigit.yzwater.module.system.service.sso.UserCenterSsoLoginService;
import com.sydigit.yzwater.module.system.service.social.SocialClientService;
import com.sydigit.yzwater.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertSet;
import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.sydigit.yzwater.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

@Tag(name = "管理后台 - 认证")
@RestController
@RequestMapping("/system/auth")
@Validated
@Slf4j
public class AuthController {

    private static final String EXTERNAL_AUTO_LOGIN_PATH = "/external-auto-login";
    /** 外网 warroom 承接基地址（env=dev 或未传） */
    private static final String EXTERNAL_AUTO_LOGIN_HOST_DEV = "http://127.0.0.1:9010";
    /** 内网 warroom 承接基地址（env=prod） */
    private static final String EXTERNAL_AUTO_LOGIN_HOST_PROD = "http://127.0.0.1:48082";
    private static final String USER_CENTER_SSO_LOGIN_PATH = "/sso/user-center/login";
    private static final String DEFAULT_SSO_REDIRECT = "/index";

    @Resource
    private AdminAuthService authService;
    @Resource
    private AdminUserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private SocialClientService socialClientService;

    @Resource
    private SecurityProperties securityProperties;
    @Resource
    private UserCenterSsoProperties userCenterSsoProperties;
    @Resource
    private UserCenterSsoLoginService userCenterSsoLoginService;
    @Resource
    private WebProperties webProperties;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "使用账号密码登录")
    public CommonResult<AuthLoginRespVO> login(@RequestBody @Valid AuthLoginReqVO reqVO) {
        return success(authService.login(reqVO));
    }

    @GetMapping(USER_CENTER_SSO_LOGIN_PATH)
    @PermitAll
    @Operation(summary = "用户中心单点登录跳转")
    @Parameter(name = "redirect", description = "登录完成后前端跳转路径，默认 /index", required = false)
    public void ssoUserCenterLogin(@RequestParam(value = "redirect", required = false) String redirect,
                                   HttpServletResponse response) {
        if (!Boolean.TRUE.equals(userCenterSsoProperties.getEnabled())) {
            throw invalidParamException("用户中心单点登录未启用");
        }
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", buildUserCenterAuthorizeUrl(redirect));
    }

    @GetMapping("/sso/user-center/callback")
    @PermitAll
    @Operation(summary = "用户中心单点登录回调")
    @Parameters({
            @Parameter(name = "code", description = "用户中心授权码", required = true),
            @Parameter(name = "state", description = "登录完成后前端跳转路径", required = false)
    })
    public void ssoUserCenterCallback(@RequestParam("code") String code,
                                      @RequestParam(value = "state", required = false) String state,
                                      HttpServletResponse response) {
        AuthLoginRespVO loginResp = userCenterSsoLoginService.loginByCode(code);
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", buildUserCenterCallbackRedirectUrl(loginResp, state));
    }

    @PostMapping("/external-auto-login")
    @PermitAll
    @Operation(summary = "对外免登录入口（账号密码换 Token，跳转外链大屏）",
            description = "JSON 传入账号、密码；可选 env：prod=内网承接页，dev 或不传=外网承接页；先校验用户存在，再校验密码并签发 Token，拼接外链目标地址。菜单与权限与登录用户一致。")
    public CommonResult<String> externalAutoLogin(@RequestBody @Valid AuthExternalAutoLoginReqVO reqVO,
                                                  HttpServletResponse response) {
        String mode = StrUtil.blankToDefault(reqVO.getMode(), "redirect");
        log.info("[externalAutoLogin][request] username={}, mode={}, env={}, redirect={}",
                reqVO.getUsername(), mode, reqVO.getEnv(), reqVO.getRedirect());

        if (userService.getUserByUsername(reqVO.getUsername()) == null) {
            log.warn("[externalAutoLogin][reject] user not exists, username={}", reqVO.getUsername());
            throw exception(USER_NOT_EXISTS);
        }

        AuthLoginRespVO loginResp = authService.loginWithoutCaptcha(reqVO.getUsername(), reqVO.getPassword());
        String targetUrl = buildExternalAutoLoginRedirectUrl(loginResp, reqVO.getRedirect(), reqVO.getEnv());
        // 不在日志中打印完整 targetUrl（query 含令牌）
        log.info("[externalAutoLogin][success] username={}, userId={}, expiresTime={}, env={}, responseMode={}, accessTokenLen={}, refreshTokenLen={}",
                reqVO.getUsername(), loginResp.getUserId(), loginResp.getExpiresTime(),
                StrUtil.blankToDefault(reqVO.getEnv(), "dev"), mode,
                loginResp.getAccessToken() != null ? loginResp.getAccessToken().length() : 0,
                loginResp.getRefreshToken() != null ? loginResp.getRefreshToken().length() : 0);

        if (StrUtil.equalsIgnoreCase(mode, "json")) {
            return success(targetUrl);
        }
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", targetUrl);
        return null;
    }

    @PostMapping("/logout")
    @PermitAll
    @Operation(summary = "登出系统")
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        }
        return success(true);
    }

    @PostMapping("/refresh-token")
    @PermitAll
    @Operation(summary = "刷新令牌")
    @Parameter(name = "refreshToken", description = "刷新令牌", required = true)
    public CommonResult<AuthLoginRespVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(authService.refreshToken(refreshToken));
    }

    @GetMapping("/get-permission-info")
    @Operation(summary = "获取登录用户的权限信息")
    @DataPermission(enable = false) // 忽略数据权限，避免因为过滤，导致无法查询用户。类似：https://t.zsxq.com/LHnrp
    public CommonResult<AuthPermissionInfoRespVO> getPermissionInfo() {
        // 1.1 获得用户信息
        AdminUserDO user = userService.getUser(getLoginUserId());
        if (user == null) {
            return success(null);
        }

        // 1.2 获得角色列表
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(getLoginUserId());
        if (CollUtil.isEmpty(roleIds)) {
            return success(AuthConvert.INSTANCE.convert(user, Collections.emptyList(), Collections.emptyList()));
        }
        List<RoleDO> roles = roleService.getRoleList(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus())); // 移除禁用的角色

        // 1.3 获得菜单列表
        Set<Long> menuIds = permissionService.getRoleMenuListByRoleId(convertSet(roles, RoleDO::getId));
        List<MenuDO> menuList = menuService.getMenuList(menuIds);
        menuList = menuService.filterDisableMenus(menuList);

        // 2. 拼接结果返回
        return success(AuthConvert.INSTANCE.convert(user, roles, menuList));
    }

    @PostMapping("/register")
    @PermitAll
    @Operation(summary = "注册用户")
    public CommonResult<AuthLoginRespVO> register(@RequestBody @Valid AuthRegisterReqVO registerReqVO) {
        return success(authService.register(registerReqVO));
    }

    // ========== 短信登录相关 ==========

    @PostMapping("/sms-login")
    @PermitAll
    @Operation(summary = "使用短信验证码登录")
    // 可按需开启限流：https://github.com/YunaiV/ruoyi-vue-pro/issues/851
    // @RateLimiter(time = 60, count = 6, keyResolver = ExpressionRateLimiterKeyResolver.class, keyArg = "#reqVO.mobile")
    public CommonResult<AuthLoginRespVO> smsLogin(@RequestBody @Valid AuthSmsLoginReqVO reqVO) {
        return success(authService.smsLogin(reqVO));
    }

    @PostMapping("/send-sms-code")
    @PermitAll
    @Operation(summary = "发送手机验证码")
    public CommonResult<Boolean> sendLoginSmsCode(@RequestBody @Valid AuthSmsSendReqVO reqVO) {
        authService.sendSmsCode(reqVO);
        return success(true);
    }

    @PostMapping("/send-sms-login-code")
    @PermitAll
    @Operation(summary = "获取短信登录验证码（仅手机号）")
    public CommonResult<Boolean> sendSmsLoginCode(@RequestBody @Valid AuthSmsLoginCodeSendReqVO reqVO) {
        authService.sendSmsLoginCode(reqVO);
        return success(true);
    }

    @PostMapping("/reset-password")
    @PermitAll
    @Operation(summary = "重置密码")
    public CommonResult<Boolean> resetPassword(@RequestBody @Valid AuthResetPasswordReqVO reqVO) {
        authService.resetPassword(reqVO);
        return success(true);
    }

    // ========== 社交登录相关 ==========

    @GetMapping("/social-auth-redirect")
    @PermitAll
    @Operation(summary = "社交授权的跳转")
    @Parameters({
            @Parameter(name = "type", description = "社交类型", required = true),
            @Parameter(name = "redirectUri", description = "回调路径")
    })
    public CommonResult<String> socialLogin(@RequestParam("type") Integer type,
                                            @RequestParam("redirectUri") String redirectUri) {
        return success(socialClientService.getAuthorizeUrl(
                type, UserTypeEnum.ADMIN.getValue(), redirectUri));
    }

    @PostMapping("/social-login")
    @PermitAll
    @Operation(summary = "社交快捷登录，使用 code 授权码", description = "适合未登录的用户，但是社交账号已绑定用户")
    public CommonResult<AuthLoginRespVO> socialQuickLogin(@RequestBody @Valid AuthSocialLoginReqVO reqVO) {
        return success(authService.socialLogin(reqVO));
    }

    /**
     * prod=内网承接基地址，dev 或未传=外网承接基地址（与历史默认一致）
     */
    private String resolveExternalAutoLoginHost(String env) {
        if (StrUtil.isNotBlank(env) && "prod".equalsIgnoreCase(env.trim())) {
            return EXTERNAL_AUTO_LOGIN_HOST_PROD;
        }
        return EXTERNAL_AUTO_LOGIN_HOST_DEV;
    }

    private String buildExternalAutoLoginRedirectUrl(AuthLoginRespVO loginResp, String redirect, String env) {
        String targetBaseUrl = resolveExternalAutoLoginHost(env) + EXTERNAL_AUTO_LOGIN_PATH;
        Map<String, Object> query = new HashMap<>();
        query.put("accessToken", loginResp.getAccessToken());
        query.put("refreshToken", loginResp.getRefreshToken());
        if (StrUtil.startWith(redirect, "/")) {
            query.put("redirect", redirect);
        }
        return HttpUtils.append(targetBaseUrl, query, null, false);
    }

    private String buildUserCenterAuthorizeUrl(String redirect) {
        String authorizeBaseUrl = StrUtil.removeSuffix(userCenterSsoProperties.getDomain(), "/")
                + "/api/v1/authorize/" + userCenterSsoProperties.getClientCode() + "/oauth2/auth";
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("response_type", "code");
        query.put("client_id", userCenterSsoProperties.getClientId());
        query.put("scope", StrUtil.blankToDefault(userCenterSsoProperties.getScope(), "openid phone profile"));
        query.put("state", normalizeSsoRedirect(redirect));
        query.put("redirect_uri", userCenterSsoProperties.getRedirectUri());
        return HttpUtils.append(authorizeBaseUrl, query, null, false);
    }

    private String normalizeSsoRedirect(String redirect) {
        return StrUtil.startWith(redirect, "/") ? redirect : DEFAULT_SSO_REDIRECT;
    }

    private String buildUserCenterCallbackRedirectUrl(AuthLoginRespVO loginResp, String redirect) {
        String targetBaseUrl = StrUtil.removeSuffix(webProperties.getAdminUi().getUrl(), "/") + EXTERNAL_AUTO_LOGIN_PATH;
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("accessToken", loginResp.getAccessToken());
        query.put("refreshToken", loginResp.getRefreshToken());
        query.put("redirect", normalizeSsoRedirect(redirect));
        return HttpUtils.append(targetBaseUrl, query, null, false);
    }

}
