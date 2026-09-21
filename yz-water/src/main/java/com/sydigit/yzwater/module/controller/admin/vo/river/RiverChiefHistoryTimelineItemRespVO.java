package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史河长时间轴条目（只读展示）
 */
@Schema(description = "仪征管理后台 - 历史河长时间轴条目 Response VO")
@Data
public class RiverChiefHistoryTimelineItemRespVO {

    @Schema(description = "河长记录ID")
    private Long id;

    @Schema(description = "河长姓名")
    private String headName;

    @Schema(description = "河长级别（字典值）")
    private String headLevel;

    @Schema(description = "河长级别名称")
    private String headLevelLabel;

    @Schema(description = "河长职务")
    private String headPosition;

    @Schema(description = "河长工作单位")
    private String headUnit;

    @Schema(description = "河长联系电话")
    private String headContact;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveFrom;

    @Schema(description = "失效时间（当前版本为空）")
    private LocalDateTime effectiveTo;

    @Schema(description = "关联设施类型（river：河道；river_section：河段；reservoir：水库）")
    private String referenceType;

    @Schema(description = "关联设施类型名称")
    private String referenceTypeLabel;

    @Schema(description = "关联设施ID")
    private Long referenceId;

    @Schema(description = "关联设施名称")
    private String referenceName;

    @Schema(description = "行政区划（编码列表，可为空）")
    private List<String> administrativeRegion;
}

