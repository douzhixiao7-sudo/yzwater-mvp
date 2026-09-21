package com.sydigit.yzwater.module.system.service.sso;

import com.sydigit.yzwater.module.system.controller.admin.auth.vo.AuthLoginRespVO;

/**
 * 用户中心单点登录编排服务
 */
public interface UserCenterSsoLoginService {

    /**
     * 根据用户中心授权码完成本地登录
     *
     * @param code 用户中心授权码
     * @return 登录结果
     */
    AuthLoginRespVO loginByCode(String code);

}
