package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT 实时数据点位映射分页 Request VO")
@Data
public class IotRealtimeDataMappingPageReqVO extends PageParam {

    @Schema(description = "采集源编号", example = "1024")
    private Long sourceId;

    @Schema(description = "点位名称", example = "modbus:xxx")
    private String pointName;

    @Schema(description = "设备编号", example = "2048")
    private Long deviceId;

    @Schema(description = "设备名称", example = "潘家河设备")
    private String deviceName;

    @Schema(description = "物模型标识符", example = "ph")
    private String identifier;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
