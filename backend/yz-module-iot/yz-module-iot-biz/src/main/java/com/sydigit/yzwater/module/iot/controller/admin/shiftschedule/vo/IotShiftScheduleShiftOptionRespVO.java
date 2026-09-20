package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

/**
 * 班次下拉选项
 */
@Schema(description = "IoT - 员工排班班次选项 Response VO")
@Data
public class IotShiftScheduleShiftOptionRespVO {

    @Schema(description = "班次 ID", example = "1")
    private Long id;

    @Schema(description = "班次编号")
    private String shiftNo;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "起始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "跨天标识")
    private Boolean crossDay;
}
