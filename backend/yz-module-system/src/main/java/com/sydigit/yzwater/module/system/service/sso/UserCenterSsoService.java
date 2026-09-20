package com.sydigit.yzwater.module.system.service.sso;

import java.util.Map;

/**
 * 用户中心单点登录服务
 */
public interface UserCenterSsoService {

    /**
     * 使用授权码获取用户中心用户信息
     *
     * @param code 授权码
     * @return 用户中心用户信息
     */
    Map<String, Object> getUserInfoByCode(String code);

}
