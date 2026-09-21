package com.sydigit.yzwater.module.iot.gateway.protocol.genesis.handler.upstream;

import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisPointRespDTO;
import com.sydigit.yzwater.module.iot.core.enums.IotDeviceMessageMethodEnum;
import com.sydigit.yzwater.module.iot.core.mq.message.IotDeviceMessage;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.client.IotGenesisRealtimeDataItem;
import com.sydigit.yzwater.module.iot.gateway.service.device.message.IotDeviceMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * {@link IotGenesisUpstreamHandler} 的单元测试
 */
@ExtendWith(MockitoExtension.class)
class IotGenesisUpstreamHandlerTest {

    @Mock
    private IotDeviceMessageService messageService;

    private IotGenesisUpstreamHandler upstreamHandler;

    @BeforeEach
    void setUp() {
        upstreamHandler = new IotGenesisUpstreamHandler(messageService, "genesis-server-1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleCollectResult_success() {
        IotGenesisDeviceConfigRespDTO config = new IotGenesisDeviceConfigRespDTO();
        config.setDeviceId(1L);
        config.setProductKey("genesis_demo");
        config.setDeviceName("device_01");
        config.setPoints(List.of(
                buildPoint(1L, "switch01", "开关量01", "modbus:3号闸.开关量06"),
                buildPoint(2L, "switch02", "开关量02", "modbus:3号闸.开关量07")
        ));
        List<IotGenesisRealtimeDataItem> dataItems = List.of(
                buildData("modbus:3号闸.开关量06", 1),
                buildData("modbus:3号闸.开关量07", 0),
                buildData("modbus:3号闸.不存在点位", 2)
        );

        upstreamHandler.handleCollectResult(config, dataItems);

        ArgumentCaptor<IotDeviceMessage> captor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        verify(messageService).sendDeviceMessage(captor.capture(), eq("genesis_demo"),
                eq("device_01"), eq("genesis-server-1"));
        assertEquals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), captor.getValue().getMethod());
        Map<String, Object> params = (Map<String, Object>) captor.getValue().getParams();
        assertEquals(2, params.size());
        assertEquals(1, params.get("switch01"));
        assertEquals(0, params.get("switch02"));
    }

    private static IotGenesisPointRespDTO buildPoint(Long id, String identifier, String name, String pointName) {
        IotGenesisPointRespDTO point = new IotGenesisPointRespDTO();
        point.setId(id);
        point.setIdentifier(identifier);
        point.setName(name);
        point.setPointName(pointName);
        return point;
    }

    private static IotGenesisRealtimeDataItem buildData(String pointName, Object value) {
        IotGenesisRealtimeDataItem item = new IotGenesisRealtimeDataItem();
        item.setPointName(pointName);
        item.setValue(value);
        item.setQuality("Good");
        return item;
    }

}
