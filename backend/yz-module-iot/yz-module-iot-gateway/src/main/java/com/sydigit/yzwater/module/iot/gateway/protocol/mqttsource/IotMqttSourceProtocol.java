package com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource;

import com.sydigit.yzwater.module.iot.core.biz.IotDeviceCommonApi;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.util.IotDeviceMessageUtils;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.handler.upstream.IotMqttSourceUpstreamHandler;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.manager.IotMqttSourceConfigCacheService;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.manager.IotMqttSourceConnectionManager;
import com.sydigit.yzwater.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * IoT MQTT Source 固定主题采集协议
 */
@Slf4j
public class IotMqttSourceProtocol {

    private final IotMqttSourceConfig properties;
    @Getter
    private final String serverId;
    @Getter
    private volatile boolean running = false;

    private final Vertx vertx;
    private Long configRefreshTimerId;

    private final IotMqttSourceConfigCacheService configCacheService;
    private final IotMqttSourceConnectionManager connectionManager;

    public IotMqttSourceProtocol(IotMqttSourceConfig properties, IotDeviceCommonApi deviceApi,
                                 IotDeviceMessageService messageService, Vertx vertx) {
        Assert.notNull(properties, "MQTT Source 协议配置不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(0);
        this.vertx = vertx;
        this.configCacheService = new IotMqttSourceConfigCacheService(deviceApi);
        IotMqttSourceUpstreamHandler upstreamHandler = new IotMqttSourceUpstreamHandler(messageService, serverId);
        this.connectionManager = new IotMqttSourceConnectionManager(vertx, configCacheService, upstreamHandler,
                properties.getMaxMessageSize());
    }

    @PostConstruct
    public void start() {
        if (running) {
            log.warn("[start][IoT MQTT Source 协议已经在运行中]");
            return;
        }
        try {
            refreshConfig();
            int refreshInterval = properties.getConfigRefreshInterval();
            configRefreshTimerId = vertx.setPeriodic(TimeUnit.SECONDS.toMillis(refreshInterval), id -> refreshConfig());
            running = true;
            log.info("[start][IoT MQTT Source 协议启动成功，serverId={}]", serverId);
        } catch (Exception e) {
            log.error("[start][IoT MQTT Source 协议启动失败]", e);
            stop0();
            throw e;
        }
    }

    @PreDestroy
    public void stop() {
        if (!running) {
            return;
        }
        stop0();
    }

    private void stop0() {
        if (configRefreshTimerId != null) {
            vertx.cancelTimer(configRefreshTimerId);
            configRefreshTimerId = null;
        }
        connectionManager.stopAll();
        running = false;
        log.info("[stop][IoT MQTT Source 协议已停止]");
    }

    private synchronized void refreshConfig() {
        try {
            Map<Long, List<IotMqttDeviceConfigRespDTO>> sourceConfigMap = configCacheService.refreshConfig();
            if (sourceConfigMap == null) {
                log.warn("[refreshConfig][API 失败，跳过本轮 MQTT Source 配置刷新]");
                return;
            }
            connectionManager.syncConnections(sourceConfigMap);
            configCacheService.cleanupRemovedSources(sourceConfigMap.keySet());
            log.debug("[refreshConfig][本轮 MQTT Source 刷新完成, sourceCount={}]", sourceConfigMap.size());
        } catch (Exception e) {
            log.error("[refreshConfig][刷新 MQTT Source 配置失败]", e);
        }
    }

}
