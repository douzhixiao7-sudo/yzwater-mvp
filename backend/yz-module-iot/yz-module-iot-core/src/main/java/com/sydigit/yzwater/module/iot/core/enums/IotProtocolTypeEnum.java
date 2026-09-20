package com.sydigit.yzwater.module.iot.core.enums;

import cn.hutool.core.util.ArrayUtil;
import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * IoT 协议类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotProtocolTypeEnum implements ArrayValuable<String> {

    TCP("tcp"),
    UDP("udp"),
    WEBSOCKET("websocket"),
    HTTP("http"),
    MQTT("mqtt"),
    EMQX("emqx"),
    COAP("coap"),
    GENESIS64_HTTP("genesis64_http"),
    MQTT_SOURCE("mqtt_source");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotProtocolTypeEnum::getType)
            .toArray(String[]::new);

    private final String type;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static IotProtocolTypeEnum of(String type) {
        return ArrayUtil.firstMatch(e -> e.getType().equals(type), values());
    }

}
