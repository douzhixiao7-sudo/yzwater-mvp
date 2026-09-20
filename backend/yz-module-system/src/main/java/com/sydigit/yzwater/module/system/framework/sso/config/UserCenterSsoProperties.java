package com.sydigit.yzwater.module.system.framework.sso.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用户中心单点登录配置
 */
@ConfigurationProperties(prefix = "yz.system.sso.user-center")
@Data
public class UserCenterSsoProperties {

    /**
     * 是否启用
     */
    private Boolean enabled = false;

    /**
     * 用户中心域名
     */
    private String domain;

    /**
     * 应用编码
     */
    private String clientCode;

    /**
     * 客户端 ID
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 授权回调地址
     */
    private String redirectUri;

    /**
     * 授权范围
     */
    private String scope = "openid phone profile";

}
