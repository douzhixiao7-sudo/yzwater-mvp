package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 员工排班新增/编辑请求
 */
@Schema(description = "IoT - 员工排班新增/编辑 Request VO")
@Data
public class IotShiftScheduleSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）", example = "1024")
    private Long id;

    @Schema(description = "值班日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-03-04")
    @NotNull(message = "值班日期不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate scheduleDate;

    @Schema(description = "所属站点", example = "ST001")
    private String stationId;

    @Schema(description = "值班班次 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "值班班次不能为空")
    private Long shiftId;

    @Schema(description = "班组 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "班组不能为空")
    private Long teamId;

    @Schema(description = "值班人员 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "值班人员不能为空")
    private Long dutyUserId;

    @Schema(description = "联系方式（手机号）", example = "13800138000")
    private String dutyMobile;

    @Schema(description = "值班开始时间（编辑时可传）", example = "2026-03-06 09:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime dutyStartTime;

    @Schema(description = "值班结束时间（编辑时可传）", example = "2026-03-06 17:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime dutyEndTime;

    @Schema(description = "值班日志")
    private String dutyLog;

    @Schema(description = "备注")
    private String remark;
}
