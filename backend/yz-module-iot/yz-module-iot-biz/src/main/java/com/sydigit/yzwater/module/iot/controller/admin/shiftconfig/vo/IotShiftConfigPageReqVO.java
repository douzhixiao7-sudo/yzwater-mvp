package com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

/**
 * 班次配置分页请求 VO
 */
@Schema(description = "IoT - 班次配置分页 Request VO")
@Data
public class IotShiftConfigPageReqVO extends PageParam {

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "时间段（起始时间~结束时间）")
    @DateTimeFormat(pattern = "HH:mm")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime[] timeRange;
}
