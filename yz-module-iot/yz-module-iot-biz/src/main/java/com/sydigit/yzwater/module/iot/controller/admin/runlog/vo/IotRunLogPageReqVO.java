package com.sydigit.yzwater.module.iot.controller.admin.runlog.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 运行日志分页请求
 */
@Schema(description = "IoT - 运行日志分页 Request VO")
@Data
public class IotRunLogPageReqVO extends PageParam {

    @Schema(description = "记录编号")
    private String logNo;

    @Schema(description = "值班班组")
    private String dutyTeamName;

    @Schema(description = "检查时段")
    private String checkPeriod;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "关联调度指令ID")
    private Long dispatchInstructionId;

    @Schema(description = "关联任务关键字（调令编号/任务名称）")
    private String dispatchKeyword;

    @Schema(description = "运行时段")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] runTime;
}
