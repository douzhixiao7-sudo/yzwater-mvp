package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 问题核验请求
 */
@Data
public class ProblemFeedbackVerifyReqVO {

    @Schema(description = "问题反馈ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问题反馈ID不能为空")
    private Long id;

    @Schema(description = "核验结论（true=问题已解决，false=问题驳回）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "核验结论不能为空")
    private Boolean solved;

    @Schema(description = "核验结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "核验结果不能为空")
    private String verificationResult;

    @Schema(description = "核验附件（图片）URL 列表")
    private List<String> uploadedFiles;
}
