package com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 交接班新增/编辑请求
 */
@Schema(description = "IoT - 交接班新增/编辑 Request VO")
@Data
public class IotShiftHandoverSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）", example = "1024")
    private Long id;

    @Schema(description = "排班 ID（交班对应排班）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "排班 ID 不能为空")
    private Long scheduleId;

    @Schema(description = "交班人用户 ID（为空时按排班自动匹配）", example = "1")
    private Long handoverUserId;

    @Schema(description = "接班人用户 ID（为空时按下一班次自动匹配）", example = "2")
    private Long takeoverUserId;

    @Schema(description = "交接班时间（为空时默认当前时间）", example = "2026-03-04 08:00:00")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime handoverTime;

    @Schema(description = "值班日志", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "值班日志不能为空")
    private String dutyLog;

    @Schema(description = "待关注事项", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "待关注事项不能为空")
    private String pendingItems;

    @Schema(description = "关联调度指令ID")
    private Long dispatchInstructionId;

    @Schema(description = "缺陷/两票关联标识（已停用，不再维护）")
    private String defectTicketFlag;

    @Schema(description = "备注")
    private String remark;
}

