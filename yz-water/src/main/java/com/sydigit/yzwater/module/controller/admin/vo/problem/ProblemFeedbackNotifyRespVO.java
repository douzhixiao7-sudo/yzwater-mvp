package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 问题反馈短信通知 Response VO")
@Data
public class ProblemFeedbackNotifyRespVO {

    @Schema(description = "反馈人短信是否发送成功")
    private Boolean publicSent;

    @Schema(description = "反馈人短信发送日志ID")
    private Long publicLogId;

    @Schema(description = "反馈人短信未发送原因（例如：手机号为空）")
    private String publicReason;

    @Schema(description = "处理人短信是否发送成功")
    private Boolean handlerSent;

    @Schema(description = "处理人短信发送日志ID")
    private Long handlerLogId;

    @Schema(description = "处理人短信未发送原因（例如：未指派处理人/手机号为空）")
    private String handlerReason;

}

