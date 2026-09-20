package com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.handler.upstream.IotMqttSourceUpstreamHandler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT MQTT Source 连接管理器
 */
@RequiredArgsConstructor
@Slf4j
public class IotMqttSourceConnectionManager {

    private final Vertx vertx;
    private final IotMqttSourceConfigCacheService configCacheService;
    private final IotMqttSourceUpstreamHandler upstreamHandler;
    private final Integer maxMessageSize;

    private final Map<Long, SourceClientContext> contextMap = new ConcurrentHashMap<>();

    public synchronized void syncConnections(Map<Long, List<IotMqttDeviceConfigRespDTO>> sourceConfigMap) {
        Set<Long> currentSourceIds = sourceConfigMap.keySet();
        for (Map.Entry<Long, List<IotMqttDeviceConfigRespDTO>> entry : sourceConfigMap.entrySet()) {
            Long sourceId = entry.getKey();
            List<IotMqttDeviceConfigRespDTO> configList = entry.getValue();
            if (CollUtil.isEmpty(configList)) {
                stopConnection(sourceId);
                continue;
            }
            SourceSnapshot snapshot = SourceSnapshot.of(configList);
            SourceClientContext currentContext = contextMap.get(sourceId);
            if (currentContext != null && Objects.equals(currentContext.getSnapshot(), snapshot)
                    && currentContext.getClient() != null && currentContext.getClient().isConnected()) {
                continue;
            }
            restartConnection(sourceId, snapshot, currentContext);
        }

        Set<Long> removedSourceIds = new LinkedHashSet<>(contextMap.keySet());
        removedSourceIds.removeAll(currentSourceIds);
        removedSourceIds.forEach(this::stopConnection);
    }

    public synchronized void stopConnection(Long sourceId) {
        SourceClientContext context = contextMap.remove(sourceId);
        if (context == null) {
            return;
        }
        cancelReconnectTimer(context);
        MqttClient client = context.getClient();
        context.setClient(null);
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                for (String topic : context.getSnapshot().getTopics()) {
                    try {
                        client.unsubscribe(topic);
                    } catch (Exception e) {
                        log.warn("[stopConnection][取消订阅 MQTT Source 主题失败, sourceId={}, topic={}]",
                                sourceId, topic, e);
                    }
                }
                disconnectClient(client, sourceId);
            }
        } catch (Exception e) {
            log.warn("[stopConnection][关闭 MQTT Source 连接失败, sourceId={}]", sourceId, e);
        }
    }

    public synchronized void stopAll() {
        List<Long> sourceIds = new ArrayList<>(contextMap.keySet());
        sourceIds.forEach(this::stopConnection);
    }

    private void restartConnection(Long sourceId, SourceSnapshot snapshot, SourceClientContext currentContext) {
        if (currentContext != null) {
            stopConnection(sourceId);
        }
        SourceClientContext context = new SourceClientContext();
        context.setSourceId(sourceId);
        context.setSnapshot(snapshot);
        contextMap.put(sourceId, context);
        createAndConnectClient(context);
    }

    private void createAndConnectClient(SourceClientContext context) {
        try {
            MqttClient client = MqttClient.create(vertx, buildClientOptions(context.getSnapshot()));
            context.setClient(client);
            setupHandlers(context, client);
            connectClient(context, client);
        } catch (Exception e) {
            log.error("[createAndConnectClient][建立 MQTT Source 连接失败, sourceId={}]",
                    context.getSourceId(), e);
            stopClientOnly(context);
            scheduleReconnect(context);
        }
    }

    private MqttClientOptions buildClientOptions(SourceSnapshot snapshot) {
        return (MqttClientOptions) new MqttClientOptions()
                .setClientId(resolveClientId(snapshot))
                .setUsername(snapshot.getUsername())
                .setPassword(snapshot.getPassword())
                .setSsl(Boolean.TRUE.equals(snapshot.getSslEnabled()))
                .setCleanSession(Boolean.TRUE.equals(snapshot.getCleanSession()))
                .setMaxMessageSize(ObjectUtil.defaultIfNull(maxMessageSize, 65536))
                .setKeepAliveInterval(ObjectUtil.defaultIfNull(snapshot.getKeepAliveIntervalSeconds(), 60))
                .setConnectTimeout(ObjectUtil.defaultIfNull(snapshot.getConnectTimeoutSeconds(), 10) * 1000);
    }

    private String resolveClientId(SourceSnapshot snapshot) {
        if (StrUtil.isNotBlank(snapshot.getClientId())) {
            return snapshot.getClientId();
        }
        return "mqtt-source-" + snapshot.getSourceId();
    }

    private void connectClient(SourceClientContext context, MqttClient client) {
        SourceSnapshot snapshot = context.getSnapshot();
        client.connect(snapshot.getBrokerPort(), snapshot.getBrokerHost(), result -> {
            if (!isActiveClient(context, client)) {
                disconnectClient(client, context.getSourceId());
                return;
            }
            if (!result.succeeded()) {
                log.error("[connectClient][连接 MQTT Source Broker 失败, sourceId={}, host={}, port={}]",
                        context.getSourceId(), snapshot.getBrokerHost(), snapshot.getBrokerPort(), result.cause());
                stopClientOnly(context);
                scheduleReconnect(context);
                return;
            }
            log.info("[createAndConnectClient][MQTT Source 连接成功, sourceId={}, host={}, port={}, topics={}]",
                    context.getSourceId(), snapshot.getBrokerHost(), snapshot.getBrokerPort(), snapshot.getTopics());
            subscribeTopics(context, client);
        });
    }

    private void subscribeTopics(SourceClientContext context, MqttClient client) {
        if (!isActiveClient(context, client) || !client.isConnected()) {
            return;
        }
        Map<String, Integer> topicMap = new HashMap<>();
        int qos = ObjectUtil.defaultIfNull(context.getSnapshot().getQos(), 1);
        for (String topic : context.getSnapshot().getTopics()) {
            topicMap.put(topic, qos);
        }
        client.subscribe(topicMap, result -> {
            if (!isActiveClient(context, client)) {
                return;
            }
            if (result.succeeded()) {
                log.info("[subscribeTopics][订阅 MQTT Source 主题成功, sourceId={}, topicCount={}]",
                        context.getSourceId(), topicMap.size());
            } else {
                log.error("[subscribeTopics][订阅 MQTT Source 主题失败, sourceId={}, topicCount={}]",
                        context.getSourceId(), topicMap.size(), result.cause());
                stopClientOnly(context);
                scheduleReconnect(context);
            }
        });
    }

    private void setupHandlers(SourceClientContext context, MqttClient client) {
        client.publishHandler(message -> {
            if (!isActiveClient(context, client)) {
                return;
            }
            byte[] payload = message.payload() != null ? message.payload().getBytes() : Buffer.buffer().getBytes();
            upstreamHandler.handleMessage(configCacheService.getConfigsBySourceId(context.getSourceId()),
                    message.topicName(), payload);
        });
        client.exceptionHandler(exception -> {
            if (!isActiveClient(context, client)) {
                return;
            }
            log.error("[setupHandlers][MQTT Source 客户端异常, sourceId={}]", context.getSourceId(), exception);
        });
        client.closeHandler(v -> {
            if (!isActiveClient(context, client)) {
                return;
            }
            context.setClient(null);
            log.warn("[closeHandler][MQTT Source 连接已关闭，准备重连, sourceId={}]", context.getSourceId());
            scheduleReconnect(context);
        });
    }

    private void scheduleReconnect(SourceClientContext context) {
        if (!contextMap.containsKey(context.getSourceId()) || context.getReconnectTimerId() != null) {
            return;
        }
        long delay = ObjectUtil.defaultIfNull(context.getSnapshot().getReconnectDelayMs(), 5000L);
        Long timerId = vertx.setTimer(delay, id -> {
            context.setReconnectTimerId(null);
            SourceClientContext latestContext = contextMap.get(context.getSourceId());
            if (latestContext == null || latestContext != context) {
                return;
            }
            stopClientOnly(latestContext);
            createAndConnectClient(latestContext);
        });
        context.setReconnectTimerId(timerId);
        log.info("[scheduleReconnect][{} ms 后重连 MQTT Source, sourceId={}]", delay, context.getSourceId());
    }

    private void stopClientOnly(SourceClientContext context) {
        cancelReconnectTimer(context);
        MqttClient client = context.getClient();
        context.setClient(null);
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect(ar -> {
                    if (ar.failed()) {
                        log.warn("[stopClientOnly][关闭 MQTT Source 客户端失败, sourceId={}]",
                                context.getSourceId(), ar.cause());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("[stopClientOnly][关闭 MQTT Source 客户端失败, sourceId={}]", context.getSourceId(), e);
        }
    }

    private boolean isActiveClient(SourceClientContext context, MqttClient client) {
        return client != null
                && contextMap.get(context.getSourceId()) == context
                && context.getClient() == client;
    }

    private void disconnectClient(MqttClient client, Long sourceId) {
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect(ar -> {
                    if (ar.failed()) {
                        log.warn("[disconnectClient][关闭过期 MQTT Source 客户端失败, sourceId={}]", sourceId,
                                ar.cause());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("[disconnectClient][关闭过期 MQTT Source 客户端失败, sourceId={}]", sourceId, e);
        }
    }

    private void cancelReconnectTimer(SourceClientContext context) {
        if (context.getReconnectTimerId() == null) {
            return;
        }
        vertx.cancelTimer(context.getReconnectTimerId());
        context.setReconnectTimerId(null);
    }

    @Data
    private static class SourceClientContext {

        private Long sourceId;
        private SourceSnapshot snapshot;
        private MqttClient client;
        private Long reconnectTimerId;

    }

    @Data
    private static class SourceSnapshot {

        private final Long sourceId;
        private final String brokerHost;
        private final Integer brokerPort;
        private final String username;
        private final String password;
        private final String clientId;
        private final Integer qos;
        private final Boolean cleanSession;
        private final Integer keepAliveIntervalSeconds;
        private final Integer connectTimeoutSeconds;
        private final Long reconnectDelayMs;
        private final Boolean sslEnabled;
        private final List<String> topics;

        private static SourceSnapshot of(Collection<IotMqttDeviceConfigRespDTO> configList) {
            IotMqttDeviceConfigRespDTO first = CollUtil.getFirst(configList);
            List<String> topics = configList.stream()
                    .map(IotMqttDeviceConfigRespDTO::getTopic)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .sorted()
                    .toList();
            return new SourceSnapshot(first.getSourceId(), first.getBrokerHost(), first.getBrokerPort(),
                    first.getUsername(), first.getPassword(), first.getClientId(), first.getQos(),
                    first.getCleanSession(), first.getKeepAliveIntervalSeconds(), first.getConnectTimeoutSeconds(),
                    first.getReconnectDelayMs(), first.getSslEnabled(), Collections.unmodifiableList(topics));
        }

    }

}
