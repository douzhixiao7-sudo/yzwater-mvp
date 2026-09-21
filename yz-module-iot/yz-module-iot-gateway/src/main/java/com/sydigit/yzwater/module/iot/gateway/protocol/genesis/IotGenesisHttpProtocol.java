package com.sydigit.yzwater.module.iot.gateway.protocol.genesis;

import cn.hutool.core.lang.Assert;
import com.sydigit.yzwater.module.iot.core.biz.IotDeviceCommonApi;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.util.IotDeviceMessageUtils;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.client.IotGenesisHttpClient;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.handler.upstream.IotGenesisUpstreamHandler;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.manager.IotGenesisConfigCacheService;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.manager.IotGenesisPollScheduler;
import com.sydigit.yzwater.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * IoT GENESIS64 HTTP 主动采集协议
 */
@Slf4j
public class IotGenesisHttpProtocol {

    private final IotGenesisHttpConfig properties;
    @Getter
    private final String serverId;
    @Getter
    private volatile boolean running = false;

    private final Vertx vertx;
    private Long configRefreshTimerId;

    private final IotGenesisConfigCacheService configCacheService;
    private final IotGenesisPollScheduler pollScheduler;

    public IotGenesisHttpProtocol(IotGenesisHttpConfig properties, IotDeviceCommonApi deviceApi,
                                  IotDeviceMessageService messageService, Vertx vertx) {
        Assert.notNull(properties, "GENESIS64 HTTP 协议配置不能为空");
        this.properties = properties;
        this.serverId = IotDeviceMessageUtils.generateServerId(0);
        this.vertx = vertx;
        this.configCacheService = new IotGenesisConfigCacheService(deviceApi);
        IotGenesisHttpClient httpClient = new IotGenesisHttpClient();
        IotGenesisUpstreamHandler upstreamHandler = new IotGenesisUpstreamHandler(messageService, serverId);
        this.pollScheduler = new IotGenesisPollScheduler(vertx, httpClient, upstreamHandler, configCacheService);
    }

    @PostConstruct
    public void start() {
        if (running) {
            log.warn("[start][IoT GENESIS64 HTTP 协议已经在运行中]");
            return;
        }
        try {
            refreshConfig();
            int refreshInterval = properties.getConfigRefreshInterval();
            configRefreshTimerId = vertx.setPeriodic(TimeUnit.SECONDS.toMillis(refreshInterval), id -> refreshConfig());
            running = true;
            log.info("[start][IoT GENESIS64 HTTP 协议启动成功，serverId={}]", serverId);
        } catch (Exception e) {
            log.error("[start][IoT GENESIS64 HTTP 协议启动失败]", e);
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
        pollScheduler.stopAll();
        running = false;
        log.info("[stop][IoT GENESIS64 HTTP 协议已停止]");
    }

    private synchronized void refreshConfig() {
        try {
            List<IotGenesisDeviceConfigRespDTO> configs = configCacheService.refreshConfig();
            if (configs == null) {
                log.warn("[refreshConfig][API 失败，跳过本轮 GENESIS64 配置刷新]");
                return;
            }
            for (IotGenesisDeviceConfigRespDTO config : configs) {
                try {
                    pollScheduler.updatePolling(config);
                } catch (Exception e) {
                    log.error("[refreshConfig][处理 GENESIS64 设备配置失败, deviceId={}]", config.getDeviceId(), e);
                }
            }
            Set<Long> removedDeviceIds = configCacheService.cleanupRemovedDevices(configs);
            for (Long deviceId : removedDeviceIds) {
                pollScheduler.stopPolling(deviceId);
            }
        } catch (Exception e) {
            log.error("[refreshConfig][刷新 GENESIS64 配置失败]", e);
        }
    }

}
