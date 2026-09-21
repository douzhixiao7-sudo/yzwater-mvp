package com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

/**
 * 交接班分页请求
 */
@Schema(description = "IoT - 交接班分页 Request VO")
@Data
public class IotShiftHandoverPageReqVO extends PageParam {

    @Schema(description = "关键字（交接班编号/班次/班组/交班人/接班人）")
    private String keyword;

    @Schema(description = "班组 ID")
    private Long teamId;

    @Schema(description = "班次 ID")
    private Long shiftId;

    @Schema(description = "交班人用户 ID")
    private Long handoverUserId;

    @Schema(description = "接班人用户 ID")
    private Long takeoverUserId;

    @Schema(description = "状态（0待交接 1已交接）")
    private Integer status;

    @Schema(description = "交接日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] handoverDateRange;
}

