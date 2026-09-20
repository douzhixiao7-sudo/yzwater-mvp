package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 实时数据采集源分页 Request VO")
@Data
public class IotRealtimeDataSourcePageReqVO extends PageParam {

    @Schema(description = "采集源名称", example = "厂站 A")
    private String name;

    @Schema(description = "采集源编码", example = "STATION_A")
    private String code;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}