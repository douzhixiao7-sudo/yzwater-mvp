package com.sydigit.yzwater.module.service.wechat;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sydigit.yzwater.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.util.http.HttpUtils;
import com.sydigit.yzwater.module.controller.admin.vo.wechat.WechatSignRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信 JS-SDK 签名服务
 */
@Service
@Slf4j
public class WechatSignService {

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";
    private static final long EXPIRE_MILLIS = 7000L * 1000L;

    private final ObjectMapper objectMapper;
    private final ReentrantLock refreshLock = new ReentrantLock();

    @Value("${wechat.appid:}")
    private String configAppId;

    @Value("${wechat.secret:}")
    private String configSecret;

    @Value("${wx.mp.app-id:}")
    private String mpAppId;

    @Value("${wx.mp.secret:}")
    private String mpSecret;

    private volatile String jsapiTicket = "";
    private volatile long expireTime = 0L;

    public WechatSignService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public WechatSignRespVO sign(String url) {
        String appId = resolveAppId();
        String secret = resolveSecret();
        if (StrUtil.isBlank(appId) || StrUtil.isBlank(secret)) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.ERROR_CONFIGURATION.getCode(),
                    "微信配置未设置，请检查 WECHAT_APPID/WECHAT_SECRET 或 wx.mp.app-id/wx.mp.secret");
        }

        String decoded = decodeUrl(url);
        String normalized = normalizeUrl(decoded);
        String ticket = getJsapiTicket(appId, secret);
        return createSignature(appId, ticket, normalized);
    }

    private String getJsapiTicket(String appId, String secret) {
        long now = System.currentTimeMillis();
        if (StrUtil.isNotBlank(jsapiTicket) && now < expireTime) {
            return jsapiTicket;
        }
        refreshLock.lock();
        try {
            if (StrUtil.isNotBlank(jsapiTicket) && now < expireTime) {
                return jsapiTicket;
            }
            String token = fetchAccessToken(appId, secret);
            String ticket = fetchJsapiTicket(token);
            jsapiTicket = ticket;
            expireTime = now + EXPIRE_MILLIS;
            return jsapiTicket;
        } finally {
            refreshLock.unlock();
        }
    }

    private String fetchAccessToken(String appId, String secret) {
        String url = UriComponentsBuilder.fromHttpUrl(TOKEN_URL)
                .queryParam("grant_type", "client_credential")
                .queryParam("appid", appId)
                .queryParam("secret", secret)
                .toUriString();
        String response = HttpUtils.get(url, Map.of());
        JsonNode node = readTree(response, "获取 access_token 失败");
        if (node.has("errcode") && node.get("errcode").asInt(0) != 0) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "获取 access_token 失败: {}", node.path("errmsg").asText(node.toString()));
        }
        String token = node.path("access_token").asText();
        if (StrUtil.isBlank(token)) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "获取 access_token 失败: {}", node.toString());
        }
        return token;
    }

    private String fetchJsapiTicket(String accessToken) {
        String url = UriComponentsBuilder.fromHttpUrl(TICKET_URL)
                .queryParam("access_token", accessToken)
                .queryParam("type", "jsapi")
                .toUriString();
        String response = HttpUtils.get(url, Map.of());
        JsonNode node = readTree(response, "获取 jsapi_ticket 失败");
        if (node.has("errcode") && node.get("errcode").asInt(0) != 0) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "获取 jsapi_ticket 失败: {}", node.path("errmsg").asText(node.toString()));
        }
        String ticket = node.path("ticket").asText();
        if (StrUtil.isBlank(ticket)) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "获取 jsapi_ticket 失败: {}", node.toString());
        }
        return ticket;
    }

    private JsonNode readTree(String body, String message) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "{}: {}", message, body);
        }
    }

    private WechatSignRespVO createSignature(String appId, String ticket, String url) {
        String nonceStr = randomNonceStr(15);
        long timestamp = System.currentTimeMillis() / 1000;
        String raw = String.format("jsapi_ticket=%s&noncestr=%s&timestamp=%s&url=%s",
                ticket, nonceStr, timestamp, url);
        String signature = sha1(raw);
        WechatSignRespVO resp = new WechatSignRespVO();
        resp.setAppId(appId);
        resp.setTimestamp(timestamp);
        resp.setNonceStr(nonceStr);
        resp.setSignature(signature);
        return resp;
    }

    private String sha1(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "SHA1 计算失败");
        }
    }

    private String randomNonceStr(int length) {
        final String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return url;
        }
    }

    private String normalizeUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return url;
        }
        String base = url.split("#", 2)[0];
        try {
            URI uri = new URI(base);
            String path = uri.getPath();
            if (StrUtil.isNotBlank(path) && path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            int port = uri.getPort();
            String scheme = uri.getScheme();
            if ("https".equalsIgnoreCase(scheme) && port == 443) {
                port = -1;
            }
            if ("http".equalsIgnoreCase(scheme) && port == 80) {
                port = -1;
            }
            URI normalized = new URI(
                    scheme,
                    uri.getUserInfo(),
                    uri.getHost(),
                    port,
                    path,
                    uri.getQuery(),
                    null
            );
            return normalized.toString();
        } catch (Exception ex) {
            log.warn("URL 规范化失败，使用原始值：{}", base);
            return base;
        }
    }

    private String resolveAppId() {
        String envAppId = System.getenv("WECHAT_APPID");
        return firstNotBlank(configAppId, mpAppId, envAppId);
    }

    private String resolveSecret() {
        String envSecret = System.getenv("WECHAT_SECRET");
        return firstNotBlank(configSecret, mpSecret, envSecret);
    }

    private String firstNotBlank(String... candidates) {
        for (String candidate : candidates) {
            if (StrUtil.isNotBlank(candidate)) {
                return candidate.trim();
            }
        }
        return "";
    }
}
