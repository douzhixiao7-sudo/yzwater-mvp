package com.sydigit.yzwater.module.iot.controller.admin.runlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 运行日志新增/编辑请求
 */
@Schema(description = "IoT - 运行日志新增/编辑 Request VO")
@Data
public class IotRunLogSaveReqVO {

    @Schema(description = "主键ID（编辑时必填）", example = "1024")
    private Long id;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "值班班组ID")
    private Long dutyTeamId;

    @Schema(description = "值班班组名称")
    private String dutyTeamName;

    @Schema(description = "记录人（允许手动修改）")
    private String recorderUserName;

    @Schema(description = "记录时间（允许手动修改）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime recordTime;

    @Schema(description = "运行开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "运行开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime runStartTime;

    @Schema(description = "运行结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "运行结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime runEndTime;

    @Schema(description = "检查时段")
    private String checkPeriod;

    @Schema(description = "所属站点")
    private String stationId;

    @Schema(description = "巡检类型")
    private String inspectionType;

    @Schema(description = "巡检标准ID")
    private Long inspectionStandardId;

    @Schema(description = "巡检标准名称（前端可不传，后端会按标准ID回填快照）")
    private String inspectionStandardName;

    @Schema(description = "巡检结果明细")
    private List<IotRunLogInspectionResultItemVO> inspectionResultItems;

    @Schema(description = "是否自动生成故障记录（仅新增场景生效）")
    private Boolean autoCreateFaultRecords;

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

    @Schema(description = "附件列表")
    private List<String> attachments;

    @Schema(description = "备注")
    private String remark;
}
