package com.sydigit.yzwater.module.iot.enums.spare;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 备件用途类型
 */
@RequiredArgsConstructor
@Getter
public enum IotSpareUsageTypeEnum implements ArrayValuable<String> {

    FAULT("fault", "维修工单"),
    MAINTENANCE("maintenance", "养护任务"),
    OTHER("other", "其他");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotSpareUsageTypeEnum::getType).toArray(String[]::new);

    /**
     * 类型编码
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
