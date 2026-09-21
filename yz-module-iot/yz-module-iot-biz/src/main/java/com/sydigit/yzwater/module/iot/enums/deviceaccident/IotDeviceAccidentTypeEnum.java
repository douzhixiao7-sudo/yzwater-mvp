package com.sydigit.yzwater.module.iot.enums.deviceaccident;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 设备事故类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotDeviceAccidentTypeEnum implements ArrayValuable<String> {

    DEVICE_FAULT("device_fault", "设备故障"),
    OPERATION_ERROR("operation_error", "操作失误"),
    OTHER("other", "其他");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotDeviceAccidentTypeEnum::getType)
            .toArray(String[]::new);

    /**
     * 类型
     */
    private final String type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}