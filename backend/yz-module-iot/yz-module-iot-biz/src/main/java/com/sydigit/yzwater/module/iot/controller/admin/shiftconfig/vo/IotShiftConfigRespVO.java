package com.sydigit.yzwater.module.iot.controller.admin.shiftconfig.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 班次配置响应 VO
 */
@Schema(description = "IoT - 班次配置 Response VO")
@Data
public class IotShiftConfigRespVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "班次编号")
    private String shiftNo;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "起始时间")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @Schema(description = "跨天标识")
    private Boolean crossDay;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
