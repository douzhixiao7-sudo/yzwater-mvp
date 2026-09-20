package com.sydigit.yzwater.module.iot.enums.dispatchmanage;

import java.util.Arrays;

/**
 * 调度指令执行状态枚举
 */
public enum IotDispatchInstructionStatusEnum {

    WAIT_EXECUTE(1, "待执行"),
    EXECUTED(2, "已执行"),
    OVERDUE(3, "已逾期");

    private final Integer status;
    private final String name;

    IotDispatchInstructionStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public static boolean isValid(Integer status) {
        return Arrays.stream(values()).anyMatch(item -> item.status.equals(status));
    }

    public static String getNameByStatus(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .map(IotDispatchInstructionStatusEnum::getName)
                .findFirst()
                .orElse("");
    }
}

