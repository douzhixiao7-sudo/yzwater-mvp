package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "仪征管理后台 - 河长管理分页 Response VO")
@Data
public class RiverChiefManagementPageRespVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "河长名称")
    private String headName;

    @Schema(description = "河长级别（字典：zd_hzjb）")
    private String headLevel;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveFrom;

    @Schema(description = "河长联系电话")
    private String headContact;

    @Schema(description = "行政区划（可为空，多选）")
    private List<String> administrativeRegion;

    @Schema(description = "关联设施类型（river：河道；river_section：河段；reservoir：水库）")
    private String referenceType;

    @Schema(description = "关联设施类型名称（河道/河段/水库）")
    private String referenceTypeLabel;

    @Schema(description = "关联设施名称（河道名称/河段名称/水库名称）")
    private String referenceName;
}
