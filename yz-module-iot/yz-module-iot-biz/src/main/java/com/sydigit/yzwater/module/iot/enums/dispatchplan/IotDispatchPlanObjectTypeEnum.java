package com.sydigit.yzwater.module.iot.enums.dispatchplan;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 调度方案操作对象类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotDispatchPlanObjectTypeEnum implements ArrayValuable<Integer> {

    DEVICE(1, "设备"),
    CUSTOM(2, "自定义");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotDispatchPlanObjectTypeEnum::getType)
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

    public static boolean isValid(Integer type) {
        if (type == null) {
            return false;
        }
        for (IotDispatchPlanObjectTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}

