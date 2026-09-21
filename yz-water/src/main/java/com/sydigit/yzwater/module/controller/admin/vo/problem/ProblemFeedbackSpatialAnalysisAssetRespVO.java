package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 问题反馈 - 空间分析水利资产统计
 */
@Data
public class ProblemFeedbackSpatialAnalysisAssetRespVO {

    @Schema(description = "涉及河道条数")
    private Long riverCount;

    @Schema(description = "涉及水库条数")
    private Long reservoirCount;

    @Schema(description = "涉及提防数")
    private Long embankmentCount;

    @Schema(description = "涉及泵站数")
    private Long pumpStationCount;

    @Schema(description = "涉及灌区数")
    private Long irrigationDistrictCount;

    @Schema(description = "涉及防汛物资个数")
    private Long floodMaterialCount;
}
