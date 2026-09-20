package com.sydigit.yzwater.module.controller.admin.vo.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 问题反馈 - 空间分析河长信息
 */
@Data
public class ProblemFeedbackSpatialAnalysisChiefRespVO {

    @Schema(description = "河长姓名")
    private String headName;

    @Schema(description = "河长级别(字典值)")
    private String headLevel;

    @Schema(description = "河长级别名称")
    private String headLevelLabel;

    @Schema(description = "关联设施类型(river/river_section/reservoir)")
    private String referenceType;

    @Schema(description = "关联设施类型名称")
    private String referenceTypeLabel;

    @Schema(description = "关联设施名称")
    private String referenceName;

    @Schema(description = "行政区划(编码列表)")
    private List<String> administrativeRegion;
}
