package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 河道页 - 河段河长分组
 */
@Data
@Schema(description = "仪征管理后台 - 河段河长分组")
public class RiverChiefOverviewSectionVO {

    @Schema(description = "河段ID")
    private Long sectionId;

    @Schema(description = "河段名称")
    private String sectionName;

    @Schema(description = "河段河长列表")
    private List<RiverChiefOverviewChiefVO> chiefs;
}
