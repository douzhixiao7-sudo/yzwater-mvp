package com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.sydigit.yzwater.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 调令接受结果提交 Request VO
 */
@Schema(description = "IoT - 调令接受结果提交 Request VO")
@Data
public class IotDispatchReceiveSubmitReqVO {

    @Schema(description = "调令 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "调令ID不能为空")
    private Long id;

    @Schema(description = "执行情况（1已完成 0未完成）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "执行情况不能为空")
    private Integer executeFlag;

    @Schema(description = "完成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "完成时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime finishTime;

    @Schema(description = "附件 URL 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "附件不能为空")
    private List<String> attachments;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "运行日志 ID 列表")
    private List<Long> runLogIds;
}
