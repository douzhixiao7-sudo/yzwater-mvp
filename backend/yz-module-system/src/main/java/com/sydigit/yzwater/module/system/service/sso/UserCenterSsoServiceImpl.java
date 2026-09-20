package com.sydigit.yzwater.module.system.service.sso;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sydigit.yzwater.framework.common.util.http.HttpUtils;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.system.framework.sso.config.UserCenterSsoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * 用户中心单点登录服务实现
 */
@Service
@RequiredArgsConstructor
public class UserCenterSsoServiceImpl implements UserCenterSsoService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };

    private final UserCenterSsoProperties userCenterSsoProperties;

    @Override
    public Map<String, Object> getUserInfoByCode(String code) {
        validateConfig();
        String accessToken = obtainAccessToken(code);
        return obtainUserInfo(accessToken);
    }

    private void validateConfig() {
        if (!Boolean.TRUE.equals(userCenterSsoProperties.getEnabled())) {
            throw invalidParamException("用户中心单点登录未启用");
        }
        if (StrUtil.hasBlank(userCenterSsoProperties.getDomain(), userCenterSsoProperties.getClientCode(),
                userCenterSsoProperties.getClientId(), userCenterSsoProperties.getClientSecret(),
                userCenterSsoProperties.getRedirectUri())) {
            throw invalidParamException("用户中心单点登录配置不完整");
        }
    }

    private String obtainAccessToken(String code) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        String responseBody = HttpUtils.post(buildTokenUrl(), headers, buildTokenRequestBody(code));
        Map<String, Object> tokenResponse = JsonUtils.parseObject(responseBody, MAP_TYPE_REFERENCE);
        String accessToken = getString(tokenResponse, "access_token");
        if (StrUtil.isBlank(accessToken)) {
            throw invalidParamException("用户中心 access_token 获取失败");
        }
        return accessToken;
    }

    private Map<String, Object> obtainUserInfo(String accessToken) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        String responseBody = HttpUtils.get(buildUserInfoUrl(), headers);
        Map<String, Object> userInfo = JsonUtils.parseObject(responseBody, MAP_TYPE_REFERENCE);
        if (userInfo == null || userInfo.isEmpty()) {
            throw invalidParamException("用户中心用户信息获取失败");
        }
        return userInfo;
    }

    private String buildTokenUrl() {
        return StrUtil.removeSuffix(userCenterSsoProperties.getDomain(), "/")
                + "/api/v1/authorize/" + userCenterSsoProperties.getClientCode() + "/oauth2/token";
    }

    private String buildUserInfoUrl() {
        return StrUtil.removeSuffix(userCenterSsoProperties.getDomain(), "/")
                + "/api/v1/authorize/" + userCenterSsoProperties.getClientCode() + "/oauth2/userinfo";
    }

    private String buildTokenRequestBody(String code) {
        StringBuilder body = new StringBuilder();
        appendFormParam(body, "client_id", userCenterSsoProperties.getClientId());
        appendFormParam(body, "client_secret", userCenterSsoProperties.getClientSecret());
        appendFormParam(body, "grant_type", "authorization_code");
        appendFormParam(body, "code", code);
        appendFormParam(body, "redirect_uri", userCenterSsoProperties.getRedirectUri());
        return body.toString();
    }

    private void appendFormParam(StringBuilder body, String key, String value) {
        if (!body.isEmpty()) {
            body.append('&');
        }
        body.append(key)
                .append('=')
                .append(HttpUtils.encodeUtf8(StrUtil.nullToEmpty(value)));
    }

    private String getString(Map<String, Object> body, String key) {
        if (body == null) {
            return null;
        }
        Object value = body.get(key);
        if (value == null && body.get("data") instanceof Map<?, ?> data) {
            value = data.get(key);
        }
        return value == null ? null : new String(String.valueOf(value).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

}
