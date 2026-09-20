package com.sydigit.yzwater.module.iot.controller.admin.runlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运行日志响应
 */
@Schema(description = "IoT - 运行日志 Response VO")
@Data
public class IotRunLogRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "记录编号")
    private String logNo;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "值班班组ID")
    private Long dutyTeamId;

    @Schema(description = "值班班组")
    private String dutyTeamName;

    @Schema(description = "记录人用户ID")
    private Long recorderUserId;

    @Schema(description = "记录人")
    private String recorderUserName;

    @Schema(description = "记录时间")
    private LocalDateTime recordTime;

    @Schema(description = "运行开始时间")
    private LocalDateTime runStartTime;

    @Schema(description = "运行结束时间")
    private LocalDateTime runEndTime;

    @Schema(description = "检查时段")
    private String checkPeriod;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "巡检标准ID")
    private Long inspectionStandardId;

    @Schema(description = "巡检标准名称")
    private String inspectionStandardName;

    @Schema(description = "巡检结果明细")
    private List<IotRunLogInspectionResultItemVO> inspectionResultItems;

    @Schema(description = "设备名称（兼容历史字段）")
    private String deviceName;

    @Schema(description = "设备名称列表（兼容历史字段）")
    private List<String> deviceNames;

    @Schema(description = "运行参数（兼容历史字段）")
    private String runParamsText;

    @Schema(description = "事件描述（兼容历史字段）")
    private String eventDesc;

    @Schema(description = "关联调度指令ID")
    private Long dispatchInstructionId;

    @Schema(description = "关联调令编号")
    private String dispatchInstructionNo;

    @Schema(description = "关联调令名称")
    private String dispatchInstructionName;

    @Schema(description = "附件")
    private List<String> attachments;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

