package com.sydigit.yzwater.module.iot.enums.maintenanceplan;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 养护类型枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotMaintenancePlanTypeEnum implements ArrayValuable<String> {

    ROUTINE("routine", "日常常规养护"),
    PERIODIC("periodic", "周期定检养护"),
    SPECIAL("special", "专项场景养护"),
    FAULT_LINKED("fault_linked", "故障联动养护");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotMaintenancePlanTypeEnum::getType)
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
}
