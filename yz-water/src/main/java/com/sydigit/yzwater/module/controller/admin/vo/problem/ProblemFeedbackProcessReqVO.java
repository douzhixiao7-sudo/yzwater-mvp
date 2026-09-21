package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 问题处理请求
 */
@Data
public class ProblemFeedbackProcessReqVO {

    @Schema(description = "问题反馈ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问题反馈ID不能为空")
    private Long id;

    @Schema(description = "处理结果（无需处理/已处理）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处理结果不能为空")
    private String handleResult;

    @Schema(description = "处理描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处理描述不能为空")
    private String resolutionDescription;

    @Schema(description = "处理附件（图片/视频）URL 列表")
    private List<String> uploadedFiles;
}
