package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问题反馈 - 空间分析问题状态统计
 */
@Data
public class ProblemFeedbackSpatialAnalysisStatusStatRespVO {

    @Schema(description = "待受理数量（状态0）")
    private Long pendingCount;

    @Schema(description = "处理中数量（状态2）")
    private Long processingCount;

    @Schema(description = "待核验数量（状态3）")
    private Long pendingVerifyCount;

    @Schema(description = "已办结数量（状态4）")
    private Long finishedCount;
}
