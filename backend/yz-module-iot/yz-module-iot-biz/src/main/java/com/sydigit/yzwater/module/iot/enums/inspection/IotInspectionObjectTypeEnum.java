package com.sydigit.yzwater.module.iot.enums.inspection;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 巡检对象类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotInspectionObjectTypeEnum implements ArrayValuable<Integer> {

    DEVICE(1, "设备"),
    LOCATION(2, "区域");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotInspectionObjectTypeEnum::getType)
            .toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
