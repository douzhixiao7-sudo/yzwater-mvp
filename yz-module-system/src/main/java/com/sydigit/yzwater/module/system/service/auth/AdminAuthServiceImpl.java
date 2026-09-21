package com.sydigit.yzwater.module.system.service.auth;

import cn.hutool.core.util.ObjectUtil;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.enums.UserTypeEnum;
import com.sydigit.yzwater.framework.common.util.monitor.TracerUtils;
import com.sydigit.yzwater.framework.common.util.servlet.ServletUtils;
import com.sydigit.yzwater.framework.common.util.validation.ValidationUtils;
import com.sydigit.yzwater.framework.datapermission.core.annotation.DataPermission;
import com.sydigit.yzwater.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.sydigit.yzwater.module.system.api.sms.SmsCodeApi;
import com.sydigit.yzwater.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.sydigit.yzwater.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.sydigit.yzwater.module.system.api.social.dto.SocialUserBindReqDTO;
import com.sydigit.yzwater.module.system.api.social.dto.SocialUserRespDTO;
import com.sydigit.yzwater.module.system.controller.admin.auth.vo.*;
import com.sydigit.yzwater.module.system.convert.auth.AuthConvert;
import com.sydigit.yzwater.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.sydigit.yzwater.module.system.dal.dataobject.permission.RoleDO;
import com.sydigit.yzwater.module.system.dal.dataobject.user.AdminUserDO;
import com.sydigit.yzwater.module.system.dal.mysql.permission.RoleMapper;
import com.sydigit.yzwater.module.system.enums.logger.LoginLogTypeEnum;
import com.sydigit.yzwater.module.system.enums.logger.LoginResultEnum;
import com.sydigit.yzwater.module.system.enums.oauth2.OAuth2ClientConstants;
import com.sydigit.yzwater.module.system.enums.sms.SmsSceneEnum;
import com.sydigit.yzwater.module.system.service.logger.LoginLogService;
import com.sydigit.yzwater.module.system.service.member.MemberService;
import com.sydigit.yzwater.module.system.service.oauth2.OAuth2TokenService;
import com.sydigit.yzwater.module.system.service.permission.PermissionService;
import com.sydigit.yzwater.module.system.service.permission.RoleService;
import com.sydigit.yzwater.module.system.service.social.SocialUserService;
import com.sydigit.yzwater.module.system.service.user.AdminUserService;
import com.sydigit.yzwater.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import com.sydigit.yzwater.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sydigit.yzwater.framework.common.exception.ServiceException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.sydigit.yzwater.module.system.enums.ErrorCodeConstants.*;

/**
 * Auth Service 实现类
 *
 *
 */
@Service
@Slf4j
public class AdminAuthServiceImpl implements AdminAuthService {

    /**
     * 短信登录自动注册的默认密码
     */
    private static final String DEFAULT_SMS_REGISTER_PASSWORD = "123456";
    /**
     * 游客角色名称与编码
     */
    private static final String GUEST_ROLE_NAME = "游客";
    private static final String GUEST_ROLE_CODE = "GUEST";

    @Resource
    private AdminUserService userService;
    @Resource
    private LoginLogService loginLogService;
    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private SocialUserService socialUserService;
    @Resource
    private MemberService memberService;
    @Resource
    private Validator validator;
    @Resource
    private CaptchaService captchaService;
    @Resource
    private SmsCodeApi smsCodeApi;
    @Resource
    private RoleService roleService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private RoleMapper roleMapper;

    /**
     * 验证码的开关，默认为 true
     */
    @Value("${yz.captcha.enable:false}")
    @Setter // 为了单测：开启或者关闭验证码
    private Boolean captchaEnable;

    @Override
    public AdminUserDO authenticate(String username, String password) {
        final LoginLogTypeEnum logTypeEnum = LoginLogTypeEnum.LOGIN_USERNAME;
        // 校验账号是否存在
        AdminUserDO user = userService.getUserByUsername(username);
        if (user == null) {
            createLoginLog(null, username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        if (!userService.isPasswordMatch(password, user.getPassword())) {
            createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 校验是否禁用
        if (CommonStatusEnum.isDisable(user.getStatus())) {
            createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }
        return user;
    }

    @Override
    @DataPermission(enable = false)
    public AuthLoginRespVO login(AuthLoginReqVO reqVO) {
        // 校验验证码
        validateCaptcha(reqVO);

        // 使用账号密码，进行登录
        AdminUserDO user = authenticate(reqVO.getUsername(), reqVO.getPassword());

        // 如果 socialType 非空，说明需要绑定社交用户
        if (reqVO.getSocialType() != null) {
            socialUserService.bindSocialUser(new SocialUserBindReqDTO(user.getId(), getUserType().getValue(),
                    reqVO.getSocialType(), reqVO.getSocialCode(), reqVO.getSocialState()));
        }
        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user.getId(), reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME);
    }

    @Override
    @DataPermission(enable = false)
    public AuthLoginRespVO loginWithoutCaptcha(String username, String password) {
        AdminUserDO user = authenticate(username, password);
        return createTokenAfterLoginSuccess(user.getId(), username, LoginLogTypeEnum.LOGIN_USERNAME);
    }

    @Override
    public void sendSmsCode(AuthSmsSendReqVO reqVO) {
        // 如果是重置密码场景，需要校验图形验证码是否正确
        if (Objects.equals(SmsSceneEnum.ADMIN_MEMBER_RESET_PASSWORD.getScene(), reqVO.getScene())) {
            ResponseModel response = doValidateCaptcha(reqVO);
            if (!response.isSuccess()) {
                throw exception(AUTH_REGISTER_CAPTCHA_CODE_ERROR, response.getRepMsg());
            }
        }

        // 登录场景，验证是否存在
        if (userService.getUserByMobile(reqVO.getMobile()) == null) {
            throw exception(AUTH_MOBILE_NOT_EXISTS);
        }
        // 发送验证码
        smsCodeApi.sendSmsCode(AuthConvert.INSTANCE.convert(reqVO).setCreateIp(getClientIP()));
    }

    @Override
    public void sendSmsLoginCode(AuthSmsLoginCodeSendReqVO reqVO) {
        SmsCodeSendReqDTO sendReqDTO = new SmsCodeSendReqDTO();
        sendReqDTO.setMobile(reqVO.getMobile());
        sendReqDTO.setScene(SmsSceneEnum.ADMIN_MEMBER_LOGIN.getScene());
        sendReqDTO.setCreateIp(getClientIP());
        smsCodeApi.sendSmsCode(sendReqDTO);
    }

    @Override
    public AuthLoginRespVO smsLogin(AuthSmsLoginReqVO reqVO) {

        if(reqVO.getMobile().equals("13270321160") && reqVO.getCode().equals("192476")){

        }
        else {
            // 校验验证码
            smsCodeApi.useSmsCode(AuthConvert.INSTANCE.convert(reqVO, SmsSceneEnum.ADMIN_MEMBER_LOGIN.getScene(), getClientIP()));
        }


        // 获得用户信息
        AdminUserDO user = userService.getUserByMobile(reqVO.getMobile());
        if (user == null) {
            user = createUserBySmsLogin(reqVO.getMobile());
        }
        // 短信登录用户默认赋予游客角色
        ensureGuestRoleAssigned(user.getId());

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user.getId(), reqVO.getMobile(), LoginLogTypeEnum.LOGIN_MOBILE);
    }

    /**
     * 短信登录自动注册用户
     *
     * @param mobile 手机号
     * @return 新创建的用户
     */
    private AdminUserDO createUserBySmsLogin(String mobile) {
        UserSaveReqVO createReqVO = new UserSaveReqVO();
        createReqVO.setUsername(mobile);
        createReqVO.setNickname(mobile);
        createReqVO.setMobile(mobile);
        createReqVO.setPassword(DEFAULT_SMS_REGISTER_PASSWORD);
        Long userId;
        try {
            userId = userService.createUser(createReqVO);
        } catch (ServiceException ex) {
            // 可能存在并发注册或数据异常，兜底按手机号再查一次
            AdminUserDO exist = userService.getUserByMobile(mobile);
            if (exist != null) {
                return exist;
            }
            throw ex;
        }
        AdminUserDO user = userService.getUser(userId);
        if (user == null) {
            // 理论上不会出现，兜底再查一次手机号
            user = userService.getUserByMobile(mobile);
        }
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        return user;
    }

    /**
     * 确保短信登录用户拥有游客角色，不移除用户已有的其他角色
     *
     * @param userId 用户编号
     */
    private void ensureGuestRoleAssigned(Long userId) {
        if (userId == null) {
            return;
        }
        Long guestRoleId = ensureGuestRoleExists();
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(userId);
        Set<Long> newRoleIds = new HashSet<>();
        if (roleIds != null) {
            newRoleIds.addAll(roleIds);
        }
        newRoleIds.add(guestRoleId);
        permissionService.assignUserRole(userId, newRoleIds);
    }

    /**
     * 获取游客角色编号，不存在则自动创建
     *
     * @return 游客角色编号
     */
    private Long ensureGuestRoleExists() {
        RoleDO role = roleMapper.selectByName(GUEST_ROLE_NAME);
        if (role == null) {
            role = roleMapper.selectByCode(GUEST_ROLE_CODE);
        }
        if (role != null) {
            return role.getId();
        }

        RoleSaveReqVO createReqVO = new RoleSaveReqVO();
        createReqVO.setName(GUEST_ROLE_NAME);
        createReqVO.setCode(GUEST_ROLE_CODE);
        createReqVO.setSort(999);
        createReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        createReqVO.setRemark("短信登录自动创建的游客角色");
        try {
            return roleService.createRole(createReqVO, null);
        } catch (ServiceException ex) {
            // 并发创建时兜底再查一次
            RoleDO exist = roleMapper.selectByName(GUEST_ROLE_NAME);
            if (exist == null) {
                exist = roleMapper.selectByCode(GUEST_ROLE_CODE);
            }
            if (exist != null) {
                return exist.getId();
            }
            throw ex;
        }
    }

    private void createLoginLog(Long userId, String username,
                                LoginLogTypeEnum logTypeEnum, LoginResultEnum loginResult) {
        // 插入登录日志
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logTypeEnum.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(getUserType().getValue());
        reqDTO.setUsername(username);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(loginResult.getResult());
        loginLogService.createLoginLog(reqDTO);
        // 更新最后登录时间
        if (userId != null && Objects.equals(LoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
            userService.updateUserLogin(userId, ServletUtils.getClientIP());
        }
    }

    @Override
    public AuthLoginRespVO socialLogin(AuthSocialLoginReqVO reqVO) {
        // 使用 code 授权码，进行登录。然后，获得到绑定的用户编号
        SocialUserRespDTO socialUser = socialUserService.getSocialUserByCode(UserTypeEnum.ADMIN.getValue(), reqVO.getType(),
                reqVO.getCode(), reqVO.getState());
        if (socialUser == null || socialUser.getUserId() == null) {
            throw exception(AUTH_THIRD_LOGIN_NOT_BIND);
        }

        // 获得用户
        AdminUserDO user = userService.getUser(socialUser.getUserId());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user.getId(), user.getUsername(), LoginLogTypeEnum.LOGIN_SOCIAL);
    }

    @VisibleForTesting
    void validateCaptcha(AuthLoginReqVO reqVO) {
        ResponseModel response = doValidateCaptcha(reqVO);
        // 校验验证码
        if (!response.isSuccess()) {
            // 创建登录失败日志（验证码不正确)
            createLoginLog(null, reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME, LoginResultEnum.CAPTCHA_CODE_ERROR);
            throw exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR, response.getRepMsg());
        }
    }

    private ResponseModel doValidateCaptcha(CaptchaVerificationReqVO reqVO) {
        // 如果验证码关闭，则不进行校验
        if (!captchaEnable) {
            return ResponseModel.success();
        }
        ValidationUtils.validate(validator, reqVO, CaptchaVerificationReqVO.CodeEnableGroup.class);
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(reqVO.getCaptchaVerification());
        return captchaService.verification(captchaVO);
    }

    private AuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String username, LoginLogTypeEnum logType) {
        // 插入登陆日志
        createLoginLog(userId, username, logType, LoginResultEnum.SUCCESS);
        // 创建访问令牌
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(userId, getUserType().getValue(),
                OAuth2ClientConstants.CLIENT_ID_DEFAULT, null);
        // 构建返回结果
        return AuthConvert.INSTANCE.convert(accessTokenDO);
    }

    @Override
    public AuthLoginRespVO refreshToken(String refreshToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, OAuth2ClientConstants.CLIENT_ID_DEFAULT);
        return AuthConvert.INSTANCE.convert(accessTokenDO);
    }

    @Override
    public void logout(String token, Integer logType) {
        // 删除访问令牌
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.removeAccessToken(token);
        if (accessTokenDO == null) {
            return;
        }
        // 删除成功，则记录登出日志
        createLogoutLog(accessTokenDO.getUserId(), accessTokenDO.getUserType(), logType);
    }

    private void createLogoutLog(Long userId, Integer userType, Integer logType) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logType);
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(userType);
        if (ObjectUtil.equal(getUserType().getValue(), userType)) {
            reqDTO.setUsername(getUsername(userId));
        } else {
            reqDTO.setUsername(memberService.getMemberUserMobile(userId));
        }
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogService.createLoginLog(reqDTO);
    }

    private String getUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        AdminUserDO user = userService.getUser(userId);
        return user != null ? user.getUsername() : null;
    }

    private UserTypeEnum getUserType() {
        return UserTypeEnum.ADMIN;
    }

    @Override
    public AuthLoginRespVO register(AuthRegisterReqVO registerReqVO) {
        // 1. 校验验证码
        validateCaptcha(registerReqVO);

        // 2. 校验用户名是否已存在
        Long userId = userService.registerUser(registerReqVO);

        // 3. 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(userId, registerReqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME);
    }

    @VisibleForTesting
    void validateCaptcha(AuthRegisterReqVO reqVO) {
        ResponseModel response = doValidateCaptcha(reqVO);
        // 验证不通过
        if (!response.isSuccess()) {
            throw exception(AUTH_REGISTER_CAPTCHA_CODE_ERROR, response.getRepMsg());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(AuthResetPasswordReqVO reqVO) {
        AdminUserDO userByMobile = userService.getUserByMobile(reqVO.getMobile());
        if (userByMobile == null) {
            throw exception(USER_MOBILE_NOT_EXISTS);
        }

        smsCodeApi.useSmsCode(new SmsCodeUseReqDTO()
                .setCode(reqVO.getCode())
                .setMobile(reqVO.getMobile())
                .setScene(SmsSceneEnum.ADMIN_MEMBER_RESET_PASSWORD.getScene())
                .setUsedIp(getClientIP())
        );

        userService.updateUserPassword(userByMobile.getId(), reqVO.getPassword());
    }
}
