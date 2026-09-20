package com.sydigit.yzwater.module.service.shortlink;

import com.sydigit.yzwater.module.controller.admin.vo.shortlink.ShortLinkResolveRespVO;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 短链服务
 */
public interface ShortLinkService {

    /**
     * 解析短链并计算跳转 URL（不做重定向，只返回解析结果）
     */
    ShortLinkResolveRespVO resolve(String code, String clientIp, String userAgent, boolean recordClick);

    /**
     * 创建短链记录并返回短链码 code（用于短信模板参数）。
     *
     * @param scene       跳转场景（例如 PROBLEM_PUBLIC_DETAIL / PROBLEM_HANDLER_TASK）
     * @param bizType     业务类型（例如 PROBLEM_FEEDBACK）
     * @param bizId       业务主键
     * @param mobilephone 手机号（用于短链免登录，可为空）
     * @param expiresTime 过期时间（为空表示不过期）
     * @param ext         扩展字段（可为空）
     * @return 短链码 code
     */
    String create(String scene, String bizType, Long bizId, String mobilephone, LocalDateTime expiresTime, Map<String, Object> ext);

}
