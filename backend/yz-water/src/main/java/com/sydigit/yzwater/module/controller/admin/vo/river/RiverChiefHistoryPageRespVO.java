package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史河长信息分页结果（按设施聚合）
 */
@Schema(description = "仪征管理后台 - 历史河长信息分页 Response VO")
@Data
public class RiverChiefHistoryPageRespVO {

    @Schema(description = "关联设施类型（river：河道；river_section：河段；reservoir：水库）")
    private String referenceType;

    @Schema(description = "关联设施类型名称")
    private String referenceTypeLabel;

    @Schema(description = "关联设施ID")
    private Long referenceId;

    @Schema(description = "关联设施名称（河道/河段/水库名称）")
    private String referenceName;

    @Schema(description = "当前河长姓名（可能多个，用“、”分隔）")
    private String currentHeadNames;

    @Schema(description = "河长级别（字典值，可能多个）")
    private String headLevel;

    @Schema(description = "河长级别名称（可能多个，用“、”分隔）")
    private String headLevelLabel;

    @Schema(description = "当前版本生效时间")
    private LocalDateTime effectiveFrom;

    @Schema(description = "行政区划（编码列表，可为空）")
    private List<String> administrativeRegion;
}

