package com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "IoT - 设备评级分页 Request VO")
@Data
public class IotDeviceRatingPageReqVO extends PageParam {

    @Schema(description = "设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "评级结果", example = "excellent")
    private String ratingResult;

    @Schema(description = "评级人", example = "张三")
    private String ratingUserName;

    @Schema(description = "评级时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] ratingTime;
}
