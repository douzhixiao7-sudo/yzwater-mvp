package com.sydigit.yzwater.module.service.weather;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherDO;
import com.sydigit.yzwater.module.dal.mysql.weather.YzWeatherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 天气采集服务
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class YzWeatherService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final YzWeatherMapper weatherMapper;

    @Value("${yz.weather.enabled:true}")
    private boolean weatherEnabled;

    @Value("${yz.weather.url:}")
    private String weatherUrl;

    @Value("${yz.weather.timeout-millis:5000}")
    private int timeoutMillis;

    @Value("${yz.weather.append-timestamp:false}")
    private boolean appendTimestamp;

    /**
     * 按 beginTime 查询天气缓存，不存在则实时采集并入库
     */
    public Map<String, Object> getWeatherByBeginTime(String beginTime) {
        String synTime = normalizeSynTime(beginTime);
        YzWeatherDO exists = weatherMapper.selectOne(new LambdaQueryWrapper<YzWeatherDO>()
                .eq(YzWeatherDO::getSynTime, synTime)
                .orderByDesc(YzWeatherDO::getUpdateTime)
                .orderByDesc(YzWeatherDO::getCreateTime)
                .last("limit 1"));
        if (exists != null && CollUtil.isNotEmpty(exists.getWeatherContent())) {
            return exists.getWeatherContent();
        }
        return collectWeatherBySynTime(synTime);
    }

    /**
     * 采集指定日期天气并落库；synTime 为空时默认当天
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> collectWeatherBySynTime(String synTime) {
        String normalizedSynTime = normalizeSynTime(synTime);
        Map<String, Object> payload = requestWeatherFromThirdParty(normalizedSynTime);

        YzWeatherDO update = new YzWeatherDO();
        update.setWeatherContent(payload);
        update.setUpdateTime(LocalDateTime.now());
        int updated = weatherMapper.update(update, new LambdaQueryWrapper<YzWeatherDO>()
                .eq(YzWeatherDO::getSynTime, normalizedSynTime));
        if (updated > 0) {
            return payload;
        }

        YzWeatherDO insert = new YzWeatherDO();
        insert.setId(IdUtil.fastSimpleUUID());
        insert.setSynTime(normalizedSynTime);
        insert.setWeatherContent(payload);
        weatherMapper.insert(insert);
        return payload;
    }

    private Map<String, Object> requestWeatherFromThirdParty(String synTime) {
        if (!weatherEnabled) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.ERROR_CONFIGURATION.getCode(),
                    "天气采集已禁用，请检查配置 yz.weather.enabled");
        }
        String template = StrUtil.trimToNull(weatherUrl);
        if (template == null) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.ERROR_CONFIGURATION.getCode(),
                    "天气接口地址未配置，请检查配置 yz.weather.url");
        }

        String requestUrl = buildRequestUrl(template, synTime);
        String responseText;
        try {
            responseText = HttpUtil.get(requestUrl, timeoutMillis);
        } catch (Exception ex) {
            log.error("[requestWeatherFromThirdParty][调用天气接口失败][url={}]", requestUrl, ex);
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "调用天气接口失败：{}", ex.getMessage());
        }
        if (StrUtil.isBlank(responseText)) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "天气接口返回为空");
        }

        Map<String, Object> payload;
        try {
            payload = JsonUtils.parseObject(responseText, Map.class);
        } catch (Exception ex) {
            log.error("[requestWeatherFromThirdParty][解析天气接口响应失败][body={}]", truncate(responseText, 1000), ex);
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "天气接口响应解析失败");
        }
        if (CollUtil.isEmpty(payload)) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "天气接口响应为空对象");
        }

        Object status = payload.get("status");
        if (status != null && !"ok".equalsIgnoreCase(String.valueOf(status))) {
            String apiCode = firstNonBlank(payload.get("error_code"), payload.get("code"), payload.get("api_status"));
            String apiMessage = firstNonBlank(payload.get("message"), payload.get("msg"), payload.get("error"));
            log.warn("[requestWeatherFromThirdParty][天气接口返回业务失败][url={}, synTime={}, status={}, code={}, message={}, body={}]",
                    requestUrl, synTime, String.valueOf(status), apiCode, apiMessage, truncate(responseText, 1000));
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "天气接口返回异常状态：{}，code={}，message={}。请检查 yz.weather.url 的 token 或接口参数",
                    String.valueOf(status), StrUtil.blankToDefault(apiCode, "未知"), StrUtil.blankToDefault(apiMessage, "无"));
        }
        return payload;
    }

    private String buildRequestUrl(String template, String synTime) {
        long timestamp = LocalDate.parse(synTime, DATE_FORMATTER)
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond();
        String url = template;
        if (url.contains("{synTime}")) {
            url = StrUtil.replace(url, "{synTime}", synTime);
        }
        if (url.contains("{timestamp}")) {
            url = StrUtil.replace(url, "{timestamp}", String.valueOf(timestamp));
        } else if (appendTimestamp) {
            url = url + timestamp;
        } else if (url.contains("%d")) {
            try {
                url = String.format(url, timestamp);
            } catch (Exception ex) {
                log.warn("[buildRequestUrl][URL 模板使用 %d 格式化失败，按原值请求][url={}]", template);
                url = template;
            }
        }
        return url;
    }

    private String normalizeSynTime(String synTime) {
        String value = StrUtil.trimToNull(synTime);
        if (value == null) {
            return LocalDate.now().format(DATE_FORMATTER);
        }
        try {
            return LocalDate.parse(value, DATE_FORMATTER).format(DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw ServiceExceptionUtil.invalidParamException(
                    "日期格式不正确，要求 yyyy-MM-dd，例如 2026-03-23");
        }
    }

    private static String firstNonBlank(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            String text = value == null ? null : String.valueOf(value);
            if (StrUtil.isNotBlank(text)) {
                return text;
            }
        }
        return null;
    }

    private static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
