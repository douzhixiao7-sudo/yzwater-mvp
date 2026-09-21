package com.sydigit.yzwater.module.iot.enums.inspection;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 巡检建议周期单位枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotInspectionCycleUnitEnum implements ArrayValuable<String> {

    DAY("DAY", "日"),
    WEEK("WEEK", "周"),
    MONTH("MONTH", "月"),
    QUARTER("QUARTER", "季度"),
    YEAR("YEAR", "年");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotInspectionCycleUnitEnum::getUnit)
            .toArray(String[]::new);

    /**
     * 周期单位值
     */
    private final String unit;
    /**
     * 周期单位名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
