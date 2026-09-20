package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 问题反馈短信通知 Request VO")
@Data
public class ProblemFeedbackNotifyReqVO {

    @Schema(description = "问题反馈ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "问题反馈ID不能为空")
    private Long id;

}

