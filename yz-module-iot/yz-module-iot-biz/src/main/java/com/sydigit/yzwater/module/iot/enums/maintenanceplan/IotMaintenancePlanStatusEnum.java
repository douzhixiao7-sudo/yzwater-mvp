package com.sydigit.yzwater.module.iot.enums.maintenanceplan;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 养护计划状态枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotMaintenancePlanStatusEnum implements ArrayValuable<String> {

    PENDING("pending", "未完成"),
    COMPLETED("completed", "已完成");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotMaintenancePlanStatusEnum::getStatus)
            .toArray(String[]::new);

    /**
     * 状态值
     */
    private final String status;
    /**
     * 状态名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
