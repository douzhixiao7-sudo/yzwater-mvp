package com.sydigit.yzwater.module.iot.enums.spare;

import com.sydigit.yzwater.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 备件出入库审批状态
 */
@RequiredArgsConstructor
@Getter
public enum IotSpareIoAuditStatusEnum implements ArrayValuable<String> {

    PENDING("pending", "待审批"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已驳回");

    public static final String[] ARRAYS = Arrays.stream(values()).map(IotSpareIoAuditStatusEnum::getStatus).toArray(String[]::new);

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
