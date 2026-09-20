package com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 交接班默认信息
 */
@Schema(description = "IoT - 交接班默认信息 Response VO")
@Data
public class IotShiftHandoverDefaultRespVO {

    @Schema(description = "排班 ID")
    private Long scheduleId;

    @Schema(description = "班次 ID")
    private Long shiftId;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "班组 ID")
    private Long teamId;

    @Schema(description = "班组名称")
    private String teamName;

    @Schema(description = "交班人 ID")
    private Long handoverUserId;

    @Schema(description = "交班人")
    private String handoverUserName;

    @Schema(description = "接班人 ID")
    private Long takeoverUserId;

    @Schema(description = "接班人")
    private String takeoverUserName;

    @Schema(description = "交接班时间")
    private String handoverTime;

    @Schema(description = "下一班次开始时间")
    private String nextShiftStartTime;
}

