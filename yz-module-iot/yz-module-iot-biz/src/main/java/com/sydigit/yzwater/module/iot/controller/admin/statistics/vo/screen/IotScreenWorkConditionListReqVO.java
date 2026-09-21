package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 大屏工情统计 Request VO")
@Data
public class IotScreenWorkConditionListReqVO {

    @Schema(description = "站点值（字典 iot_zd_sbzd）", requiredMode = Schema.RequiredMode.REQUIRED, example = "320902102")
    @NotBlank(message = "所属站点不能为空")
    private String stationId;

    @Schema(description = "设备类型（字典 iot_deivce_type；不传则该站全部类型）", example = "7")
    private Integer deviceType;

    @Schema(description = "开始时间", example = "2026-03-18 00:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-03-19 23:59:59")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "设备名称（可选；不传则查询该类型下全部设备）", example = "1#闸门")
    private String deviceName;
}
