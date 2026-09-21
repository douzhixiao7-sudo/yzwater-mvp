package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问题反馈 - 空间分析问题类型统计
 */
@Data
public class ProblemFeedbackSpatialAnalysisTypeStatRespVO {

    @Schema(description = "问题类型(字典值)")
    private String feedbackType;

    @Schema(description = "问题类型名称")
    private String feedbackTypeLabel;

    @Schema(description = "问题数量")
    private Long count;
}
