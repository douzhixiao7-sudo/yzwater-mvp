package com.sydigit.yzwater.module.iot.gateway.protocol.genesis.manager;

import cn.hutool.core.collection.CollUtil;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.client.IotGenesisHttpClient;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.client.IotGenesisRealtimeDataItem;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.handler.upstream.IotGenesisUpstreamHandler;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoT GENESIS64 轮询调度器
 */
@Slf4j
public class IotGenesisPollScheduler {

    private final Vertx vertx;
    private final IotGenesisHttpClient httpClient;
    private final IotGenesisUpstreamHandler upstreamHandler;
    private final IotGenesisConfigCacheService configCacheService;
    private final Map<Long, DeviceTimerInfo> deviceTimers = new ConcurrentHashMap<>();

    public IotGenesisPollScheduler(Vertx vertx, IotGenesisHttpClient httpClient,
                                   IotGenesisUpstreamHandler upstreamHandler,
                                   IotGenesisConfigCacheService configCacheService) {
        this.vertx = vertx;
        this.httpClient = httpClient;
        this.upstreamHandler = upstreamHandler;
        this.configCacheService = configCacheService;
    }

    @Data
    @AllArgsConstructor
    private static class DeviceTimerInfo {
        private Long timerId;
        private Integer collectInterval;
    }

    public void updatePolling(IotGenesisDeviceConfigRespDTO config) {
        Long deviceId = config.getDeviceId();
        Integer collectInterval = config.getCollectInterval();
        if (collectInterval == null || collectInterval <= 0 || CollUtil.isEmpty(config.getPoints())) {
            stopPolling(deviceId);
            return;
        }
        DeviceTimerInfo currentTimer = deviceTimers.get(deviceId);
        if (currentTimer == null) {
            deviceTimers.put(deviceId, new DeviceTimerInfo(createCollectTimer(deviceId, collectInterval),
                    collectInterval));
            return;
        }
        if (!Objects.equals(currentTimer.getCollectInterval(), collectInterval)) {
            vertx.cancelTimer(currentTimer.getTimerId());
            deviceTimers.put(deviceId, new DeviceTimerInfo(createCollectTimer(deviceId, collectInterval),
                    collectInterval));
        }
    }

    public void stopPolling(Long deviceId) {
        DeviceTimerInfo timerInfo = deviceTimers.remove(deviceId);
        if (timerInfo != null) {
            vertx.cancelTimer(timerInfo.getTimerId());
        }
    }

    public void stopAll() {
        Set<Long> deviceIds = new HashMap<>(deviceTimers).keySet();
        for (Long deviceId : deviceIds) {
            stopPolling(deviceId);
        }
    }

    private Long createCollectTimer(Long deviceId, Integer collectInterval) {
        return vertx.setPeriodic(collectInterval, timerId -> collectDevice(deviceId));
    }

    private void collectDevice(Long deviceId) {
        IotGenesisDeviceConfigRespDTO config = configCacheService.getConfig(deviceId);
        if (config == null || CollUtil.isEmpty(config.getPoints())) {
            return;
        }
        try {
            List<IotGenesisRealtimeDataItem> dataItems = httpClient.queryRealtimeData(config);
            upstreamHandler.handleCollectResult(config, dataItems);
        } catch (Exception e) {
            log.error("[collectDevice][设备 {} 采集失败]", deviceId, e);
        }
    }

}
