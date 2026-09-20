package com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.handler.upstream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sydigit.yzwater.framework.common.util.date.LocalDateTimeUtils;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttPointRespDTO;
import com.sydigit.yzwater.module.iot.core.enums.IotDeviceMessageMethodEnum;
import com.sydigit.yzwater.module.iot.core.mq.message.IotDeviceMessage;
import com.sydigit.yzwater.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT MQTT Source 上行数据处理器
 */
@Slf4j
public class IotMqttSourceUpstreamHandler {

    private final IotDeviceMessageService messageService;
    private final String serverId;

    public IotMqttSourceUpstreamHandler(IotDeviceMessageService messageService, String serverId) {
        this.messageService = messageService;
        this.serverId = serverId;
    }

    public void handleMessage(List<IotMqttDeviceConfigRespDTO> configList, String topic, byte[] payload) {
        if (CollUtil.isEmpty(configList) || ArrayUtil.isEmpty(payload) || StrUtil.isBlank(topic)) {
            return;
        }
        try {
            JsonNode payloadNode = JsonUtils.parseTree(payload);
            if (payloadNode == null || !payloadNode.isObject()) {
                log.warn("[handleMessage][MQTT Source 报文不是 JSON Object，忽略, topic={}]", topic);
                return;
            }
            Map<Long, AggregatedMessage> messageMap = new LinkedHashMap<>();
            for (IotMqttDeviceConfigRespDTO config : configList) {
                if (!StrUtil.equals(topic, config.getTopic()) || CollUtil.isEmpty(config.getPoints())) {
                    continue;
                }
                AggregatedMessage aggregated = messageMap.computeIfAbsent(config.getDeviceId(),
                        key -> new AggregatedMessage(config.getProductKey(), config.getDeviceName()));
                aggregated.mergeReportTime(parseReportTime(payloadNode, config));
                for (IotMqttPointRespDTO point : config.getPoints()) {
                    if (StrUtil.isBlank(point.getPayloadKey()) || StrUtil.isBlank(point.getIdentifier())) {
                        continue;
                    }
                    JsonNode valueNode = payloadNode.get(point.getPayloadKey());
                    if (valueNode == null || valueNode.isNull()) {
                        continue;
                    }
                    aggregated.getParams().put(point.getIdentifier(), convertNodeValue(valueNode));
                }
            }
            for (AggregatedMessage aggregated : messageMap.values()) {
                if (aggregated.getParams().isEmpty()) {
                    continue;
                }
                IotDeviceMessage message = IotDeviceMessage.requestOf(
                        IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), aggregated.getParams());
                if (aggregated.getReportTime() != null) {
                    message.setReportTime(aggregated.getReportTime());
                }
                messageService.sendDeviceMessage(message, aggregated.getProductKey(),
                        aggregated.getDeviceName(), serverId);
            }
        } catch (Exception e) {
            log.error("[handleMessage][处理 MQTT Source 消息失败, topic={}]", topic, e);
        }
    }

    private Object convertNodeValue(JsonNode valueNode) throws JsonProcessingException {
        return JsonUtils.getObjectMapper().treeToValue(valueNode, Object.class);
    }

    private LocalDateTime parseReportTime(JsonNode payloadNode, IotMqttDeviceConfigRespDTO config) {
        if (StrUtil.isBlank(config.getReportTimeKey())) {
            return LocalDateTime.now();
        }
        JsonNode reportTimeNode = payloadNode.get(config.getReportTimeKey());
        if (reportTimeNode == null || reportTimeNode.isNull()) {
            return LocalDateTime.now();
        }
        try {
            if (reportTimeNode.isNumber()) {
                long timestamp = reportTimeNode.asLong();
                if (String.valueOf(Math.abs(timestamp)).length() <= 10) {
                    timestamp *= 1000;
                }
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            }
            String text = reportTimeNode.asText();
            if (StrUtil.isBlank(text)) {
                return LocalDateTime.now();
            }
            if (StrUtil.isNotBlank(config.getReportTimeFormat())) {
                return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(config.getReportTimeFormat()));
            }
            try {
                return OffsetDateTime.parse(text).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            } catch (Exception ignore) {
                return LocalDateTimeUtils.parse(text);
            }
        } catch (Exception e) {
            log.warn("[parseReportTime][解析 MQTT Source 报文时间失败, topic={}, deviceId={}, reportTimeKey={}]",
                    config.getTopic(), config.getDeviceId(), config.getReportTimeKey(), e);
            return LocalDateTime.now();
        }
    }

    @Getter
    private static class AggregatedMessage {

        private final String productKey;
        private final String deviceName;
        private final Map<String, Object> params = new LinkedHashMap<>();
        private LocalDateTime reportTime;

        private AggregatedMessage(String productKey, String deviceName) {
            this.productKey = productKey;
            this.deviceName = deviceName;
        }

        private void mergeReportTime(LocalDateTime candidate) {
            if (candidate == null) {
                return;
            }
            if (reportTime == null || ObjUtil.compare(candidate, reportTime) > 0) {
                reportTime = candidate;
            }
        }

    }

}
