package com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 调度管理提交执行反馈 Request VO
 */
@Schema(description = "IoT - 调度管理提交执行反馈 Request VO")
@Data
public class IotDispatchManageSubmitResultReqVO {

    @Schema(description = "调度指令 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "调度指令不能为空")
    private Long id;

    @Schema(description = "反馈内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "反馈内容不能为空")
    private String feedbackContent;

    @Schema(description = "反馈备注")
    private String feedbackRemark;
}

