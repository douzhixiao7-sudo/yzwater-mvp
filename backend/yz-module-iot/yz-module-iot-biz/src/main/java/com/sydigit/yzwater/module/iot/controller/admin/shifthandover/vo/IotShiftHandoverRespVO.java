package com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交接班响应
 */
@Schema(description = "IoT - 交接班 Response VO")
@Data
public class IotShiftHandoverRespVO {

    @Schema(description = "主键 ID", example = "1")
    private Long id;

    @Schema(description = "交接班编号")
    private String handoverNo;

    @Schema(description = "排班 ID")
    private Long scheduleId;

    @Schema(description = "交接班时间")
    private LocalDateTime handoverTime;

    @Schema(description = "班次 ID")
    private Long shiftId;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "班组 ID")
    private Long teamId;

    @Schema(description = "班组名称")
    private String teamName;

    @Schema(description = "交班人 ID")
    private Long handoverUserId;

    @Schema(description = "交班人")
    private String handoverUserName;

    @Schema(description = "接班人 ID")
    private Long takeoverUserId;

    @Schema(description = "接班人")
    private String takeoverUserName;

    @Schema(description = "值班日志")
    private String dutyLog;

    @Schema(description = "待关注事项")
    private String pendingItems;

    @Schema(description = "关联调度指令ID")
    private Long dispatchInstructionId;

    @Schema(description = "关联调度编号")
    private String dispatchInstructionNo;

    @Schema(description = "关联指令名称")
    private String dispatchInstructionName;

    @Schema(description = "缺陷/两票关联标识（已停用，不再维护）")
    private String defectTicketFlag;

    @Schema(description = "状态（0待交接 1已交接）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

