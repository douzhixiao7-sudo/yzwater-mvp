package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大屏统计 - 问题反馈关联设施数量
 */
@Schema(description = "大屏统计 - 问题反馈关联设施数量")
@Data
public class BigScreenProblemFeedbackReferenceCountRespVO {

    @Schema(description = "设施类型（river/river_section/reservoir）")
    private String referenceType;

    @Schema(description = "设施ID")
    private Long referenceId;

    @Schema(description = "河道名称")
    private String riverName;

    @Schema(description = "河段名称")
    private String riverSectionName;

    @Schema(description = "水库名称")
    private String reservoirName;

    @Schema(description = "问题数量")
    private Long count;
}
