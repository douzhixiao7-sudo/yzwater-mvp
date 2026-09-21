package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工排班月历请求
 */
@Schema(description = "IoT - 员工排班月历 Request VO")
@Data
public class IotShiftScheduleCalendarReqVO {

    @Schema(description = "月份（yyyy-MM）", example = "2026-03")
    private String month;

    @Schema(description = "关键字（排班编号/值班人员/联系方式/班组/班次）")
    private String keyword;

    @Schema(description = "班组 ID")
    private Long teamId;

    @Schema(description = "班次 ID")
    private Long shiftId;

    @Schema(description = "值班人员 ID")
    private Long dutyUserId;
}
