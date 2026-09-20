package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 河道页 - 河长概览响应
 */
@Data
@Schema(description = "仪征管理后台 - 河道页河长概览响应")
public class RiverChiefOverviewRespVO {

    @Schema(description = "河道ID")
    private Long riverId;

    @Schema(description = "河道名称")
    private String riverName;

    @Schema(description = "河道直属河长")
    private List<RiverChiefOverviewChiefVO> riverChiefs;

    @Schema(description = "河段河长分组")
    private List<RiverChiefOverviewSectionVO> sectionChiefGroups;

    @Schema(description = "当前有效河长总数")
    private Integer totalCount;
}
