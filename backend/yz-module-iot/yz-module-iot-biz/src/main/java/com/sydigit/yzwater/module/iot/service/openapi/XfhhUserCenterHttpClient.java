package com.sydigit.yzwater.module.iot.service.openapi;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 幸福河湖平台用户中心 HTTP 客户端
 */
@Slf4j
@Service
public class XfhhUserCenterHttpClient implements XfhhUserCenterClient {

    @Value("${yz.usercenter.client-id:}")
    private String clientId;

    @Value("${yz.usercenter.client-secret:}")
    private String clientSecret;

    @Value("${yz.usercenter.get-access-token-url:}")
    private String getAccessTokenUrl;

    @Value("${yz.usercenter.get-user-info-url:}")
    private String getUserInfoUrl;

    @Override
    public String getAccessToken(String code, String redirectUri) {
        if (StrUtil.hasBlank(clientId, clientSecret, getAccessTokenUrl, code, redirectUri)) {
            return "";
        }
        Map<String, Object> formMap = new HashMap<>();
        formMap.put("client_id", clientId);
        formMap.put("client_secret", clientSecret);
        formMap.put("grant_type", "authorization_code");
        formMap.put("code", code);
        formMap.put("redirect_uri", redirectUri);
        try {
            HttpResponse response = HttpRequest.post(getAccessTokenUrl)
                    .header("Content-type", "application/x-www-form-urlencoded")
                    .setConnectionTimeout(5000)
                    .setReadTimeout(10000)
                    .form(formMap)
                    .execute();
            if (!response.isOk()) {
                return "";
            }
            JSONObject jsonObject = JSONUtil.parseObj(response.body());
            return jsonObject.getStr("access_token", "");
        } catch (Exception ex) {
            log.warn("获取用户中心 accessToken 失败", ex);
            return "";
        }
    }

    @Override
    public String getUserinfo(String accessToken) {
        if (StrUtil.hasBlank(getUserInfoUrl, accessToken)) {
            return "";
        }
        try {
            HttpResponse response = HttpRequest.get(getUserInfoUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .setConnectionTimeout(5000)
                    .setReadTimeout(10000)
                    .execute();
            if (!response.isOk()) {
                return "";
            }
            JSONObject jsonObject = JSONUtil.parseObj(response.body());
            return jsonObject.getStr("preferred_username", "");
        } catch (Exception ex) {
            log.warn("获取用户中心用户信息失败", ex);
            return "";
        }
    }
}
