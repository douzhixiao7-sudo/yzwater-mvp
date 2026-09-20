package com.sydigit.yzwater.module.controller.app.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手机端-问题反馈缓冲区查询响应
 */
@Schema(description = "手机端-问题反馈缓冲区查询响应")
@Data
public class AppProblemFeedbackBufferQueryRespVO {

    @Schema(description = "中心点经度")
    private BigDecimal longitude;

    @Schema(description = "中心点纬度")
    private BigDecimal latitude;

    @Schema(description = "查询半径（米）")
    private BigDecimal radiusM;

    @Schema(description = "河道列表")
    private List<AppProblemFeedbackBufferQueryRiverRespVO> rivers;

    @Schema(description = "河段列表")
    private List<AppProblemFeedbackBufferQuerySectionRespVO> riverSections;

    @Schema(description = "水库列表")
    private List<AppProblemFeedbackBufferQueryReservoirRespVO> reservoirs;
}
