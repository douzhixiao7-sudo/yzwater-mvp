package com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.manager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.iot.core.biz.IotDeviceCommonApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link IotMqttSourceConfigCacheService} 的单元测试
 */
@ExtendWith(MockitoExtension.class)
class IotMqttSourceConfigCacheServiceTest {

    @Mock
    private IotDeviceCommonApi deviceApi;

    private IotMqttSourceConfigCacheService cacheService;
    private Logger logger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        cacheService = new IotMqttSourceConfigCacheService(deviceApi);
        logger = (Logger) LoggerFactory.getLogger(IotMqttSourceConfigCacheService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
    }

    @Test
    void testRefreshConfig_warnWhenRpcReturnsBusinessError() {
        when(deviceApi.getMqttDeviceConfigList(any()))
                .thenReturn(CommonResult.error(401, "账号未登录"));

        assertNull(cacheService.refreshConfig());

        assertEquals(1, logAppender.list.size());
        ILoggingEvent loggingEvent = logAppender.list.get(0);
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertTrue(loggingEvent.getFormattedMessage().contains("code=401"));
        assertTrue(loggingEvent.getFormattedMessage().contains("账号未登录"));
        assertTrue(loggingEvent.getFormattedMessage().contains("MQTT Source RPC 接口"));
    }

}
