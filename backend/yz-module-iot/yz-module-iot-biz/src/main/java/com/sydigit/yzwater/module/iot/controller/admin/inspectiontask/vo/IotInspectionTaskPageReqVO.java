package com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo;

import com.sydigit.yzwater.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "IoT - 巡检任务分页 Request VO")
@Data
public class IotInspectionTaskPageReqVO extends PageParam {

    @Schema(description = "任务编号")
    private String taskNo;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "任务状态")
    private Integer taskStatus;

    @Schema(description = "任务来源（1计划生成 2人工创建）")
    private Integer sourceType;

    @Schema(description = "流程状态（0未发起 1进行中 2已结束）")
    private Integer workflowStatus;

    @Schema(description = "所属闸站")
    private String stationId;

    @Schema(description = "执行人用户 ID")
    private Long executorUserId;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] planStartTime;

    @Schema(description = "计划结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] planEndTime;

}
