package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 大屏水位列表 Request VO")
@Data
public class IotScreenStreamWaterListReqVO {

    @Schema(description = "站点值（字典 iot_zd_sbzd）", requiredMode = Schema.RequiredMode.REQUIRED, example = "320902102")
    @NotBlank(message = "所属站点不能为空")
    private String stationId;

    @Schema(description = "设备名称集合（可多选）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> deviceNames;

    @Schema(description = "时间窗口，仅支持 24h/48h/72h，默认 24h",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "24h")
    @Pattern(regexp = "24h|48h|72h", message = "时间窗口仅支持 24h、48h、72h")
    private String timeRange;

    @Schema(description = "开始时间（与 endTime 配对，且不可与 timeRange 同时传）",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2026-03-18 00:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间（与 startTime 配对，且不可与 timeRange 同时传）",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "2026-03-19 23:59:59")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
