package com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - IoT MQTT 数据源分页 Request VO")
@Data
public class IotMqttSourcePageReqVO extends PageParam {

    @Schema(description = "数据源名称", example = "闸站 MQTT 源")
    private String name;

    @Schema(description = "数据源编码", example = "GATE_MQTT_SOURCE")
    private String code;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
