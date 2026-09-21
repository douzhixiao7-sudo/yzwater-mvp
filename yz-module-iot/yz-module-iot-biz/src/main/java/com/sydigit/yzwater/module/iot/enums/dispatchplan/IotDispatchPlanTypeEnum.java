package com.sydigit.yzwater.module.iot.enums.dispatchplan;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 调度方案类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotDispatchPlanTypeEnum implements ArrayValuable<String> {

    ROUTINE("routine", "日常调度"),
    FLOOD("flood", "防洪调度"),
    DROUGHT("drought", "抗旱调度"),
    ECO_REPLENISHMENT("eco_replenishment", "生态补水"),
    EMERGENCY("emergency", "应急调度");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotDispatchPlanTypeEnum::getType)
            .toArray(String[]::new);

    /**
     * 类型值
     */
    private final String type;
    /**
     * 类型名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static boolean isValid(String type) {
        if (type == null) {
            return false;
        }
        for (IotDispatchPlanTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static String getNameByType(String type) {
        if (type == null) {
            return "";
        }
        for (IotDispatchPlanTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value.getName();
            }
        }
        return type;
    }
}

