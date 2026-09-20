package com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 调度方案分页 Request VO
 */
@Schema(description = "IoT - 调度方案分页 Request VO")
@Data
public class IotDispatchPlanPageReqVO extends PageParam {

    @Schema(description = "方案编号")
    private String planNo;

    @Schema(description = "方案名称")
    private String planName;

    @Schema(description = "方案类型")
    private String planType;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "编制人")
    private String prepareUserName;

    @Schema(description = "方案状态")
    private Integer planStatus;

    @Schema(description = "编制时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] prepareTime;
}
