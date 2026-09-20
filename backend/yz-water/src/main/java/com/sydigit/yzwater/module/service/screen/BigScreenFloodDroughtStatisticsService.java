package com.sydigit.yzwater.module.service.screen;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sydigit.yzwater.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodDroughtFyImageRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodDroughtRainfallRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodDroughtStationWeatherRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenFloodDroughtWeatherIndicatorRespVO;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherDO;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherRealtimeFyDO;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherStationDO;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherStationSsDO;
import com.sydigit.yzwater.module.dal.mysql.weather.YzWeatherMapper;
import com.sydigit.yzwater.module.dal.mysql.weather.YzWeatherRealtimeFyMapper;
import com.sydigit.yzwater.module.dal.mysql.weather.YzWeatherStationMapper;
import com.sydigit.yzwater.module.dal.mysql.weather.YzWeatherStationSsMapper;
import com.sydigit.yzwater.module.service.weather.YzWeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 大屏统计-防汛抗旱 服务
 */
@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class BigScreenFloodDroughtStatisticsService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter WEATHER_TIME_SOURCE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter WEATHER_TIME_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final BigDecimal INVALID_THRESHOLD = new BigDecimal("999000");

    private final YzWeatherStationMapper stationMapper;
    private final YzWeatherStationSsMapper stationSsMapper;
    private final YzWeatherRealtimeFyMapper realtimeFyMapper;
    private final YzWeatherMapper weatherMapper;
    private final YzWeatherService weatherService;

    @Value("${yz.weather.fy.storage-root:.}")
    private String fyStorageRoot;

    @Value("${yz.weather.cache-max-age-minutes:30}")
    private long weatherCacheMaxAgeMinutes;

    /**
     * 接口1：获取所有站点名称
     */
    public List<String> getStationNames() {
        List<YzWeatherStationDO> stations = stationMapper.selectList(new LambdaQueryWrapper<YzWeatherStationDO>()
                .select(YzWeatherStationDO::getStationCode, YzWeatherStationDO::getStationName));
        if (CollUtil.isEmpty(stations)) {
            return List.of();
        }
        stations.sort((left, right) -> compareStationCode(left.getStationCode(), right.getStationCode()));
        return stations.stream()
                .map(YzWeatherStationDO::getStationName)
                .filter(StrUtil::isNotBlank)
                .map(StrUtil::trim)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
    }

    /**
     * 站点编码排序规则：
     * 1. 非字母开头在前
     * 2. 字母开头按首字母升序
     * 3. 首字母一致时按后续数字升序
     */
    private int compareStationCode(String leftCode, String rightCode) {
        String left = StrUtil.trimToEmpty(leftCode);
        String right = StrUtil.trimToEmpty(rightCode);
        if (StrUtil.equals(left, right)) {
            return 0;
        }
        if (StrUtil.isBlank(left)) {
            return 1;
        }
        if (StrUtil.isBlank(right)) {
            return -1;
        }

        boolean leftStartsWithLetter = Character.isLetter(left.charAt(0));
        boolean rightStartsWithLetter = Character.isLetter(right.charAt(0));
        if (leftStartsWithLetter != rightStartsWithLetter) {
            return leftStartsWithLetter ? 1 : -1;
        }

        if (!leftStartsWithLetter) {
            Long leftNumber = parseLongOrNull(left);
            Long rightNumber = parseLongOrNull(right);
            if (leftNumber != null && rightNumber != null) {
                int numberCompare = leftNumber.compareTo(rightNumber);
                if (numberCompare != 0) {
                    return numberCompare;
                }
            }
            return left.compareToIgnoreCase(right);
        }

        char leftPrefix = Character.toUpperCase(left.charAt(0));
        char rightPrefix = Character.toUpperCase(right.charAt(0));
        int prefixCompare = Character.compare(leftPrefix, rightPrefix);
        if (prefixCompare != 0) {
            return prefixCompare;
        }

        Long leftNumber = parseLongOrNull(StrUtil.trim(left.substring(1)));
        Long rightNumber = parseLongOrNull(StrUtil.trim(right.substring(1)));
        if (leftNumber != null && rightNumber != null) {
            int numberCompare = leftNumber.compareTo(rightNumber);
            if (numberCompare != 0) {
                return numberCompare;
            }
        }
        return left.compareToIgnoreCase(right);
    }

    private Long parseLongOrNull(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 接口2：根据站点名称返回对应实时天气
     */
    public BigScreenFloodDroughtStationWeatherRespVO getStationWeatherByStationName(String stationName) {
        String normalizedName = StrUtil.trimToNull(stationName);
        if (normalizedName == null) {
            throw ServiceExceptionUtil.invalidParamException("stationName 不能为空");
        }

        YzWeatherStationDO station = stationMapper.selectOne(new LambdaQueryWrapper<YzWeatherStationDO>()
                .eq(YzWeatherStationDO::getStationName, normalizedName)
                .orderByAsc(YzWeatherStationDO::getStationCode)
                .last("limit 1"));
        if (station == null) {
            throw ServiceExceptionUtil.invalidParamException("未找到站点：{}", normalizedName);
        }

        YzWeatherStationSsDO stationSs = stationSsMapper.selectLatestByStationCode(station.getStationCode());
        BigScreenFloodDroughtStationWeatherRespVO respVO = new BigScreenFloodDroughtStationWeatherRespVO();
        respVO.setStationName(station.getStationName());
        respVO.setStationCode(station.getStationCode());
        if (stationSs != null) {
            respVO.setCreateTime(stationSs.getCreateTime());
            respVO.setWeather(stationSs.getWeather());
        }
        return respVO;
    }

    /**
     * 接口3：未来 1h/12h/24h 降雨量
     */
    public BigScreenFloodDroughtRainfallRespVO getRainfallForecast() {
        Map<String, Object> weatherContent = getLatestWeatherContent();
        List<BigDecimal> precipValues = extractHourlyPrecipitationValues(weatherContent);

        BigScreenFloodDroughtRainfallRespVO respVO = new BigScreenFloodDroughtRainfallRespVO();
        if (CollUtil.isNotEmpty(precipValues)) {
            respVO.setRainfallNext1h(sumFirstHours(precipValues, 1));
            respVO.setRainfallNext12h(sumFirstHours(precipValues, 12));
            respVO.setRainfallNext24h(sumFirstHours(precipValues, 24));
            return respVO;
        }

        // 兜底：参考旧项目站点实时字段 PRE_1h
        Map<String, Object> latestSurfMap = getLatestStationSurfMap();
        BigDecimal pre1h = toNormalizedDecimal(getMapValueIgnoreCase(latestSurfMap, "PRE_1h"));
        if (pre1h != null) {
            respVO.setRainfallNext1h(pre1h);
            respVO.setRainfallNext12h(pre1h.multiply(BigDecimal.valueOf(12)).setScale(2, RoundingMode.HALF_UP));
            respVO.setRainfallNext24h(pre1h.multiply(BigDecimal.valueOf(24)).setScale(2, RoundingMode.HALF_UP));
            return respVO;
        }

        respVO.setRainfallNext1h(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        respVO.setRainfallNext12h(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        respVO.setRainfallNext24h(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        return respVO;
    }

    /**
     * 接口4：按 weatherTime 返回当天 FY 图层（weatherImage 转完整 URL）
     */
    public List<BigScreenFloodDroughtFyImageRespVO> getTodayFyImages(String day, String baseUrl) {
        LocalDate targetDay = parseTargetDay(day);
        String dayPrefix = targetDay.format(DAY_FORMATTER);

        List<YzWeatherRealtimeFyDO> list = realtimeFyMapper.selectList(new LambdaQueryWrapper<YzWeatherRealtimeFyDO>()
                .likeRight(YzWeatherRealtimeFyDO::getWeatherTime, dayPrefix)
                .orderByDesc(YzWeatherRealtimeFyDO::getWeatherTime)
                .orderByDesc(YzWeatherRealtimeFyDO::getId));
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }

        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        return list.stream()
                .sorted((a, b) -> StrUtil.blankToDefault(b.getWeatherTime(), "").compareTo(
                        StrUtil.blankToDefault(a.getWeatherTime(), "")))
                .map(it -> {
                    BigScreenFloodDroughtFyImageRespVO vo = new BigScreenFloodDroughtFyImageRespVO();
                    vo.setId(it.getId());
                    vo.setType(it.getType());
                    vo.setWeatherTime(formatWeatherTimeDisplay(it.getWeatherTime()));
                    vo.setFileName(it.getFileName());
                    vo.setWeatherImage(toFullImageUrl(normalizedBaseUrl, it.getWeatherImage()));
                    return vo;
                }).collect(Collectors.toList());
    }

    /**
     * 免登录返回 FY 图层图片内容
     */
    public void writeFyImage(String id, HttpServletResponse response) {
        String normalizedId = StrUtil.trimToNull(id);
        if (normalizedId == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        YzWeatherRealtimeFyDO fy = realtimeFyMapper.selectById(normalizedId);
        if (fy == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        File imageFile = resolveFyImageFile(fy.getWeatherImage());
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        response.setHeader("Cache-Control", "public, max-age=300");
        response.setContentType(detectContentType(imageFile.getName()));
        try {
            response.getOutputStream().write(FileUtil.readBytes(imageFile));
        } catch (IOException ex) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "FY 图片输出失败：id={}, message={}", normalizedId, ex.getMessage());
        }
    }

    /**
     * 接口5：温度、湿度、风力、PM2.5、空气质量
     */
    public BigScreenFloodDroughtWeatherIndicatorRespVO getWeatherIndicators() {
        Map<String, Object> weatherContent = getLatestWeatherContent();
        Map<String, Object> realtimeMap = getRealtimeMap(weatherContent);
        Map<String, Object> latestSurfMap = getLatestStationSurfMap();

        BigScreenFloodDroughtWeatherIndicatorRespVO respVO = new BigScreenFloodDroughtWeatherIndicatorRespVO();

        BigDecimal temperature = firstDecimal(
                getNestedValue(realtimeMap, "temperature"),
                getMapValueIgnoreCase(latestSurfMap, "TEM")
        );
        respVO.setTemperature(temperature);

        BigDecimal humidity = firstDecimal(
                getNestedValue(realtimeMap, "humidity"),
                getMapValueIgnoreCase(latestSurfMap, "RHU")
        );
        if (humidity != null && humidity.compareTo(BigDecimal.ZERO) >= 0 && humidity.compareTo(BigDecimal.ONE) <= 0) {
            humidity = humidity.multiply(BigDecimal.valueOf(100));
        }
        respVO.setHumidity(scale2(humidity));

        BigDecimal windSpeed = firstDecimal(
                getNestedValue(realtimeMap, "wind", "speed"),
                getMapValueIgnoreCase(latestSurfMap, "WIN_S_Avg_2mi")
        );
        respVO.setWindSpeed(windSpeed);

        BigDecimal pm25 = firstDecimal(
                getNestedValue(realtimeMap, "air_quality", "pm25", "value"),
                getNestedValue(realtimeMap, "air_quality", "pm25"),
                getFirstHourlyValue(weatherContent, "pm25")
        );
        respVO.setPm25(pm25);

        Object aqiVal = firstNonNull(
                getNestedValue(realtimeMap, "air_quality", "aqi", "chn"),
                getNestedValue(realtimeMap, "air_quality", "aqi", "usa"),
                getNestedValue(realtimeMap, "air_quality", "aqi")
        );
        String qualityDesc = firstNonBlank(
                toStringSafe(getNestedValue(realtimeMap, "air_quality", "description", "chn")),
                toStringSafe(getNestedValue(realtimeMap, "air_quality", "description", "usa")),
                toStringSafe(getNestedValue(realtimeMap, "air_quality", "description"))
        );
        BigDecimal aqi = toNormalizedDecimal(aqiVal);
        if (StrUtil.isNotBlank(qualityDesc) && aqi != null) {
            respVO.setAirQuality(qualityDesc + "(" + aqi.stripTrailingZeros().toPlainString() + ")");
        } else if (StrUtil.isNotBlank(qualityDesc)) {
            respVO.setAirQuality(qualityDesc);
        } else if (aqi != null) {
            respVO.setAirQuality(aqi.stripTrailingZeros().toPlainString());
        }

        return respVO;
    }

    /**
     * 接口6：当天天气描述
     */
    public String getTodayWeatherDescription() {
        Map<String, Object> weatherContent = getLatestWeatherContent();
        Map<String, Object> latestSurfMap = getLatestStationSurfMap();
        Map<String, Object> realtimeMap = getRealtimeMap(weatherContent);

        // 优先使用站点实时天气，避免预报类字段导致“晴天/阴天”与当前体感不一致
        String weatherText = normalizeWeatherText(firstNonBlank(
                toStringSafe(getMapValueIgnoreCase(latestSurfMap, "WEP_Now")),
                toStringSafe(getMapValueIgnoreCase(latestSurfMap, "weather")),
                toStringSafe(getMapValueIgnoreCase(latestSurfMap, "weatherNow"))
        ));
        if (StrUtil.isBlank(weatherText)) {
            weatherText = normalizeWeatherText(firstNonBlank(
                    toStringSafe(getNestedValue(realtimeMap, "weather")),
                    toStringSafe(getNestedValue(realtimeMap, "skycon")),
                    resolveWeatherDescriptionFromContent(weatherContent)
            ));
        }

        String windText = buildWindDescription(realtimeMap, latestSurfMap);
        if (StrUtil.isNotBlank(weatherText) && StrUtil.isNotBlank(windText)) {
            return weatherText + "\u00B7" + windText;
        }
        return StrUtil.blankToDefault(firstNonBlank(weatherText, windText), "");
    }

    private String resolveWeatherDescriptionFromContent(Map<String, Object> weatherContent) {
        Map<String, Object> realtimeMap = getRealtimeMap(weatherContent);
        return firstNonBlank(
                toStringSafe(getNestedValue(realtimeMap, "description", "chn")),
                toStringSafe(getNestedValue(realtimeMap, "description", "zh")),
                toStringSafe(getNestedValue(realtimeMap, "description")),
                toStringSafe(getNestedValue(realtimeMap, "weatherDescription")),
                toStringSafe(getNestedValue(realtimeMap, "weather_desc")),
                toStringSafe(getNestedValue(realtimeMap, "weather")),
                toStringSafe(getNestedValue(weatherContent, "result", "daily", "description")),
                toStringSafe(getNestedValue(weatherContent, "daily", "description")),
                toStringSafe(getMapValueIgnoreCase(weatherContent, "weatherDescription")),
                toStringSafe(getMapValueIgnoreCase(weatherContent, "weather_desc")),
                toStringSafe(getMapValueIgnoreCase(weatherContent, "description"))
        );
    }

    private String normalizeWeatherText(String text) {
        String normalized = StrUtil.trimToNull(text);
        if (normalized == null) {
            return null;
        }

        String result = normalized;
        result = result.replace("CLEAR_DAY", "\u6674");
        result = result.replace("CLEAR_NIGHT", "\u6674");
        result = result.replace("PARTLY_CLOUDY_DAY", "\u591A\u4E91");
        result = result.replace("PARTLY_CLOUDY_NIGHT", "\u591A\u4E91");
        result = result.replace("CLOUDY", "\u9634");
        result = result.replace("LIGHT_HAZE", "\u8F7B\u5EA6\u96FE\u973E");
        result = result.replace("MODERATE_HAZE", "\u4E2D\u5EA6\u96FE\u973E");
        result = result.replace("HEAVY_HAZE", "\u91CD\u5EA6\u96FE\u973E");
        result = result.replace("LIGHT_RAIN", "\u5C0F\u96E8");
        result = result.replace("MODERATE_RAIN", "\u4E2D\u96E8");
        result = result.replace("HEAVY_RAIN", "\u5927\u96E8");
        result = result.replace("STORM_RAIN", "\u66B4\u96E8");
        result = result.replace("FOG", "\u96FE");
        result = result.replace("LIGHT_SNOW", "\u5C0F\u96EA");
        result = result.replace("MODERATE_SNOW", "\u4E2D\u96EA");
        result = result.replace("HEAVY_SNOW", "\u5927\u96EA");
        result = result.replace("STORM_SNOW", "\u66B4\u96EA");
        result = result.replace("DUST", "\u6D6E\u5C18");
        result = result.replace("SAND", "\u6C99\u5C18");
        result = result.replace("WIND", "\u5927\u98CE");
        return result;
    }

    private String buildWindDescription(Map<String, Object> realtimeMap, Map<String, Object> latestSurfMap) {
        String windDirection = resolveWindDirection(
                getMapValueIgnoreCase(latestSurfMap, "WIN_D_Avg_2mi"),
                getMapValueIgnoreCase(latestSurfMap, "WIN_D_Inst_Max"),
                getMapValueIgnoreCase(latestSurfMap, "WIN_D_S_Max"),
                getNestedValue(realtimeMap, "wind", "direction")
        );
        BigDecimal windSpeed = firstDecimal(
                getMapValueIgnoreCase(latestSurfMap, "WIN_S_Avg_2mi"),
                getMapValueIgnoreCase(latestSurfMap, "WIN_S_Inst_Max"),
                getNestedValue(realtimeMap, "wind", "speed")
        );
        String windLevel = resolveWindLevel(
                firstNonNull(
                        getMapValueIgnoreCase(latestSurfMap, "WIN_Power_Avg_2mi"),
                        getMapValueIgnoreCase(latestSurfMap, "WIN_Power_Inst_Max"),
                        getNestedValue(realtimeMap, "wind", "level"),
                        getNestedValue(realtimeMap, "wind", "scale")
                ),
                windSpeed
        );

        if (StrUtil.isNotBlank(windDirection) && StrUtil.isNotBlank(windLevel)) {
            return windDirection + windLevel;
        }
        return firstNonBlank(windDirection, windLevel);
    }

    private String resolveWindDirection(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            String raw = StrUtil.trimToNull(toStringSafe(value));
            if (raw == null) {
                continue;
            }
            BigDecimal degree = toNormalizedDecimal(raw);
            if (degree != null) {
                String directionByDegree = toDirectionByDegree(degree);
                if (directionByDegree != null) {
                    return directionByDegree + "\u98CE";
                }
            }
            String normalizedText = normalizeWindDirection(raw);
            if (normalizedText != null) {
                return normalizedText;
            }
        }
        return null;
    }

    private String normalizeWindDirection(String text) {
        String normalized = StrUtil.trimToNull(text);
        if (normalized == null) {
            return null;
        }
        if (StrUtil.contains(normalized, "\u98CE")) {
            return normalized;
        }
        if (StrUtil.contains(normalized, "\u7EA7")) {
            return null;
        }
        return normalized + "\u98CE";
    }

    private String toDirectionByDegree(BigDecimal degree) {
        if (degree == null) {
            return null;
        }
        BigDecimal normalized = degree.remainder(BigDecimal.valueOf(360));
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            normalized = normalized.add(BigDecimal.valueOf(360));
        }
        String[] directions = {
                "\u5317", "\u5317\u4E1C\u5317", "\u4E1C\u5317", "\u4E1C\u4E1C\u5317", "\u4E1C", "\u4E1C\u4E1C\u5357", "\u4E1C\u5357", "\u5357\u4E1C\u5357",
                "\u5357", "\u5357\u897F\u5357", "\u897F\u5357", "\u897F\u897F\u5357", "\u897F", "\u897F\u897F\u5317", "\u5317\u897F", "\u5317\u897F\u5317"
        };
        int index = normalized.add(BigDecimal.valueOf(11.25))
                .divide(BigDecimal.valueOf(22.5), 0, RoundingMode.FLOOR)
                .intValue() % directions.length;
        return directions[index];
    }

    private String resolveWindLevel(Object rawLevel, BigDecimal speed) {
        String normalizedLevel = normalizeWindLevel(rawLevel == null ? null : String.valueOf(rawLevel));
        if (normalizedLevel != null) {
            return normalizedLevel;
        }
        if (speed == null) {
            return null;
        }
        return convertWindSpeedToLevel(speed);
    }

    private String normalizeWindLevel(String text) {
        String normalized = StrUtil.trimToNull(text);
        if (normalized == null) {
            return null;
        }
        if (StrUtil.contains(normalized, "\u7EA7") || StrUtil.contains(normalized, "\u98CE")) {
            return normalized;
        }
        BigDecimal numericLevel = toNormalizedDecimal(normalized);
        if (numericLevel == null) {
            return normalized;
        }
        if (numericLevel.compareTo(BigDecimal.ONE) < 0) {
            return "<1\u7EA7";
        }
        return numericLevel.stripTrailingZeros().toPlainString() + "\u7EA7";
    }

    private String convertWindSpeedToLevel(BigDecimal speed) {
        if (speed.compareTo(BigDecimal.ONE) < 0) {
            return "<1\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("1.6")) < 0) {
            return "1\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("3.4")) < 0) {
            return "2\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("5.5")) < 0) {
            return "3\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("8.0")) < 0) {
            return "4\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("10.8")) < 0) {
            return "5\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("13.9")) < 0) {
            return "6\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("17.2")) < 0) {
            return "7\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("20.8")) < 0) {
            return "8\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("24.5")) < 0) {
            return "9\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("28.5")) < 0) {
            return "10\u7EA7";
        }
        if (speed.compareTo(new BigDecimal("32.7")) < 0) {
            return "11\u7EA7";
        }
        return "12\u7EA7";
    }

    private Map<String, Object> getLatestWeatherContent() {
        String today = LocalDate.now().toString();

        YzWeatherDO todayRecord = weatherMapper.selectOne(new LambdaQueryWrapper<YzWeatherDO>()
                .eq(YzWeatherDO::getSynTime, today)
                .orderByDesc(YzWeatherDO::getUpdateTime)
                .orderByDesc(YzWeatherDO::getCreateTime)
                .last("limit 1"));
        if (isWeatherContentFresh(todayRecord, today)) {
            return todayRecord.getWeatherContent();
        }

        // 当天记录不存在或过旧时，优先实时拉取，确保温湿度不是凌晨旧值
        try {
            return weatherService.collectWeatherBySynTime(today);
        } catch (Exception ex) {
            log.warn("[getLatestWeatherContent][当天天气缓存刷新失败，使用库内兜底][today={}, message={}]",
                    today, ex.getMessage());
        }

        // 刷新失败后，优先回退当天旧缓存
        if (todayRecord != null && CollUtil.isNotEmpty(todayRecord.getWeatherContent())) {
            return todayRecord.getWeatherContent();
        }

        YzWeatherDO latestRecord = weatherMapper.selectOne(new LambdaQueryWrapper<YzWeatherDO>()
                .orderByDesc(YzWeatherDO::getUpdateTime)
                .orderByDesc(YzWeatherDO::getSynTime)
                .orderByDesc(YzWeatherDO::getCreateTime)
                .last("limit 1"));
        if (latestRecord != null && CollUtil.isNotEmpty(latestRecord.getWeatherContent())) {
            return latestRecord.getWeatherContent();
        }

        return Collections.emptyMap();
    }

    /**
     * 缓存新鲜度规则：
     * 1. synTime 必须是当天
     * 2. updateTime/createTime 在阈值分钟内
     */
    private boolean isWeatherContentFresh(YzWeatherDO weather, String today) {
        if (weather == null || CollUtil.isEmpty(weather.getWeatherContent())) {
            return false;
        }
        if (!StrUtil.equals(StrUtil.trimToEmpty(weather.getSynTime()), today)) {
            return false;
        }
        LocalDateTime baseTime = weather.getUpdateTime() != null ? weather.getUpdateTime() : weather.getCreateTime();
        if (baseTime == null) {
            return false;
        }
        if (weatherCacheMaxAgeMinutes <= 0) {
            return true;
        }
        return !baseTime.isBefore(LocalDateTime.now().minusMinutes(weatherCacheMaxAgeMinutes));
    }

    private List<BigDecimal> extractHourlyPrecipitationValues(Map<String, Object> weatherContent) {
        Object precipitationObj = firstNonNull(
                getNestedValue(weatherContent, "result", "hourly", "precipitation"),
                getNestedValue(weatherContent, "hourly", "precipitation")
        );
        List<Map<String, Object>> rows = toMapList(precipitationObj);
        if (CollUtil.isEmpty(rows)) {
            return List.of();
        }

        List<BigDecimal> values = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            BigDecimal value = firstDecimal(
                    getMapValueIgnoreCase(row, "value"),
                    getMapValueIgnoreCase(row, "avg"),
                    getMapValueIgnoreCase(row, "max")
            );
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private BigDecimal sumFirstHours(List<BigDecimal> list, int hours) {
        if (CollUtil.isEmpty(list) || hours <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        int end = Math.min(hours, list.size());
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < end; i++) {
            BigDecimal item = list.get(i);
            if (item != null) {
                sum = sum.add(item);
            }
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> getRealtimeMap(Map<String, Object> weatherContent) {
        Map<String, Object> realtime = toMap(firstNonNull(
                getNestedValue(weatherContent, "result", "realtime"),
                getNestedValue(weatherContent, "realtime")
        ));
        return realtime == null ? Collections.emptyMap() : realtime;
    }

    private Map<String, Object> getLatestStationSurfMap() {
        YzWeatherStationSsDO latest = stationSsMapper.selectOne(new LambdaQueryWrapper<YzWeatherStationSsDO>()
                .orderByDesc(YzWeatherStationSsDO::getCreateTime)
                .last("limit 1"));
        if (latest == null || CollUtil.isEmpty(latest.getWeather())) {
            return Collections.emptyMap();
        }
        return getLatestSurfElement(latest.getWeather());
    }

    private Map<String, Object> getLatestSurfElement(Map<String, Object> weatherMap) {
        Object surfHoursObj = getMapValueIgnoreCase(weatherMap, "SurfEle_Hours");
        List<Map<String, Object>> list = toMapList(surfHoursObj);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.get(list.size() - 1);
    }

    private Object getFirstHourlyValue(Map<String, Object> weatherContent, String key) {
        Object arrayObj = firstNonNull(
                getNestedValue(weatherContent, "result", "hourly", key),
                getNestedValue(weatherContent, "hourly", key)
        );
        List<Map<String, Object>> rows = toMapList(arrayObj);
        if (CollUtil.isEmpty(rows)) {
            return null;
        }
        Map<String, Object> firstRow = rows.get(0);
        return firstNonNull(getMapValueIgnoreCase(firstRow, "value"), getMapValueIgnoreCase(firstRow, key));
    }

    private String formatWeatherTimeDisplay(String weatherTime) {
        String normalized = StrUtil.trimToNull(weatherTime);
        if (normalized == null) {
            return null;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(normalized, WEATHER_TIME_SOURCE_FORMATTER);
            return dateTime.format(WEATHER_TIME_DISPLAY_FORMATTER);
        } catch (DateTimeParseException ex) {
            return normalized;
        }
    }

    private LocalDate parseTargetDay(String day) {
        String normalized = StrUtil.trimToNull(day);
        if (normalized == null) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw ServiceExceptionUtil.invalidParamException("day 格式不正确，要求 yyyy-MM-dd，例如 2026-03-23");
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = StrUtil.trimToNull(baseUrl);
        if (normalized == null) {
            return null;
        }
        return StrUtil.removeSuffix(normalized, "/");
    }

    private String toFullImageUrl(String baseUrl, String weatherImage) {
        String path = StrUtil.trimToNull(weatherImage);
        if (path == null) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (baseUrl == null) {
            return path;
        }
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return baseUrl + normalizedPath;
    }

    private File resolveFyImageFile(String weatherImage) {
        String normalizedPath = StrUtil.trimToNull(weatherImage);
        if (normalizedPath == null) {
            return null;
        }
        if (normalizedPath.startsWith("http://") || normalizedPath.startsWith("https://")) {
            return null;
        }

        String storageRoot = StrUtil.blankToDefault(StrUtil.trim(fyStorageRoot), ".");
        String relativePath = StrUtil.removePrefix(normalizedPath, "/");
        if (!StrUtil.startWith(relativePath, "web/weather/")) {
            return null;
        }

        File rootDir = new File(storageRoot);
        File baseDir = new File(rootDir, "web/weather");
        File candidate = new File(rootDir, relativePath);
        try {
            String baseCanonical = baseDir.getCanonicalPath();
            String candidateCanonical = candidate.getCanonicalPath();
            if (!StrUtil.startWith(candidateCanonical, baseCanonical + File.separator)
                    && !StrUtil.equals(candidateCanonical, baseCanonical)) {
                return null;
            }
        } catch (IOException ex) {
            return null;
        }
        return candidate;
    }

    private String detectContentType(String fileName) {
        String lower = StrUtil.blankToDefault(fileName, "").toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map<?, ?> rawMap) {
            Map<String, Object> result = new LinkedHashMap<>();
            rawMap.forEach((k, v) -> result.put(String.valueOf(k), v));
            return result;
        }
        if (obj instanceof String str && StrUtil.isNotBlank(str)) {
            try {
                return JsonUtils.parseObject(str, new TypeReference<Map<String, Object>>() {
                });
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toMapList(Object obj) {
        if (obj == null) {
            return List.of();
        }
        if (obj instanceof List<?> rawList) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object item : rawList) {
                Map<String, Object> map = toMap(item);
                if (map != null) {
                    result.add(map);
                }
            }
            return result;
        }
        if (obj instanceof String str && StrUtil.isNotBlank(str)) {
            try {
                List<Map<String, Object>> list = JsonUtils.parseObject(str, new TypeReference<List<Map<String, Object>>>() {
                });
                return list == null ? List.of() : list;
            } catch (Exception ignored) {
                return List.of();
            }
        }
        return List.of();
    }

    private Object getNestedValue(Map<String, Object> root, String... keys) {
        if (root == null || keys == null || keys.length == 0) {
            return null;
        }
        Object current = root;
        for (String key : keys) {
            Map<String, Object> map = toMap(current);
            if (map == null) {
                return null;
            }
            current = getMapValueIgnoreCase(map, key);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private Object getMapValueIgnoreCase(Map<String, Object> map, String key) {
        if (map == null || StrUtil.isBlank(key)) {
            return null;
        }
        if (map.containsKey(key)) {
            return map.get(key);
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (StrUtil.equalsIgnoreCase(entry.getKey(), key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private BigDecimal firstDecimal(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            BigDecimal decimal = toNormalizedDecimal(value);
            if (decimal != null) {
                return decimal;
            }
        }
        return null;
    }

    private BigDecimal toNormalizedDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            BigDecimal decimal = new BigDecimal(String.valueOf(value));
            if (decimal.abs().compareTo(INVALID_THRESHOLD) >= 0) {
                return null;
            }
            return scale2(decimal);
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal scale2(BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        return decimal.setScale(2, RoundingMode.HALF_UP);
    }

    private Object firstNonNull(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String normalized = StrUtil.trimToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String toStringSafe(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
