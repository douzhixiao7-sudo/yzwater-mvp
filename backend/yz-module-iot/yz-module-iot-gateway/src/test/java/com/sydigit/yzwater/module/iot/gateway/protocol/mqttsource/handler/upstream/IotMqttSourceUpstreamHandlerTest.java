package com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.handler.upstream;

import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttPointRespDTO;
import com.sydigit.yzwater.module.iot.core.enums.IotDeviceMessageMethodEnum;
import com.sydigit.yzwater.module.iot.core.mq.message.IotDeviceMessage;
import com.sydigit.yzwater.module.iot.gateway.service.device.message.IotDeviceMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * {@link IotMqttSourceUpstreamHandler} 的单元测试
 */
@ExtendWith(MockitoExtension.class)
class IotMqttSourceUpstreamHandlerTest {

    @Mock
    private IotDeviceMessageService messageService;

    private IotMqttSourceUpstreamHandler upstreamHandler;

    @BeforeEach
    void setUp() {
        upstreamHandler = new IotMqttSourceUpstreamHandler(messageService, "mqtt-source-1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleMessage_success() {
        List<IotMqttDeviceConfigRespDTO> configList = List.of(
                buildConfig(1L, "gate_product", "device_01", "data/test/v1", "collectTime",
                        "yyyy-MM-dd HH:mm:ss", List.of(
                                buildPoint(1L, "rise", "上升信号", "ZMQBJ3_rise", "bool"),
                                buildPoint(2L, "fall", "下降信号", "ZMQBJ3_fall", "bool"))),
                buildConfig(2L, "gate_product", "device_02", "data/test/v1", null,
                        null, List.of(buildPoint(3L, "opening", "开度", "gate_opening", "float"))));

        upstreamHandler.handleMessage(configList, "data/test/v1",
                "{\"ZMQBJ3_rise\":1,\"ZMQBJ3_fall\":0,\"gate_opening\":12.5,\"collectTime\":\"2026-04-19 16:00:00\",\"ignored\":99}".getBytes());

        ArgumentCaptor<IotDeviceMessage> messageCaptor = ArgumentCaptor.forClass(IotDeviceMessage.class);
        ArgumentCaptor<String> productKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> deviceNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageService, times(2)).sendDeviceMessage(messageCaptor.capture(), productKeyCaptor.capture(),
                deviceNameCaptor.capture(), eq("mqtt-source-1"));

        Map<String, Integer> deviceIndexMap = Map.of(
                deviceNameCaptor.getAllValues().get(0), 0,
                deviceNameCaptor.getAllValues().get(1), 1);

        IotDeviceMessage device1Message = messageCaptor.getAllValues().get(deviceIndexMap.get("device_01"));
        assertEquals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), device1Message.getMethod());
        Map<String, Object> device1Params = (Map<String, Object>) device1Message.getParams();
        assertEquals(2, device1Params.size());
        assertEquals(1, device1Params.get("rise"));
        assertEquals(0, device1Params.get("fall"));
        assertEquals(LocalDateTime.of(2026, 4, 19, 16, 0, 0), device1Message.getReportTime());

        IotDeviceMessage device2Message = messageCaptor.getAllValues().get(deviceIndexMap.get("device_02"));
        assertEquals(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), device2Message.getMethod());
        Map<String, Object> device2Params = (Map<String, Object>) device2Message.getParams();
        assertEquals(1, device2Params.size());
        assertEquals(12.5D, device2Params.get("opening"));
        assertNotNull(device2Message.getReportTime());
    }

    @Test
    void testHandleMessage_ignoreInvalidTopic() {
        List<IotMqttDeviceConfigRespDTO> configList = List.of(
                buildConfig(1L, "gate_product", "device_01", "data/test/v1", null,
                        null, List.of(buildPoint(1L, "rise", "上升信号", "ZMQBJ3_rise", "bool"))));

        upstreamHandler.handleMessage(configList, "data/test/v2",
                "{\"ZMQBJ3_rise\":1}".getBytes());

        verify(messageService, never()).sendDeviceMessage(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    private static IotMqttDeviceConfigRespDTO buildConfig(Long deviceId, String productKey, String deviceName,
                                                          String topic, String reportTimeKey,
                                                          String reportTimeFormat, List<IotMqttPointRespDTO> points) {
        IotMqttDeviceConfigRespDTO config = new IotMqttDeviceConfigRespDTO();
        config.setDeviceId(deviceId);
        config.setProductKey(productKey);
        config.setDeviceName(deviceName);
        config.setTopic(topic);
        config.setPayloadMode("flat_json");
        config.setReportTimeKey(reportTimeKey);
        config.setReportTimeFormat(reportTimeFormat);
        config.setPoints(points);
        return config;
    }

    private static IotMqttPointRespDTO buildPoint(Long id, String identifier, String name,
                                                  String payloadKey, String valueType) {
        IotMqttPointRespDTO point = new IotMqttPointRespDTO();
        point.setId(id);
        point.setIdentifier(identifier);
        point.setName(name);
        point.setPayloadKey(payloadKey);
        point.setValueType(valueType);
        return point;
    }

}
