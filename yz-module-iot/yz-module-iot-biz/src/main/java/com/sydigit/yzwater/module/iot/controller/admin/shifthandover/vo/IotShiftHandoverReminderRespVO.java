package com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 交接班提醒响应
 */
@Schema(description = "IoT - 交接班提醒 Response VO")
@Data
public class IotShiftHandoverReminderRespVO {

    @Schema(description = "是否需要提醒")
    private Boolean needRemind;

    @Schema(description = "提醒文案")
    private String message;

    @Schema(description = "交接班默认信息")
    private IotShiftHandoverDefaultRespVO handoverDefault;
}

