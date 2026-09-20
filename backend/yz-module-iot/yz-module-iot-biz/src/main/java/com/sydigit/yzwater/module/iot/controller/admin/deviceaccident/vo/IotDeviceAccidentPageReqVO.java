package com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 设备事故分页 Request VO")
@Data
public class IotDeviceAccidentPageReqVO extends PageParam {

    @Schema(description = "设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "事故类型", example = "device_fault")
    private String accidentType;

    @Schema(description = "责任人", example = "张三")
    private String responsibleName;

    @Schema(description = "事故发生时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] accidentTime;
}