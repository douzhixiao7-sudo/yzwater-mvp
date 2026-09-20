package com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工排班响应
 */
@Schema(description = "IoT - 员工排班 Response VO")
@Data
public class IotShiftScheduleRespVO {

    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "排班编号")
    private String scheduleNo;

    @Schema(description = "值班日期")
    private LocalDate scheduleDate;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "班次 ID")
    private Long shiftId;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "班组 ID")
    private Long teamId;

    @Schema(description = "班组名称")
    private String teamName;

    @Schema(description = "值班人员 ID")
    private Long dutyUserId;

    @Schema(description = "值班人员")
    private String dutyUserName;

    @Schema(description = "联系方式")
    private String dutyMobile;

    @Schema(description = "岗位")
    private String dutyPostName;

    @Schema(description = "值班开始时间")
    private LocalDateTime dutyStartTime;

    @Schema(description = "值班结束时间")
    private LocalDateTime dutyEndTime;

    @Schema(description = "值班日志")
    private String dutyLog;

    @Schema(description = "状态（0待值班 1值班中 2已完成 3已取消）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
