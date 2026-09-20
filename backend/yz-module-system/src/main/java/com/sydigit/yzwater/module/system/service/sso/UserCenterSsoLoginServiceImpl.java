package com.sydigit.yzwater.module.system.service.sso;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.enums.UserTypeEnum;
import com.sydigit.yzwater.framework.common.exception.ServiceException;
import com.sydigit.yzwater.framework.common.util.monitor.TracerUtils;
import com.sydigit.yzwater.framework.common.util.servlet.ServletUtils;
import com.sydigit.yzwater.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.sydigit.yzwater.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import com.sydigit.yzwater.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import com.sydigit.yzwater.module.system.convert.auth.AuthConvert;
import com.sydigit.yzwater.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.sydigit.yzwater.module.system.dal.dataobject.permission.RoleDO;
import com.sydigit.yzwater.module.system.dal.dataobject.user.AdminUserDO;
import com.sydigit.yzwater.module.system.dal.mysql.permission.RoleMapper;
import com.sydigit.yzwater.module.system.enums.logger.LoginLogTypeEnum;
import com.sydigit.yzwater.module.system.enums.logger.LoginResultEnum;
import com.sydigit.yzwater.module.system.enums.oauth2.OAuth2ClientConstants;
import com.sydigit.yzwater.module.system.service.logger.LoginLogService;
import com.sydigit.yzwater.module.system.service.oauth2.OAuth2TokenService;
import com.sydigit.yzwater.module.system.service.permission.PermissionService;
import com.sydigit.yzwater.module.system.service.user.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static com.sydigit.yzwater.module.system.enums.ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED;
import static com.sydigit.yzwater.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 用户中心单点登录编排服务实现
 */
@Service
@RequiredArgsConstructor
public class UserCenterSsoLoginServiceImpl implements UserCenterSsoLoginService {

    private static final String AUTO_ASSIGN_ROLE_CODE = "yzadmin";
    private static final String AUTO_ASSIGN_ROLE_NAME = "yzadmin";
    private static final int USERNAME_MIN_LENGTH = 4;
    private static final int USERNAME_MAX_LENGTH = 30;
    private static final int NICKNAME_MAX_LENGTH = 30;

    private final UserCenterSsoService userCenterSsoService;
    private final AdminUserService userService;
    private final PermissionService permissionService;
    private final RoleMapper roleMapper;
    private final OAuth2TokenService oauth2TokenService;
    private final LoginLogService loginLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginRespVO loginByCode(String code) {
        Map<String, Object> userInfo = userCenterSsoService.getUserInfoByCode(code);
        String preferredUsername = getString(userInfo, "preferred_username");
        String phone = getString(userInfo, "phone");
        String name = getString(userInfo, "name");

        AdminUserDO user = findOrCreateUser(preferredUsername, phone, name);
        validateUserStatus(user, preferredUsername, phone);
        ensureYzadminRole(user.getId());
        return createTokenAfterLoginSuccess(user);
    }

    private AdminUserDO findOrCreateUser(String preferredUsername, String phone, String name) {
        AdminUserDO user = getUser(preferredUsername, phone);
        if (user != null) {
            return user;
        }

        String localUsername = buildLocalUsername(preferredUsername, phone);
        UserSaveReqVO createReqVO = new UserSaveReqVO();
        createReqVO.setUsername(localUsername);
        createReqVO.setNickname(buildNickname(name, preferredUsername, phone, localUsername));
        createReqVO.setMobile(StrUtil.blankToDefault(phone, null));
        // SSO 用户不使用本地密码登录，为每个自动注册用户生成独立的随机密码。
        createReqVO.setPassword(java.util.UUID.randomUUID().toString());

        Long userId;
        try {
            userId = userService.createUser(createReqVO);
        } catch (ServiceException ex) {
            AdminUserDO existUser = getUser(preferredUsername, phone);
            if (existUser == null && !StrUtil.equals(localUsername, preferredUsername)) {
                existUser = userService.getUserByUsername(localUsername);
            }
            if (existUser != null) {
                return existUser;
            }
            throw ex;
        }

        AdminUserDO createdUser = userService.getUser(userId);
        if (createdUser == null && StrUtil.isNotBlank(phone)) {
            createdUser = userService.getUserByMobile(phone);
        }
        if (createdUser == null) {
            createdUser = userService.getUserByUsername(localUsername);
        }
        if (createdUser == null) {
            throw exception(USER_NOT_EXISTS);
        }
        return createdUser;
    }

    private AdminUserDO getUser(String preferredUsername, String phone) {
        if (StrUtil.isNotBlank(preferredUsername)) {
            AdminUserDO user = userService.getUserByUsername(preferredUsername);
            if (user != null) {
                return user;
            }
        }
        if (StrUtil.isNotBlank(phone)) {
            AdminUserDO user = userService.getUserByMobile(phone);
            if (user != null) {
                return user;
            }
        }
        String compatibleUsername = buildCompatibleUsername(preferredUsername);
        if (StrUtil.isNotBlank(compatibleUsername)
                && !StrUtil.equals(compatibleUsername, preferredUsername)) {
            return userService.getUserByUsername(compatibleUsername);
        }
        return null;
    }

    private void validateUserStatus(AdminUserDO user, String preferredUsername, String phone) {
        if (CommonStatusEnum.isDisable(user.getStatus())) {
            createLoginLog(user.getId(), resolveLoginIdentity(preferredUsername, phone, user),
                    LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }
    }

    private void ensureYzadminRole(Long userId) {
        RoleDO yzadminRole = roleMapper.selectByCode(AUTO_ASSIGN_ROLE_CODE);
        if (yzadminRole == null) {
            yzadminRole = roleMapper.selectByName(AUTO_ASSIGN_ROLE_NAME);
        }
        if (yzadminRole == null) {
            throw new IllegalArgumentException("未找到用户中心自动授权角色 yzadmin");
        }

        Set<Long> mergedRoleIds = new LinkedHashSet<>(
                Objects.requireNonNullElse(permissionService.getUserRoleIdListByUserId(userId), Collections.emptySet()));
        mergedRoleIds.add(yzadminRole.getId());
        permissionService.assignUserRole(userId, mergedRoleIds);
    }

    private AuthLoginRespVO createTokenAfterLoginSuccess(AdminUserDO user) {
        createLoginLog(user.getId(), user.getUsername(), LoginResultEnum.SUCCESS);
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(user.getId(),
                UserTypeEnum.ADMIN.getValue(), OAuth2ClientConstants.CLIENT_ID_DEFAULT, null);
        return AuthConvert.INSTANCE.convert(accessTokenDO);
    }

    private void createLoginLog(Long userId, String username, LoginResultEnum loginResult) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(LoginLogTypeEnum.LOGIN_SOCIAL.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(UserTypeEnum.ADMIN.getValue());
        reqDTO.setUsername(username);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(loginResult.getResult());
        loginLogService.createLoginLog(reqDTO);
        if (userId != null && Objects.equals(LoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
            userService.updateUserLogin(userId, ServletUtils.getClientIP());
        }
    }

    private String buildLocalUsername(String preferredUsername, String phone) {
        if (isValidLocalUsername(preferredUsername)) {
            return preferredUsername;
        }
        if (isValidLocalUsername(phone)) {
            return phone;
        }
        String compatibleUsername = buildCompatibleUsername(preferredUsername);
        if (isValidLocalUsername(compatibleUsername)) {
            return compatibleUsername;
        }
        throw invalidParamException("用户中心账号信息不完整，无法创建本地用户");
    }

    private String buildCompatibleUsername(String preferredUsername) {
        if (StrUtil.isBlank(preferredUsername)) {
            return null;
        }
        String sanitized = preferredUsername.replaceAll("[^A-Za-z0-9]", "");
        if (StrUtil.isBlank(sanitized)) {
            return null;
        }
        if (sanitized.length() > USERNAME_MAX_LENGTH) {
            sanitized = sanitized.substring(0, USERNAME_MAX_LENGTH);
        }
        if (sanitized.length() >= USERNAME_MIN_LENGTH) {
            return sanitized;
        }
        StringBuilder builder = new StringBuilder("uc").append(sanitized);
        while (builder.length() < USERNAME_MIN_LENGTH) {
            builder.append('0');
        }
        if (builder.length() > USERNAME_MAX_LENGTH) {
            return builder.substring(0, USERNAME_MAX_LENGTH);
        }
        return builder.toString();
    }

    private boolean isValidLocalUsername(String username) {
        return StrUtil.isNotBlank(username)
                && username.length() >= USERNAME_MIN_LENGTH
                && username.length() <= USERNAME_MAX_LENGTH
                && username.matches("^[A-Za-z0-9]+$");
    }

    private String buildNickname(String name, String preferredUsername, String phone, String localUsername) {
        String nickname = StrUtil.firstNonBlank(name, preferredUsername, phone, localUsername, "用户中心用户");
        if (nickname.length() > NICKNAME_MAX_LENGTH) {
            return nickname.substring(0, NICKNAME_MAX_LENGTH);
        }
        return nickname;
    }

    private String resolveLoginIdentity(String preferredUsername, String phone, AdminUserDO user) {
        return StrUtil.firstNonBlank(preferredUsername, phone, user.getUsername());
    }

    private String getString(Map<String, Object> userInfo, String key) {
        if (userInfo == null) {
            return null;
        }
        Object value = userInfo.get(key);
        if (value == null && userInfo.get("data") instanceof Map<?, ?> data) {
            value = data.get(key);
        }
        return value == null ? null : StrUtil.trim(String.valueOf(value));
    }

}
