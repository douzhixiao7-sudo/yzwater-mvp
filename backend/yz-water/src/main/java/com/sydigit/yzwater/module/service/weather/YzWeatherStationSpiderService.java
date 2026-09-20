package com.sydigit.yzwater.module.service.weather;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sydigit.yzwater.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherStationDO;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherStationSsDO;
import com.sydigit.yzwater.module.dal.mysql.weather.YzWeatherStationMapper;
import com.sydigit.yzwater.module.dal.mysql.weather.YzWeatherStationSsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 天气站点实时数据采集服务
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class YzWeatherStationSpiderService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final YzWeatherStationMapper stationMapper;
    private final YzWeatherStationSsMapper stationSsMapper;

    @Value("${yz.weather.station-spider.enabled:true}")
    private boolean spiderEnabled;

    @Value("${yz.weather.station-spider.url-template:https://weatherwechat.chtot.com/DataMetar/GetSurfEleHoursByStaID?stationID={stationCode}&order=asc}")
    private String stationSpiderUrlTemplate;

    @Value("${yz.weather.station-spider.timeout-millis:8000}")
    private int timeoutMillis;

    @Value("${yz.weather.station-spider.cookie:}")
    private String stationSpiderCookie;

    @Value("${yz.weather.station-spider.referer:https://weatherwechat.chtot.com/Home/Index?appcode=yizheng_wx&title=0sfV98zsxvg=}")
    private String stationSpiderReferer;

    @Value("${yz.weather.station-spider.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36}")
    private String stationSpiderUserAgent;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> collectStationRealtime(List<String> stationCodeList) {
        if (!spiderEnabled) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.ERROR_CONFIGURATION.getCode(),
                    "天气站点采集已禁用，请检查配置 yz.weather.station-spider.enabled");
        }
        List<String> targetStationCodes = resolveTargetStationCodes(stationCodeList);
        if (CollUtil.isEmpty(targetStationCodes)) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(),
                    "天气站点为空，请先初始化 yz_weather_station");
        }

        int successCount = 0;
        List<Map<String, Object>> detailList = new ArrayList<>();
        for (String stationCode : targetStationCodes) {
            try {
                YzWeatherStationSsDO saved = collectSingleStation(stationCode);
                successCount++;
                detailList.add(buildDetail(stationCode, "success", saved.getId(), null));
            } catch (Exception ex) {
                log.error("[collectStationRealtime][站点采集失败][stationCode={}]", stationCode, ex);
                detailList.add(buildDetail(stationCode, "failed", null, ex.getMessage()));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", targetStationCodes.size());
        result.put("success", successCount);
        result.put("failed", targetStationCodes.size() - successCount);
        result.put("details", detailList);
        return result;
    }

    public YzWeatherStationSsDO getLatestByStationCode(String stationCode) {
        if (StrUtil.isBlank(stationCode)) {
            throw ServiceExceptionUtil.invalidParamException("stationCode 不能为空");
        }
        return stationSsMapper.selectLatestByStationCode(stationCode.trim());
    }

    private List<String> resolveTargetStationCodes(List<String> stationCodeList) {
        if (CollUtil.isNotEmpty(stationCodeList)) {
            LinkedHashSet<String> dedupCodes = new LinkedHashSet<>();
            for (String stationCode : stationCodeList) {
                String code = StrUtil.trimToNull(stationCode);
                if (code != null) {
                    dedupCodes.add(code);
                }
            }
            return new ArrayList<>(dedupCodes);
        }
        return stationMapper.selectStationCodeList();
    }

    private YzWeatherStationSsDO collectSingleStation(String stationCode) {
        String requestUrl = StrUtil.replace(stationSpiderUrlTemplate, "{stationCode}", stationCode);
        String responseText = requestStationWeather(requestUrl);
        Map<String, Object> weatherContent = parseWeatherContent(responseText);
        String parsedStationCode = parseStationCodeFromWeather(weatherContent, stationCode);
        String nowText = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        YzWeatherStationSsDO record = new YzWeatherStationSsDO();
        record.setId(IdUtil.fastSimpleUUID());
        record.setStationCode(parsedStationCode);
        record.setCreateTime(nowText);
        record.setWeather(weatherContent);
        stationSsMapper.insert(record);

        YzWeatherStationDO update = new YzWeatherStationDO();
        update.setStationSsId(record.getId());
        stationMapper.update(update, new LambdaUpdateWrapper<YzWeatherStationDO>()
                .eq(YzWeatherStationDO::getStationCode, parsedStationCode));
        return record;
    }

    private String requestStationWeather(String requestUrl) {
        HttpRequest request = HttpRequest.get(requestUrl)
                .timeout(timeoutMillis)
                .header("User-Agent", stationSpiderUserAgent)
                .header("Referer", stationSpiderReferer);
        if (StrUtil.isNotBlank(stationSpiderCookie)) {
            request.header("Cookie", stationSpiderCookie);
        }

        HttpResponse response;
        try {
            response = request.execute();
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "天气站点接口请求失败：{}", ex.getMessage());
        }
        if (!response.isOk()) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "天气站点接口响应异常，status={}", response.getStatus());
        }

        String body = response.body();
        if (StrUtil.isBlank(body)) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "天气站点接口响应为空");
        }
        return body;
    }

    private Map<String, Object> parseWeatherContent(String responseText) {
        try {
            return JsonUtils.parseObject(responseText, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "天气站点响应解析失败：{}", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String parseStationCodeFromWeather(Map<String, Object> weatherContent, String fallbackStationCode) {
        if (weatherContent == null) {
            return fallbackStationCode;
        }
        Object surfEleHoursObj = weatherContent.get("SurfEle_Hours");
        if (surfEleHoursObj == null) {
            return fallbackStationCode;
        }
        try {
            if (surfEleHoursObj instanceof String surfEleHoursJson) {
                List<Map<String, Object>> list = JsonUtils.parseObject(surfEleHoursJson,
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                if (CollUtil.isNotEmpty(list)) {
                    String stationCode = StrUtil.toStringOrNull(list.get(0).get("Station_ID_C"));
                    return StrUtil.blankToDefault(stationCode, fallbackStationCode);
                }
                return fallbackStationCode;
            }
            if (surfEleHoursObj instanceof List<?> rawList && CollUtil.isNotEmpty(rawList)) {
                Object first = rawList.get(0);
                if (first instanceof Map<?, ?> map) {
                    String stationCode = StrUtil.toStringOrNull(map.get("Station_ID_C"));
                    return StrUtil.blankToDefault(stationCode, fallbackStationCode);
                }
            }
        } catch (Exception ex) {
            log.warn("[parseStationCodeFromWeather][解析站点编码失败，使用兜底站点编码][stationCode={}]", fallbackStationCode, ex);
        }
        return fallbackStationCode;
    }

    private Map<String, Object> buildDetail(String stationCode, String status, String recordId, String errorMessage) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("stationCode", stationCode);
        detail.put("status", status);
        detail.put("recordId", recordId);
        detail.put("error", errorMessage);
        return detail;
    }

}
