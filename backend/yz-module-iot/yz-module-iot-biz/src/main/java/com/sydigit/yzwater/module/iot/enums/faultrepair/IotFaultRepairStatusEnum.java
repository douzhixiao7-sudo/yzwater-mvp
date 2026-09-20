package com.sydigit.yzwater.module.iot.enums.faultrepair;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 故障维修工单状态枚举
 */
@RequiredArgsConstructor
@Getter
public enum IotFaultRepairStatusEnum implements ArrayValuable<String> {

    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已处理");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotFaultRepairStatusEnum::getStatus).toArray(String[]::new);

    /**
     * 状态编码
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
