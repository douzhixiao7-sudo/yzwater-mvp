package com.sydigit.yzwater.module.iot.enums.inspection;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 巡检检查结果枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotInspectionCheckResultEnum implements ArrayValuable<String> {

    EXCELLENT("excellent", "优秀"),
    GOOD("good", "良好"),
    QUALIFIED("qualified", "合格"),
    UNQUALIFIED("unqualified", "不合格");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotInspectionCheckResultEnum::getValue)
            .toArray(String[]::new);

    /**
     * 结果值
     */
    private final String value;
    /**
     * 结果名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
