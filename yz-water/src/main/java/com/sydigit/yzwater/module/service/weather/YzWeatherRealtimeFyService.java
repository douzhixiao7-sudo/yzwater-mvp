package com.sydigit.yzwater.module.service.weather;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.module.dal.dataobject.weather.YzWeatherRealtimeFyDO;
import com.sydigit.yzwater.module.dal.mysql.weather.YzWeatherRealtimeFyMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FY 实时天气图层采集服务
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class YzWeatherRealtimeFyService {

    private static final String TYPE_FENGYUN2 = "fengyun2";
    private static final String TYPE_FENGYUN4 = "fengyun4";
    private static final String TYPE_HIMAWARI8 = "himawari8";
    private static final Set<String> SUPPORTED_TYPES = Set.of(TYPE_FENGYUN2, TYPE_FENGYUN4, TYPE_HIMAWARI8);

    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter DATE_FOLDER_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    // himawari8 实测存在发布时间延迟，先回退 30 分钟，再按 10 分钟步进回看 3 小时
    private static final int HIMAWARI8_DELAY_MINUTES = 30;
    private static final int HIMAWARI8_STEP_MINUTES = 10;
    private static final int HIMAWARI8_LOOKBACK_STEPS = 18;

    private final YzWeatherRealtimeFyMapper realtimeFyMapper;

    @Value("${yz.weather.fy.enabled:true}")
    private boolean fyEnabled;

    @Value("${yz.weather.fy.storage-root:.}")
    private String fyStorageRoot;

    @Value("${yz.weather.fy.timeout-millis:10000}")
    private int timeoutMillis;

    @Value("${yz.weather.fy.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36}")
    private String userAgent;

    @Value("${yz.weather.fy.referer:https://www.qweather.com/}")
    private String referer;

    @Value("${yz.weather.fy.fengyun2-url-template:https://imagery.qweather.com/imagery/satellite/china_fy2/thumbnail/{fileName}}")
    private String fengyun2UrlTemplate;

    @Value("${yz.weather.fy.fengyun4-url-template:http://image.nmc.cn/product/{ymd}/WXBL/medium/SEVP_NSMC_WXBL_FY4B_ETCC_ACHN_LNO_PY_{time}00000.JPG?v={timestamp}}")
    private String fengyun4UrlTemplate;

    @Value("${yz.weather.fy.himawari8-url-template:https://imagery.qweather.com/imagery/satellite/japan/thumbnail/{fileName}}")
    private String himawari8UrlTemplate;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> collectRealtimeFy(List<String> types) {
        if (!fyEnabled) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.ERROR_CONFIGURATION.getCode(),
                    "FY 图层采集已禁用，请检查配置 yz.weather.fy.enabled");
        }

        List<String> targetTypes = resolveTargetTypes(types);
        if (CollUtil.isEmpty(targetTypes)) {
            throw ServiceExceptionUtil.invalidParamException("types 为空或不在支持范围：{}", SUPPORTED_TYPES);
        }

        int success = 0;
        int skipped = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        for (String type : targetTypes) {
            try {
                CollectResult result = collectSingleType(type);
                if (result.isSkipped()) {
                    skipped++;
                    details.add(buildDetail(type, "skipped", result.getFileName(), result.getRecordId(), result.getSkipReason()));
                } else {
                    success++;
                    details.add(buildDetail(type, "success", result.getFileName(), result.getRecordId(), null));
                }
            } catch (Exception ex) {
                log.error("[collectRealtimeFy][FY 图层采集失败][type={}]", type, ex);
                details.add(buildDetail(type, "failed", null, null, ex.getMessage()));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", targetTypes.size());
        result.put("success", success);
        result.put("skipped", skipped);
        result.put("failed", targetTypes.size() - success - skipped);
        result.put("details", details);
        return result;
    }

    public List<YzWeatherRealtimeFyDO> getLatestList(String type, Integer limit) {
        int finalLimit = limit == null || limit <= 0 ? 10 : Math.min(limit, 100);
        LambdaQueryWrapper<YzWeatherRealtimeFyDO> queryWrapper = new LambdaQueryWrapper<YzWeatherRealtimeFyDO>()
                .orderByDesc(YzWeatherRealtimeFyDO::getWeatherTime)
                .orderByDesc(YzWeatherRealtimeFyDO::getId)
                .last("limit " + finalLimit);
        if (StrUtil.isNotBlank(type)) {
            queryWrapper.eq(YzWeatherRealtimeFyDO::getType, type.trim());
        }
        return realtimeFyMapper.selectList(queryWrapper);
    }

    private List<String> resolveTargetTypes(List<String> types) {
        if (CollUtil.isEmpty(types)) {
            return new ArrayList<>(SUPPORTED_TYPES);
        }
        LinkedHashSet<String> dedupTypes = new LinkedHashSet<>();
        for (String type : types) {
            String normalizedType = StrUtil.trimToNull(type);
            if (normalizedType == null) {
                continue;
            }
            String lowerType = normalizedType.toLowerCase();
            if (SUPPORTED_TYPES.contains(lowerType)) {
                dedupTypes.add(lowerType);
            }
        }
        return new ArrayList<>(dedupTypes);
    }

    private CollectResult collectSingleType(String type) {
        if (TYPE_HIMAWARI8.equals(type)) {
            return collectHimawari8WithFallback(type);
        }
        ImageCollectPlan plan = buildCollectPlan(type);
        return collectByPlan(type, plan);
    }

    private CollectResult collectHimawari8WithFallback(String type) {
        OffsetDateTime baseTime = roundForHimawari8(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(HIMAWARI8_DELAY_MINUTES));
        int notFoundCount = 0;
        for (int i = 0; i < HIMAWARI8_LOOKBACK_STEPS; i++) {
            OffsetDateTime imageTime = baseTime.minusMinutes((long) i * HIMAWARI8_STEP_MINUTES);
            ImageCollectPlan plan = buildHimawari8Plan(type, imageTime);
            try {
                return collectByPlan(type, plan);
            } catch (FyDownloadException ex) {
                if (Integer.valueOf(404).equals(ex.getStatus())) {
                    notFoundCount++;
                    continue;
                }
                throw convertDownloadException(type, plan.imageUrl, ex);
            }
        }
        int lookbackMinutes = HIMAWARI8_LOOKBACK_STEPS * HIMAWARI8_STEP_MINUTES;
        return CollectResult.skipped(null, null,
                StrUtil.format("源站暂无可用影像（最近 {} 分钟连续 {} 次返回 404）", lookbackMinutes, notFoundCount));
    }

    private CollectResult collectByPlan(String type, ImageCollectPlan plan) {
        YzWeatherRealtimeFyDO exists = realtimeFyMapper.selectFirstOne(
                YzWeatherRealtimeFyDO::getType, type,
                YzWeatherRealtimeFyDO::getFileName, plan.fileName);
        if (exists != null) {
            return CollectResult.skipped(plan.fileName, exists.getId(), "文件已存在");
        }

        byte[] imageBytes;
        try {
            imageBytes = downloadImageBytes(type, plan.imageUrl);
        } catch (FyDownloadException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "FY 图层下载异常，type={}, url={}, message={}", type, plan.imageUrl, ex.getMessage());
        }

        String relativePath = "/" + StrUtil.removePrefix(plan.relativeFolder, "/") + "/" + plan.fileName;
        File targetFile = buildStorageFile(relativePath);
        FileUtil.mkParentDirs(targetFile);
        FileUtil.writeBytes(imageBytes, targetFile);

        YzWeatherRealtimeFyDO insert = new YzWeatherRealtimeFyDO();
        insert.setId(IdUtil.fastSimpleUUID());
        insert.setType(type);
        insert.setWeatherImage(relativePath);
        insert.setWeatherTime(plan.weatherTime);
        insert.setFileName(plan.fileName);
        realtimeFyMapper.insert(insert);
        return CollectResult.success(plan.fileName, insert.getId());
    }

    private ImageCollectPlan buildCollectPlan(String type) {
        OffsetDateTime utcNow = OffsetDateTime.now(ZoneOffset.UTC);
        if (TYPE_FENGYUN2.equals(type)) {
            OffsetDateTime imageTime = roundForFengyun2(utcNow.minusHours(1));
            String fileName = imageTime.format(MINUTE_FORMATTER) + ".jpg";
            String ymd = imageTime.format(DATE_FOLDER_FORMATTER);
            String imageUrl = buildUrl(fengyun2UrlTemplate, ymd, imageTime.format(MINUTE_FORMATTER), fileName);
            return new ImageCollectPlan(fileName, imageTime.format(MINUTE_FORMATTER),
                    "web/weather/" + type + "/" + ymd, imageUrl);
        }
        if (TYPE_FENGYUN4.equals(type)) {
            OffsetDateTime imageTime = roundForFengyun4(utcNow.minusHours(1));
            String fileName = imageTime.format(MINUTE_FORMATTER) + ".jpg";
            String ymd = imageTime.format(DATE_FOLDER_FORMATTER);
            String imageUrl = buildUrl(fengyun4UrlTemplate, ymd, imageTime.format(MINUTE_FORMATTER), fileName);
            return new ImageCollectPlan(fileName, imageTime.format(MINUTE_FORMATTER),
                    "web/weather/" + type + "/" + ymd, imageUrl);
        }
        return buildHimawari8Plan(type, roundForHimawari8(utcNow));
    }

    private ImageCollectPlan buildHimawari8Plan(String type, OffsetDateTime imageTime) {
        String fileName = imageTime.format(MINUTE_FORMATTER) + ".png";
        String ymd = imageTime.format(DATE_FOLDER_FORMATTER);
        String imageUrl = buildUrl(himawari8UrlTemplate, ymd, imageTime.format(MINUTE_FORMATTER), fileName);
        return new ImageCollectPlan(fileName, imageTime.format(MINUTE_FORMATTER),
                "web/weather/" + type + "/" + ymd, imageUrl);
    }

    private String buildUrl(String template, String ymd, String time, String fileName) {
        String url = template;
        url = StrUtil.replace(url, "{ymd}", ymd);
        url = StrUtil.replace(url, "{time}", time);
        url = StrUtil.replace(url, "{fileName}", fileName);
        url = StrUtil.replace(url, "{timestamp}", String.valueOf(Instant.now().toEpochMilli()));
        return url;
    }

    private File buildStorageFile(String relativePath) {
        String root = StrUtil.blankToDefault(StrUtil.trim(fyStorageRoot), "./web/weather");
        String normalizedRelativePath = StrUtil.removePrefix(relativePath, "/");
        if (root.endsWith("/") || root.endsWith("\\")) {
            return new File(root + normalizedRelativePath);
        }
        return new File(root + File.separator + normalizedRelativePath);
    }

    private byte[] downloadImageBytes(String type, String imageUrl) {
        try {
            return doDownloadImageBytes(type, imageUrl);
        } catch (FyDownloadException ex) {
            if (needFallbackToQweatherCom(ex, imageUrl)) {
                String fallbackUrl = StrUtil.replace(imageUrl, "imagery.qweather.net", "imagery.qweather.com");
                try {
                    log.warn("[downloadImageBytes][主域名不可用，尝试回退域名][type={}, from={}, to={}]", type, imageUrl, fallbackUrl);
                    return doDownloadImageBytes(type, fallbackUrl);
                } catch (FyDownloadException retryEx) {
                    throw new FyDownloadException(retryEx.getStatus(), fallbackUrl,
                            StrUtil.format("FY 图层下载异常，type={}, url={}, fallbackUrl={}, message={}",
                                    type, imageUrl, fallbackUrl, retryEx.getMessage()), retryEx);
                }
            }
            throw ex;
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "FY 图层下载异常，type={}, url={}, message={}", type, imageUrl, ex.getMessage());
        }
    }

    private byte[] doDownloadImageBytes(String type, String imageUrl) {
        if (TYPE_FENGYUN4.equals(type)) {
            return HttpUtil.downloadBytes(imageUrl);
        }
        HttpResponse response = HttpRequest.get(imageUrl)
                .timeout(timeoutMillis)
                .header("Referer", referer)
                .header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                .header("User-Agent", userAgent)
                .execute();
        if (!response.isOk()) {
            throw new FyDownloadException(response.getStatus(), imageUrl,
                    StrUtil.format("FY 图层下载失败，status={}, type={}, url={}", response.getStatus(), type, imageUrl));
        }
        return response.bodyBytes();
    }

    private RuntimeException convertDownloadException(String type, String imageUrl, FyDownloadException ex) {
        return ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                "FY 图层下载异常，type={}, url={}, status={}, message={}",
                type, imageUrl, StrUtil.blankToDefault(String.valueOf(ex.getStatus()), "未知"), ex.getMessage());
    }

    private boolean needFallbackToQweatherCom(Exception ex, String imageUrl) {
        if (StrUtil.isBlank(imageUrl) || !imageUrl.contains("imagery.qweather.net")) {
            return false;
        }
        Throwable current = ex;
        while (current != null) {
            if (current instanceof UnknownHostException) {
                return true;
            }
            current = current.getCause();
        }
        String message = ex.getMessage();
        return StrUtil.isNotBlank(message) && message.contains("UnknownHostException")
                && message.contains("imagery.qweather.net");
    }

    private OffsetDateTime roundForFengyun2(OffsetDateTime dateTime) {
        int minute = dateTime.getMinute();
        int roundedMinute = minute <= 30 ? 0 : 30;
        return dateTime.withMinute(roundedMinute).withSecond(0).withNano(0);
    }

    private OffsetDateTime roundForFengyun4(OffsetDateTime dateTime) {
        int minute = dateTime.getMinute();
        int roundedMinute;
        if (minute <= 15) {
            roundedMinute = 0;
        } else if (minute <= 30) {
            roundedMinute = 15;
        } else if (minute <= 45) {
            roundedMinute = 30;
        } else {
            roundedMinute = 45;
        }
        return dateTime.withMinute(roundedMinute).withSecond(0).withNano(0);
    }

    private OffsetDateTime roundForHimawari8(OffsetDateTime dateTime) {
        int minute = dateTime.getMinute();
        int roundedMinute = (minute / 10) * 10;
        return dateTime.withMinute(roundedMinute).withSecond(0).withNano(0);
    }

    private Map<String, Object> buildDetail(String type, String status, String fileName, String recordId, String error) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("type", type);
        detail.put("status", status);
        detail.put("fileName", fileName);
        detail.put("recordId", recordId);
        detail.put("error", error);
        return detail;
    }

    private static class ImageCollectPlan {
        private final String fileName;
        private final String weatherTime;
        private final String relativeFolder;
        private final String imageUrl;

        private ImageCollectPlan(String fileName, String weatherTime, String relativeFolder, String imageUrl) {
            this.fileName = fileName;
            this.weatherTime = weatherTime;
            this.relativeFolder = relativeFolder;
            this.imageUrl = imageUrl;
        }
    }

    private static class CollectResult {
        private final boolean skipped;
        private final String fileName;
        private final String recordId;
        private final String skipReason;

        private CollectResult(boolean skipped, String fileName, String recordId, String skipReason) {
            this.skipped = skipped;
            this.fileName = fileName;
            this.recordId = recordId;
            this.skipReason = skipReason;
        }

        public static CollectResult success(String fileName, String recordId) {
            return new CollectResult(false, fileName, recordId, null);
        }

        public static CollectResult skipped(String fileName, String recordId, String skipReason) {
            return new CollectResult(true, fileName, recordId, skipReason);
        }

        public boolean isSkipped() {
            return skipped;
        }

        public String getFileName() {
            return fileName;
        }

        public String getRecordId() {
            return recordId;
        }

        public String getSkipReason() {
            return skipReason;
        }
    }

    @Getter
    private static class FyDownloadException extends RuntimeException {
        private final Integer status;
        private final String url;

        private FyDownloadException(Integer status, String url, String message) {
            super(message);
            this.status = status;
            this.url = url;
        }

        private FyDownloadException(Integer status, String url, String message, Throwable cause) {
            super(message, cause);
            this.status = status;
            this.url = url;
        }
    }

}
