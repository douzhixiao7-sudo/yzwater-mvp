package com.sydigit.yzwater.module.iot.enums.dispatchplan;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 调度方案状态枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotDispatchPlanStatusEnum implements ArrayValuable<Integer> {

    DRAFT(0, "草稿"),
    FINISHED(1, "已完成"),
    ARCHIVED(2, "已归档"),
    VOIDED(3, "已作废");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(IotDispatchPlanStatusEnum::getStatus)
            .toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isValid(Integer status) {
        if (status == null) {
            return false;
        }
        for (IotDispatchPlanStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return true;
            }
        }
        return false;
    }

    public static String getNameByStatus(Integer status) {
        if (status == null) {
            return "";
        }
        for (IotDispatchPlanStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value.getName();
            }
        }
        return String.valueOf(status);
    }
}

