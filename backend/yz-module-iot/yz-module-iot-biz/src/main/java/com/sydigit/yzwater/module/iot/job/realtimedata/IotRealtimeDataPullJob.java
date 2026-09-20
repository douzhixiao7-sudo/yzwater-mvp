package com.sydigit.yzwater.module.iot.job.realtimedata;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.util.date.LocalDateTimeUtils;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.framework.quartz.core.handler.JobHandler;
import com.sydigit.yzwater.module.iot.core.enums.IotDeviceMessageMethodEnum;
import com.sydigit.yzwater.module.iot.core.mq.message.IotDeviceMessage;
import com.sydigit.yzwater.module.iot.core.mq.producer.IotDeviceMessageProducer;
import com.sydigit.yzwater.module.iot.core.util.IotDeviceMessageUtils;
import com.sydigit.yzwater.module.iot.service.realtimedata.dto.IotRealtimeDataPointDTO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataMappingDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataSourceDO;
import com.sydigit.yzwater.module.iot.service.realtimedata.IotRealtimeDataMappingService;
import com.sydigit.yzwater.module.iot.service.realtimedata.IotRealtimeDataSourceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 点位实时数据拉取任务
 */
@Component
@Slf4j
public class IotRealtimeDataPullJob implements JobHandler {

    private static final int MAX_FAILURE_DETAIL_COUNT = 20;
    private static final int MAX_EXECUTE_RESULT_LENGTH = 3800;
    private static final int MAX_ERROR_REASON_LENGTH = 180;
    private static final int MAX_REQUEST_BODY_LOG_LENGTH = 1200;
    private static final int HISTORICAL_WINDOW_MINUTES = 5;
    private static final String HISTORICAL_DATA_PATH = "HistoricalData";
    private static final String REQUEST_FIELD_POINT_NAME = "PointName";
    private static final String REQUEST_FIELD_START_DATE = "StartDate";
    private static final String REQUEST_FIELD_END_DATE = "EndDate";
    private static final DateTimeFormatter HISTORICAL_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+00:00'");

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private IotRealtimeDataSourceService sourceService;
    @Resource
    private IotRealtimeDataMappingService mappingService;
    @Resource
    private IotDeviceMessageProducer deviceMessageProducer;

    @Override
    public String execute(String param) throws Exception {
        List<IotRealtimeDataSourceDO> sources = sourceService.getSourceListByEnabledForJob(true);
        if (CollUtil.isEmpty(sources)) {
            return "实时数据采集源为空";
        }

        int totalPoints = 0;
        int totalDevices = 0;
        int totalMessages = 0;
        int totalIgnored = 0;
        int totalSources = 0;
        List<String> failedDetails = new ArrayList<>();
        for (IotRealtimeDataSourceDO source : sources) {
            if (source == null) {
                continue;
            }
            if (StrUtil.isBlank(source.getUrl())) {
                log.warn("[execute][采集源拉取地址为空，已跳过，sourceId={}]", source.getId());
                continue;
            }
            if (StrUtil.isBlank(source.getUsername()) || StrUtil.isBlank(source.getPassword())) {
                log.warn("[execute][采集源认证信息为空，已跳过，sourceId={}]", source.getId());
                continue;
            }

            List<IotRealtimeDataMappingDO> mappings = mappingService
                    .getMappingListBySourceIdAndEnabledForJob(source.getId(), true);
            if (CollUtil.isEmpty(mappings)) {
                log.warn("[execute][点位映射为空，已跳过，sourceId={}]", source.getId());
                continue;
            }

            String responseText = requestRealtimeData(source, mappings, failedDetails);
            if (StrUtil.isBlank(responseText)) {
                continue;
            }

            List<IotRealtimeDataPointDTO> points;
            try {
                points = JsonUtils.parseArray(responseText, IotRealtimeDataPointDTO.class);
            } catch (Exception ex) {
                appendFailureDetail(failedDetails, source.getId(), source.getUrl(),
                        "响应解析失败: " + ExceptionUtil.getRootCauseMessage(ex));
                log.error("[execute][实时数据解析失败，sourceId={}, response={}]", source.getId(), responseText, ex);
                continue;
            }
            if (CollUtil.isEmpty(points)) {
                continue;
            }

            Map<String, List<IotRealtimeDataMappingDO>> mappingMap = buildMappingMap(mappings);
            Map<String, AggregatedPoint> aggregatedMap = new HashMap<>();
            int ignoredCount = 0;
            for (IotRealtimeDataPointDTO point : points) {
                String normalizedPointName = point == null ? null : normalizePointName(point.getPointName());
                if (StrUtil.isBlank(normalizedPointName)) {
                    ignoredCount++;
                    continue;
                }
                List<IotRealtimeDataMappingDO> pointMappings = mappingMap.get(normalizedPointName);
                if (CollUtil.isEmpty(pointMappings)) {
                    ignoredCount++;
                    log.warn("[execute][点位映射不存在，sourceId={}, pointName={}]", source.getId(), point.getPointName());
                    continue;
                }
                LocalDateTime reportTime = parseReportTime(point.getTimestamp());
                for (IotRealtimeDataMappingDO mapping : pointMappings) {
                    Long tenantId = mapping.getTenantId() != null ? mapping.getTenantId() : source.getTenantId();
                    String key = tenantId + ":" + mapping.getDeviceId();
                    AggregatedPoint aggregatedPoint = aggregatedMap.computeIfAbsent(key,
                            item -> new AggregatedPoint(mapping.getDeviceId(), tenantId));
                    aggregatedPoint.getParams().put(mapping.getIdentifier(), point.getValue());
                    aggregatedPoint.mergeReportTime(reportTime);
                }
            }

            int sendCount = 0;
            for (AggregatedPoint aggregatedPoint : aggregatedMap.values()) {
                if (CollUtil.isEmpty(aggregatedPoint.getParams())) {
                    continue;
                }
                IotDeviceMessage message = buildMessage(aggregatedPoint.getDeviceId(), aggregatedPoint.getTenantId(),
                        aggregatedPoint.getParams(), aggregatedPoint.getReportTime());
                deviceMessageProducer.sendDeviceMessage(message);
                sendCount++;
            }
            totalSources++;
            totalPoints += points.size();
            totalDevices += aggregatedMap.size();
            totalMessages += sendCount;
            totalIgnored += ignoredCount;
            log.info("[execute][采集源拉取完成，sourceId={}，原始点位: {}，聚合设备: {}，发送消息: {}，忽略点位: {}]",
                    source.getId(), points.size(), aggregatedMap.size(), sendCount, ignoredCount);
        }
        String summary = StrUtil.format("实时数据拉取完成，采集源: {}，原始点位: {}，聚合设备: {}，发送消息: {}，忽略点位: {}",
                totalSources, totalPoints, totalDevices, totalMessages, totalIgnored);
        return buildExecuteResult(summary, failedDetails);
    }

    private String requestRealtimeData(IotRealtimeDataSourceDO source,
                                       List<IotRealtimeDataMappingDO> mappings,
                                       List<String> failedDetails) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        headers.setBasicAuth(source.getUsername(), source.getPassword(), StandardCharsets.UTF_8);
        String requestBody = buildRequestBody(source, mappings);
        log.info("[requestRealtimeData][发起请求，sourceId={}, url={}, requestBody={}]",
                source.getId(), source.getUrl(), safeLogText(requestBody));
        if (isHistoricalDataUrl(source.getUrl())) {
            return requestHistoricalDataWithFallback(source, requestBody, headers, failedDetails);
        }
        return requestDataOnce(source, requestBody, headers, failedDetails, null);
    }

    private String requestHistoricalDataWithFallback(IotRealtimeDataSourceDO source, String requestBody,
                                                     HttpHeaders headers, List<String> failedDetails) {
        Map<String, Object> body;
        try {
            body = JsonUtils.parseObject(requestBody, Map.class);
        } catch (Exception ex) {
            log.warn("[requestHistoricalDataWithFallback][请求体解析失败，回退到单次请求，sourceId={}]", source.getId(), ex);
            return requestDataOnce(source, requestBody, headers, failedDetails, "single");
        }
        if (body == null || body.isEmpty()) {
            return requestDataOnce(source, requestBody, headers, failedDetails, "single");
        }

        List<String> pointNames = extractPointNames(body.get(REQUEST_FIELD_POINT_NAME));
        if (CollUtil.isEmpty(pointNames)) {
            return requestDataOnce(source, requestBody, headers, failedDetails, "single");
        }

        List<Map<String, Object>> mergedResult = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        for (String pointName : pointNames) {
            Map<String, Object> singleBody = new HashMap<>(body);
            singleBody.put(REQUEST_FIELD_POINT_NAME, List.of(pointName));
            String requestTag = StrUtil.format("point={}", pointName);
            String singleResponse = requestDataOnce(source, JsonUtils.toJsonString(singleBody), headers, failedDetails, requestTag);
            if (StrUtil.isBlank(singleResponse)) {
                failCount++;
                continue;
            }
            successCount++;
            mergeHistoricalResponse(mergedResult, singleResponse, source.getId(), source.getUrl(), failedDetails, requestTag);
        }

        log.info("[requestHistoricalDataWithFallback][请求完成，sourceId={}, totalPoints={}, successPoints={}, failPoints={}, mergedSize={}]",
                source.getId(), pointNames.size(), successCount, failCount, mergedResult.size());
        if (CollUtil.isEmpty(mergedResult)) {
            return null;
        }
        return JsonUtils.toJsonString(mergedResult);
    }

    private void mergeHistoricalResponse(List<Map<String, Object>> mergedResult, String responseText,
                                         Long sourceId, String url, List<String> failedDetails, String requestTag) {
        try {
            List<Map<String, Object>> parsed = JsonUtils.parseObject(responseText,
                    new TypeReference<List<Map<String, Object>>>() {});
            if (CollUtil.isNotEmpty(parsed)) {
                mergedResult.addAll(parsed);
            }
        } catch (Exception ex) {
            appendFailureDetail(failedDetails, sourceId, url,
                    StrUtil.format("历史响应解析失败[{}]: {}", requestTag, ExceptionUtil.getRootCauseMessage(ex)));
            log.error("[mergeHistoricalResponse][响应解析失败，sourceId={}, tag={}, response={}]",
                    sourceId, requestTag, safeLogText(responseText), ex);
        }
    }

    private String requestDataOnce(IotRealtimeDataSourceDO source, String requestBody, HttpHeaders headers,
                                   List<String> failedDetails, String requestTag) {
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response;
        String tag = StrUtil.blankToDefault(requestTag, "single");
        try {
            response = restTemplate.exchange(source.getUrl(), HttpMethod.POST, requestEntity, String.class);
        } catch (Exception ex) {
            appendFailureDetail(failedDetails, source.getId(), source.getUrl(),
                    StrUtil.format("请求失败[{}]: {}", tag, ExceptionUtil.getRootCauseMessage(ex)));
            log.error("[requestRealtimeData][请求失败，sourceId={}, url={}, tag={}, requestBody={}]",
                    source.getId(), source.getUrl(), tag, safeLogText(requestBody), ex);
            return null;
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            appendFailureDetail(failedDetails, source.getId(), source.getUrl(),
                    StrUtil.format("HTTP 状态异常[{}]: {}", tag, response.getStatusCode()));
            log.error("[requestRealtimeData][请求失败，sourceId={}, status={}, url={}, tag={}, requestBody={}]",
                    source.getId(), response.getStatusCode(), source.getUrl(), tag, safeLogText(requestBody));
            return null;
        }
        if (StrUtil.isBlank(response.getBody())) {
            appendFailureDetail(failedDetails, source.getId(), source.getUrl(),
                    StrUtil.format("响应体为空[{}]", tag));
            return null;
        }
        return response.getBody();
    }

    private List<String> extractPointNames(Object pointNameObj) {
        if (pointNameObj == null) {
            return new ArrayList<>();
        }
        if (pointNameObj instanceof Collection<?> collection) {
            List<String> result = new ArrayList<>();
            for (Object item : collection) {
                if (item == null) {
                    continue;
                }
                String pointName = StrUtil.trimToEmpty(String.valueOf(item));
                if (StrUtil.isNotBlank(pointName)) {
                    result.add(pointName);
                }
            }
            return result;
        }
        String singlePoint = StrUtil.trimToEmpty(String.valueOf(pointNameObj));
        if (StrUtil.isBlank(singlePoint)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(singlePoint));
    }

    private String safeLogText(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        if (text.length() <= MAX_REQUEST_BODY_LOG_LENGTH) {
            return text;
        }
        return StrUtil.sub(text, 0, MAX_REQUEST_BODY_LOG_LENGTH) + "...";
    }

    private void appendFailureDetail(List<String> failedDetails, Long sourceId, String url, String errorReason) {
        if (failedDetails == null) {
            return;
        }
        String safeReason = StrUtil.blankToDefault(errorReason, "未知错误");
        safeReason = StrUtil.replace(safeReason, "\r", " ");
        safeReason = StrUtil.replace(safeReason, "\n", " ");
        if (safeReason.length() > MAX_ERROR_REASON_LENGTH) {
            safeReason = StrUtil.sub(safeReason, 0, MAX_ERROR_REASON_LENGTH) + "...";
        }
        failedDetails.add(StrUtil.format("sourceId={},url={},error={}",
                sourceId, StrUtil.blankToDefault(url, "-"), safeReason));
    }

    private String buildExecuteResult(String summary, List<String> failedDetails) {
        if (CollUtil.isEmpty(failedDetails)) {
            return summary;
        }
        List<String> displayDetails = failedDetails.stream()
                .limit(MAX_FAILURE_DETAIL_COUNT)
                .toList();
        String detailText = CollUtil.join(displayDetails, " | ");
        if (failedDetails.size() > displayDetails.size()) {
            detailText = detailText + StrUtil.format(" | 其余 {} 条失败明细已省略",
                    failedDetails.size() - displayDetails.size());
        }
        String result = summary + "；失败明细: " + detailText;
        if (result.length() > MAX_EXECUTE_RESULT_LENGTH) {
            return StrUtil.sub(result, 0, MAX_EXECUTE_RESULT_LENGTH - 3) + "...";
        }
        return result;
    }

    private Map<String, List<IotRealtimeDataMappingDO>> buildMappingMap(List<IotRealtimeDataMappingDO> mappings) {
        Map<String, List<IotRealtimeDataMappingDO>> mappingMap = new HashMap<>();
        for (IotRealtimeDataMappingDO mapping : mappings) {
            String pointName = mapping == null ? null : normalizePointName(mapping.getPointName());
            if (mapping == null || StrUtil.isBlank(pointName)
                    || mapping.getDeviceId() == null || StrUtil.isBlank(mapping.getIdentifier())) {
                log.warn("[buildMappingMap][点位映射配置不完整，已跳过，pointName={}]", mapping == null ? null : mapping.getPointName());
                continue;
            }
            mappingMap.computeIfAbsent(pointName, key -> new ArrayList<>()).add(mapping);
        }
        return mappingMap;
    }

    private String buildRequestBody(IotRealtimeDataSourceDO source, List<IotRealtimeDataMappingDO> mappings) {
        Map<String, Object> body = parseSourceRequestBody(source);
        fillPointNamesIfMissing(body, mappings);
        if (isHistoricalDataUrl(source.getUrl())) {
            fillHistoricalWindowIfMissing(body, source.getId());
        }
        if (body.isEmpty()) {
            return "{}";
        }
        return JsonUtils.toJsonString(body);
    }

    private Map<String, Object> parseSourceRequestBody(IotRealtimeDataSourceDO source) {
        String requestBody = source == null ? null : StrUtil.trimToEmpty(source.getRequestBody());
        if (StrUtil.isBlank(requestBody)) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> parsed = JsonUtils.parseObject(requestBody, Map.class);
            return parsed == null ? new HashMap<>() : new HashMap<>(parsed);
        } catch (Exception ex) {
            log.warn("[buildRequestBody][requestBody 不是合法 JSON，已降级为自动拼装，sourceId={}, requestBody={}]",
                    source == null ? null : source.getId(), requestBody, ex);
            return new HashMap<>();
        }
    }

    private void fillPointNamesIfMissing(Map<String, Object> body, List<IotRealtimeDataMappingDO> mappings) {
        if (body == null || hasValidPointName(body.get(REQUEST_FIELD_POINT_NAME))) {
            return;
        }
        if (CollUtil.isEmpty(mappings)) {
            return;
        }
        Set<String> pointNames = new LinkedHashSet<>();
        for (IotRealtimeDataMappingDO mapping : mappings) {
            String pointName = mapping == null ? null : normalizePointName(mapping.getPointName());
            if (StrUtil.isBlank(pointName)) {
                continue;
            }
            pointNames.add(pointName);
        }
        if (CollUtil.isNotEmpty(pointNames)) {
            body.put(REQUEST_FIELD_POINT_NAME, new ArrayList<>(pointNames));
        }
    }

    private String normalizePointName(String pointName) {
        return StrUtil.trimToNull(pointName);
    }

    private boolean hasValidPointName(Object pointNameValue) {
        if (pointNameValue == null) {
            return false;
        }
        if (pointNameValue instanceof Collection) {
            return CollUtil.isNotEmpty((Collection<?>) pointNameValue);
        }
        if (pointNameValue instanceof Object[]) {
            return ((Object[]) pointNameValue).length > 0;
        }
        if (pointNameValue instanceof String) {
            return StrUtil.isNotBlank((String) pointNameValue);
        }
        return true;
    }

    private boolean isHistoricalDataUrl(String url) {
        return StrUtil.isNotBlank(url) && StrUtil.containsIgnoreCase(url, HISTORICAL_DATA_PATH);
    }

    private void fillHistoricalWindowIfMissing(Map<String, Object> body, Long sourceId) {
        if (body == null) {
            return;
        }
        boolean startMissing = isEmptyRequestField(body.get(REQUEST_FIELD_START_DATE));
        boolean endMissing = isEmptyRequestField(body.get(REQUEST_FIELD_END_DATE));
        if (!startMissing && !endMissing) {
            return;
        }
        OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
        OffsetDateTime start = end.minusMinutes(HISTORICAL_WINDOW_MINUTES);
        if (startMissing) {
            body.put(REQUEST_FIELD_START_DATE, start.format(HISTORICAL_TIME_FORMATTER));
        }
        if (endMissing) {
            body.put(REQUEST_FIELD_END_DATE, end.format(HISTORICAL_TIME_FORMATTER));
        }
        log.warn("[buildRequestBody][检测到 HistoricalData 请求缺少时间范围，已自动补齐，sourceId={}, startDate={}, endDate={}]",
                sourceId, body.get(REQUEST_FIELD_START_DATE), body.get(REQUEST_FIELD_END_DATE));
    }

    private boolean isEmptyRequestField(Object fieldValue) {
        if (fieldValue == null) {
            return true;
        }
        if (fieldValue instanceof String) {
            return StrUtil.isBlank((String) fieldValue);
        }
        return false;
    }

    private IotDeviceMessage buildMessage(Long deviceId, Long tenantId, Map<String, Object> params,
                                          LocalDateTime reportTime) {
        String messageId = IotDeviceMessageUtils.generateMessageId();
        return new IotDeviceMessage()
                .setId(messageId)
                .setRequestId(messageId)
                .setDeviceId(deviceId)
                .setTenantId(tenantId)
                .setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod())
                .setParams(params)
                .setReportTime(reportTime);
    }

    private LocalDateTime parseReportTime(String timestamp) {
        if (StrUtil.isBlank(timestamp)) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(timestamp)
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception ex) {
            return LocalDateTimeUtils.parse(timestamp);
        }
    }

    private static class AggregatedPoint {
        private final Long deviceId;
        private final Long tenantId;
        private final Map<String, Object> params = new HashMap<>();
        private LocalDateTime reportTime;

        private AggregatedPoint(Long deviceId, Long tenantId) {
            this.deviceId = deviceId;
            this.tenantId = tenantId;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public Long getTenantId() {
            return tenantId;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public LocalDateTime getReportTime() {
            return reportTime;
        }

        public void mergeReportTime(LocalDateTime candidate) {
            if (candidate == null) {
                return;
            }
            if (reportTime == null || candidate.isAfter(reportTime)) {
                reportTime = candidate;
            }
        }
    }
}
