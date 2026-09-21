package com.sydigit.yzwater.module.iot.enums.devicerating;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 设备评级标准
 */
@RequiredArgsConstructor
@Getter
public enum IotDeviceRatingResultEnum implements ArrayValuable<String> {

    EXCELLENT("excellent", "优秀"),
    GOOD("good", "良好"),
    QUALIFIED("qualified", "合格"),
    UNQUALIFIED("unqualified", "不合格");

    public static final String[] ARRAYS = Arrays.stream(values())
            .map(IotDeviceRatingResultEnum::getResult)
            .toArray(String[]::new);

    /**
     * 评级结果
     */
    private final String result;
    /**
     * 评级名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
