package com.sydigit.yzwater.module.controller.app.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 手机端-问题反馈缓冲区河段结果
 */
@Schema(description = "手机端-问题反馈缓冲区河段结果")
@Data
public class AppProblemFeedbackBufferQuerySectionRespVO {

    @Schema(description = "河段ID")
    private Long id;

    @Schema(description = "河道ID")
    private Long riverChannelId;

    @Schema(description = "河段名称")
    private String sectionName;

    @Schema(description = "展示名称")
    private String displayName;

    @Schema(description = "几何类型")
    private String geomType;

    @Schema(description = "几何WKT（附带SRID前缀）")
    private String geomWkt;

    @Schema(description = "距离中心点距离（米）")
    private Double distanceM;
}
