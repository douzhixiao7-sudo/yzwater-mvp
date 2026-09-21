package com.sydigit.yzwater.module.service.shortlink;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sydigit.yzwater.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.sydigit.yzwater.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import com.sydigit.yzwater.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import com.sydigit.yzwater.framework.common.enums.UserTypeEnum;
import com.sydigit.yzwater.module.infra.api.config.ConfigApi;
import com.sydigit.yzwater.module.controller.admin.vo.shortlink.ShortLinkResolveRespVO;
import com.sydigit.yzwater.module.dal.dataobject.shortlink.YzShortLinkDO;
import com.sydigit.yzwater.module.dal.mysql.shortlink.YzShortLinkMapper;
import com.sydigit.yzwater.module.dal.mysql.system.SystemUserSimpleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 短链服务实现
 */
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl implements ShortLinkService {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,64}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{1,64}$");

    private static final String SCENE_PROBLEM_PUBLIC_DETAIL = "PROBLEM_PUBLIC_DETAIL";
    private static final String SCENE_PROBLEM_HANDLER_TASK = "PROBLEM_HANDLER_TASK";

    /**
     * 配置键：反馈人跳转地址
     */
    private static final String CONFIG_KEY_FEEDBACK_PERSON_REDIRECT_URL = "feedback_person_redirect_url";

    /**
     * 配置键：处理人跳转地址
     */
    private static final String CONFIG_KEY_HANDLE_PERSON_REDIRECT_URL = "handle_person_redirect_url";

    /**
     * 兜底 H5 入口（当未配置跳转地址或读取失败时使用）
     */
    private static final String DEFAULT_H5_ENTRY = "https://yzriver.sy-digit.com/h5/";

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    private static final char[] CODE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private final YzShortLinkMapper shortLinkMapper;
    private final SystemUserSimpleMapper systemUserSimpleMapper;
    private final OAuth2TokenCommonApi oauth2TokenApi;
    private final ConfigApi configApi;

    /**
     * OAuth2 客户端编号（用于生成访问令牌）。
     * <p>
     * 默认使用初始化数据里的 default。
     */
    @Value("${yz.short-link.oauth-client-id:default}")
    private String oauthClientId;

    @Override
    public ShortLinkResolveRespVO resolve(String code, String clientIp, String userAgent, boolean recordClick) {
        ShortLinkResolveRespVO resp = new ShortLinkResolveRespVO();
        resp.setCode(code);

        String normalizedCode = StrUtil.trimToEmpty(code);
        if (StrUtil.isBlank(normalizedCode) || !CODE_PATTERN.matcher(normalizedCode).matches()) {
            resp.setValid(false);
            resp.setInvalidReason("invalid");
            resp.setRedirectUrl(buildErrorUrl("invalid", normalizedCode));
            return resp;
        }

        YzShortLinkDO link = shortLinkMapper.selectOne("code", normalizedCode);
        if (link == null) {
            // 兼容阶段一：纯数字 code 作为业务ID（例如问题反馈ID）
            if (StrUtil.isNumeric(normalizedCode)) {
                resp.setValid(true);
                resp.setScene(SCENE_PROBLEM_PUBLIC_DETAIL);
                resp.setBizType("PROBLEM_FEEDBACK");
                resp.setBizId(Long.parseLong(normalizedCode));
                resp.setRedirectUrl(buildSceneUrl(SCENE_PROBLEM_PUBLIC_DETAIL, normalizedCode, null));
                return resp;
            }
            resp.setValid(false);
            resp.setInvalidReason("not_found");
            resp.setRedirectUrl(buildErrorUrl("not_found", normalizedCode));
            return resp;
        }

        resp.setScene(link.getScene());
        resp.setBizType(link.getBizType());
        resp.setBizId(link.getBizId());
        resp.setExpiresTime(link.getExpiresTime());

        if (Boolean.TRUE.equals(link.getDeleted())) {
            resp.setValid(false);
            resp.setInvalidReason("disabled");
            resp.setRedirectUrl(buildErrorUrl("disabled", normalizedCode));
            return resp;
        }
        if (!Objects.equals(link.getStatus(), 1)) {
            resp.setValid(false);
            resp.setInvalidReason("disabled");
            resp.setRedirectUrl(buildErrorUrl("disabled", normalizedCode));
            return resp;
        }
        LocalDateTime now = LocalDateTime.now();
        if (link.getExpiresTime() != null && link.getExpiresTime().isBefore(now)) {
            resp.setValid(false);
            resp.setInvalidReason("expired");
            resp.setRedirectUrl(buildErrorUrl("expired", normalizedCode));
            return resp;
        }

        resp.setValid(true);
        String token = generateTokenByMobile(link.getMobilephone());
        resp.setRedirectUrl(buildSceneUrl(link.getScene(), normalizedCode, token));

        if (recordClick) {
            recordClick(link.getId(), clientIp, userAgent);
        }
        return resp;
    }

    @Override
    public String create(String scene, String bizType, Long bizId, String mobilephone, LocalDateTime expiresTime, Map<String, Object> ext) {
        String normalizedScene = StrUtil.trimToEmpty(scene);
        String normalizedBizType = StrUtil.trimToEmpty(bizType);
        if (StrUtil.isBlank(normalizedScene) || StrUtil.isBlank(normalizedBizType) || bizId == null) {
            throw new IllegalArgumentException("短链创建参数不完整");
        }
        if (!NAME_PATTERN.matcher(normalizedScene).matches()) {
            // scene 仅用于记录与解析，不允许包含特殊字符，避免前端跳转路由被污染
            throw new IllegalArgumentException("短链场景不合法");
        }
        if (!NAME_PATTERN.matcher(normalizedBizType).matches()) {
            throw new IllegalArgumentException("短链业务类型不合法");
        }
        YzShortLinkDO link = new YzShortLinkDO();
        link.setId(SNOWFLAKE.nextId());
        link.setScene(normalizedScene);
        link.setBizType(normalizedBizType);
        link.setBizId(bizId);
        link.setMobilephone(StrUtil.trimToNull(mobilephone));
        link.setStatus(1);
        link.setExpiresTime(expiresTime);
        link.setClickCount(0L);
        link.setExt(ext);

        for (int attempt = 0; attempt < 10; attempt++) {
            String code = randomCode(8);
            if (shortLinkMapper.selectOne("code", code) != null) {
                continue;
            }
            link.setCode(code);
            try {
                shortLinkMapper.insert(link);
                return code;
            } catch (DuplicateKeyException ex) {
                // 极小概率并发冲突：重试生成新的 code
            }
        }
        throw new IllegalStateException("短链码生成失败，请稍后重试");
    }

    private void recordClick(Long id, String clientIp, String userAgent) {
        if (id == null) {
            return;
        }
        String ip = StrUtil.maxLength(StrUtil.blankToDefault(clientIp, ""), 64);
        String ua = StrUtil.maxLength(StrUtil.blankToDefault(userAgent, ""), 512);
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<YzShortLinkDO> wrapper = new LambdaUpdateWrapper<YzShortLinkDO>()
                .eq(YzShortLinkDO::getId, id)
                .setSql("click_count = COALESCE(click_count, 0) + 1")
                .set(YzShortLinkDO::getLastClickTime, now)
                .set(YzShortLinkDO::getLastClickIp, ip)
                .set(YzShortLinkDO::getLastClickUa, ua);
        shortLinkMapper.update(null, wrapper);
    }

    private String buildSceneUrl(String scene, String code, String token) {
        if (SCENE_PROBLEM_PUBLIC_DETAIL.equals(scene)) {//反馈人
            String baseUrl = getRedirectBaseUrl(CONFIG_KEY_FEEDBACK_PERSON_REDIRECT_URL,
                    normalizeH5Entry(DEFAULT_H5_ENTRY) + "shortUrlDetail");
            if (StrUtil.isNotBlank(token)) {
                return buildUrlWithQuery(baseUrl, Map.of("mark", token, "code", code));
            }
            return buildUrlWithQuery(baseUrl, Map.of("mark", code));
        }
        if (SCENE_PROBLEM_HANDLER_TASK.equals(scene)) {//处理人
            String baseUrl = getRedirectBaseUrl(CONFIG_KEY_HANDLE_PERSON_REDIRECT_URL,
                    normalizeH5Entry(DEFAULT_H5_ENTRY) + "adminLogin");
            if (StrUtil.isNotBlank(token)) {
                return buildUrlWithQuery(baseUrl, Map.of("code", code, "mark", token));
            }
            return buildUrlWithQuery(baseUrl, Map.of("code", code));
        }
        return buildErrorUrl("invalid", code);
    }

    private String buildErrorUrl(String reason, String code) {
        String baseUrl = normalizeH5Entry(DEFAULT_H5_ENTRY) + "sms/error";
        return buildUrlWithQuery(baseUrl, Map.of(
                "reason", StrUtil.blankToDefault(reason, ""),
                "code", StrUtil.blankToDefault(code, "")
        ));
    }

    private String normalizeH5Entry(String entry) {
        String value = StrUtil.trimToEmpty(entry);
        if (StrUtil.isBlank(value)) {
            value = DEFAULT_H5_ENTRY;
        }
        return value;
    }

    /**
     * 根据配置键获取跳转地址。
     * <p>
     * 说明：前端通过 /infra/config/page 按 key 查询时，最终也是取配置项的 value；
     * 服务端为避免权限拦截与额外的 HTTP 调用，直接通过 ConfigApi 读取同一份配置数据。
     */
    private String getRedirectBaseUrl(String configKey, String fallbackUrl) {
        String key = StrUtil.trimToNull(configKey);
        if (key == null) {
            return fallbackUrl;
        }
        try {
            String value = StrUtil.trimToNull(configApi.getConfigValueByKey(key));
            return value != null ? value : fallbackUrl;
        } catch (Exception ignore) {
            // 读取配置失败时，降级为兜底地址，避免短链跳转 500
            return fallbackUrl;
        }
    }

    private String buildUrlWithQuery(String baseUrl, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(StrUtil.blankToDefault(baseUrl, ""));
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach((key, value) -> builder.queryParam(key, StrUtil.blankToDefault(value, "")));
        }
        return builder.encode(StandardCharsets.UTF_8).build().toUriString();
    }

    private String randomCode(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = CODE_CHARS[random.nextInt(CODE_CHARS.length)];
        }
        return new String(buf);
    }

    /**
     * 根据手机号生成访问令牌，供 H5 端免登录使用。
     * <p>
     * 说明：
     * <ul>
     *     <li>手机号为空或查不到用户时，返回空，前端走原有登录逻辑</li>
     *     <li>当前项目 app-api 暂不区分用户类型，统一按 ADMIN 生成 token</li>
     * </ul>
     */
    private String generateTokenByMobile(String mobilephone) {
        String mobile = StrUtil.trimToNull(mobilephone);
        if (mobile == null) {
            return null;
        }
        Long userId = systemUserSimpleMapper.selectIdByMobile(mobile);
        if (userId == null) {
            return null;
        }
        try {
            OAuth2AccessTokenCreateReqDTO reqDTO = new OAuth2AccessTokenCreateReqDTO();
            reqDTO.setUserId(userId);
            reqDTO.setUserType(UserTypeEnum.ADMIN.getValue());
            reqDTO.setClientId(StrUtil.blankToDefault(StrUtil.trimToNull(oauthClientId), "default"));
            OAuth2AccessTokenRespDTO tokenResp = oauth2TokenApi.createAccessToken(reqDTO);
            return tokenResp == null ? null : StrUtil.trimToNull(tokenResp.getAccessToken());
        } catch (Exception ignore) {
            // 生成 token 失败时，降级为走原有登录逻辑，避免短链跳转 500
            return null;
        }
    }

}
