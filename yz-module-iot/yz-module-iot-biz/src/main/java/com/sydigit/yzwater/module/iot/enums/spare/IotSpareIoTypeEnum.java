package com.sydigit.yzwater.module.iot.enums.spare;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 备件出入库类型
 */
@RequiredArgsConstructor
@Getter
public enum IotSpareIoTypeEnum implements ArrayValuable<String> {

    IN("IN", "入库"),
    OUT("OUT", "出库");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotSpareIoTypeEnum::getType).toArray(String[]::new);

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
