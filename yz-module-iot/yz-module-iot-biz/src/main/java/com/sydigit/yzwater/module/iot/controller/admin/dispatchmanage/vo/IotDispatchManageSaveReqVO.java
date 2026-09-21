package com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 调度管理新增/编辑 Request VO
 */
@Schema(description = "IoT - 调度管理新增/编辑 Request VO")
@Data
public class IotDispatchManageSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）", example = "1024")
    private Long id;

    @Schema(description = "发令单位", example = "运行调度中心")
    private String issueOrgName;

    @Schema(description = "指令名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "泵站夜间排涝指令")
    @NotEmpty(message = "指令名称不能为空")
    private String instructionName;

    @Schema(description = "发令人", example = "张三")
    private String issueUserName;

    @Schema(description = "所属站点", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotBlank(message = "所属站点不能为空")
    private String stationId;

    @Schema(description = "调度内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "调度内容不能为空")
    private String instructionContent;

    @Schema(description = "调度方案 ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "调度方案不能为空")
    private List<Long> planIds;

    @Schema(description = "计划完成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划完成时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime plannedFinishTime;

    @Schema(description = "接收单位 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "接收单位不能为空")
    private Long receiverDeptId;

    @Schema(description = "接收人 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "接收人不能为空")
    private Long receiverUserId;

    @Schema(description = "执行人 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "执行人不能为空")
    private Long executorUserId;

    @Schema(description = "操作票查看链接")
    private String operationTicketUrl;

    @Schema(description = "备注")
    private String remark;
}

