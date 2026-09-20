package com.sydigit.yzwater.module.iot.service.openapi;

/**
 * 幸福河湖平台用户中心客户端
 */
public interface XfhhUserCenterClient {

    /**
     * 根据授权码换取 accessToken
     */
    String getAccessToken(String code, String redirectUri);

    /**
     * 根据 accessToken 获取用户名
     */
    String getUserinfo(String accessToken);
}
