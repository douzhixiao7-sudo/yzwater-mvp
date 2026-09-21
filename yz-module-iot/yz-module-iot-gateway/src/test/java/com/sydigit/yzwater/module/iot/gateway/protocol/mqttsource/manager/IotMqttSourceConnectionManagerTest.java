package com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.manager;

import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.handler.upstream.IotMqttSourceUpstreamHandler;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttServerOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link IotMqttSourceConnectionManager} 的单元测试
 */
@ExtendWith(MockitoExtension.class)
class IotMqttSourceConnectionManagerTest {

    @Mock
    private IotMqttSourceConfigCacheService configCacheService;
    @Mock
    private IotMqttSourceUpstreamHandler upstreamHandler;

    private Vertx vertx;
    private MqttServer mqttServer;
    private IotMqttSourceConnectionManager connectionManager;

    @BeforeEach
    void setUp() throws InterruptedException {
        vertx = Vertx.vertx();
        mqttServer = MqttServer.create(vertx, new MqttServerOptions().setPort(0));
        mqttServer.endpointHandler(endpoint -> endpoint.accept(false));
        CountDownLatch listenLatch = new CountDownLatch(1);
        AtomicReference<Throwable> listenError = new AtomicReference<>();
        mqttServer.listen(ar -> {
            if (ar.failed()) {
                listenError.set(ar.cause());
            }
            listenLatch.countDown();
        });
        assertTrue(listenLatch.await(5, TimeUnit.SECONDS));
        assertNull(listenError.get());
        connectionManager = new IotMqttSourceConnectionManager(vertx, configCacheService, upstreamHandler, 8192);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (connectionManager != null) {
            connectionManager.stopAll();
        }
        if (mqttServer != null) {
            CountDownLatch closeServerLatch = new CountDownLatch(1);
            mqttServer.close(ar -> closeServerLatch.countDown());
            closeServerLatch.await(5, TimeUnit.SECONDS);
        }
        if (vertx != null) {
            CountDownLatch closeVertxLatch = new CountDownLatch(1);
            vertx.close(ar -> closeVertxLatch.countDown());
            closeVertxLatch.await(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void testSyncConnections_shouldNotBlockEventLoop() throws InterruptedException {
        Map<Long, List<IotMqttDeviceConfigRespDTO>> sourceConfigMap = Map.of(1L,
                List.of(buildConfig(1L, mqttServer.actualPort())));
        CountDownLatch timerLatch = new CountDownLatch(1);

        vertx.runOnContext(ignored -> {
            vertx.setTimer(100, id -> timerLatch.countDown());
            connectionManager.syncConnections(sourceConfigMap);
        });

        assertTrue(timerLatch.await(1, TimeUnit.SECONDS));
    }

    @Test
    void testBuildClientOptions_shouldApplyConfiguredMaxMessageSize() throws Exception {
        connectionManager = new IotMqttSourceConnectionManager(vertx, configCacheService, upstreamHandler, 20000);
        IotMqttDeviceConfigRespDTO config = buildConfig(1L, mqttServer.actualPort());

        Class<?> snapshotClass = Class.forName(
                "com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.manager.IotMqttSourceConnectionManager$SourceSnapshot");
        Constructor<?> constructor = snapshotClass.getDeclaredConstructor(Long.class, String.class, Integer.class,
                String.class, String.class, String.class, Integer.class, Boolean.class, Integer.class,
                Integer.class, Long.class, Boolean.class, List.class);
        constructor.setAccessible(true);
        Object snapshot = constructor.newInstance(config.getSourceId(), config.getBrokerHost(), config.getBrokerPort(),
                config.getUsername(), config.getPassword(), config.getClientId(), config.getQos(),
                config.getCleanSession(), config.getKeepAliveIntervalSeconds(), config.getConnectTimeoutSeconds(),
                config.getReconnectDelayMs(), config.getSslEnabled(), List.of(config.getTopic()));

        Method method = IotMqttSourceConnectionManager.class
                .getDeclaredMethod("buildClientOptions", snapshotClass);
        method.setAccessible(true);
        MqttClientOptions options = (MqttClientOptions) method.invoke(connectionManager, snapshot);

        assertEquals(20000, options.getMaxMessageSize());
    }

    private static IotMqttDeviceConfigRespDTO buildConfig(Long sourceId, int brokerPort) {
        IotMqttDeviceConfigRespDTO config = new IotMqttDeviceConfigRespDTO();
        config.setSourceId(sourceId);
        config.setBrokerHost("127.0.0.1");
        config.setBrokerPort(brokerPort);
        config.setClientId("mqtt-source-test-" + sourceId);
        config.setQos(1);
        config.setCleanSession(true);
        config.setKeepAliveIntervalSeconds(30);
        config.setConnectTimeoutSeconds(0);
        config.setReconnectDelayMs(5000L);
        config.setSslEnabled(false);
        config.setTopic("data/test/v1");
        return config;
    }

}
