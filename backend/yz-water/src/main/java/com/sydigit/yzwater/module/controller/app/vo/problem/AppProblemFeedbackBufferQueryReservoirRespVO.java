package com.sydigit.yzwater.module.controller.app.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 手机端-问题反馈缓冲区水库结果
 */
@Schema(description = "手机端-问题反馈缓冲区水库结果")
@Data
public class AppProblemFeedbackBufferQueryReservoirRespVO {

    @Schema(description = "水库ID")
    private Long id;

    @Schema(description = "水库名称")
    private String reservoirName;

    @Schema(description = "几何类型")
    private String geomType;

    @Schema(description = "几何WKT（附带SRID前缀）")
    private String geomWkt;

    @Schema(description = "距离中心点距离（米）")
    private Double distanceM;
}
