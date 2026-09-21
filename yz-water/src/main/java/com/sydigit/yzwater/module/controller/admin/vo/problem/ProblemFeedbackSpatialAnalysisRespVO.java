package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题反馈 - 空间分析响应
 */
@Data
public class ProblemFeedbackSpatialAnalysisRespVO {

    @Schema(description = "中心点经度")
    private BigDecimal longitude;

    @Schema(description = "中心点纬度")
    private BigDecimal latitude;

    @Schema(description = "分析半径(米)")
    private BigDecimal radiusM;

    @Schema(description = "统计时间")
    private LocalDateTime statsTime;

    @Schema(description = "问题类型统计")
    private List<ProblemFeedbackSpatialAnalysisTypeStatRespVO> problemTypeStats;

    @Schema(description = "问题状态统计")
    private ProblemFeedbackSpatialAnalysisStatusStatRespVO statusStat;

    @Schema(description = "水利资产统计")
    private ProblemFeedbackSpatialAnalysisAssetRespVO assetStats;

    @Schema(description = "管理责任(河长列表)")
    private List<ProblemFeedbackSpatialAnalysisChiefRespVO> managementList;
}
