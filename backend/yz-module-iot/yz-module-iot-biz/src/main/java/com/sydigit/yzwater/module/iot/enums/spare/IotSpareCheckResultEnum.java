package com.sydigit.yzwater.module.iot.enums.spare;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 盘点结果类型
 */
@RequiredArgsConstructor
@Getter
public enum IotSpareCheckResultEnum implements ArrayValuable<String> {

    NORMAL("normal", "正常"),
    OVER("over", "盘盈"),
    SHORT("short", "盘亏");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotSpareCheckResultEnum::getResult).toArray(String[]::new);

    /**
     * 结果编码
     */
    private final String result;
    /**
     * 结果名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }
}
