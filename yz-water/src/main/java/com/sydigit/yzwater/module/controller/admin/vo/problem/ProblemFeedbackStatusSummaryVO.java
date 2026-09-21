package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问题状态汇总返回
 */
@Data
public class ProblemFeedbackStatusSummaryVO {

    @Schema(description = "全部问题数量")
    private Long totalCount;

    @Schema(description = "处理中数量")
    private Long processingCount;

    @Schema(description = "待核验数量")
    private Long pendingVerifyCount;

    @Schema(description = "已办结数量")
    private Long finishedCount;
}
