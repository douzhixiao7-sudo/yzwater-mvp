package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大屏统计 - 河道模糊查询返回
 */
@Data
public class BigScreenRiverChannelSearchRespVO {

    @Schema(description = "河道主键ID（yz_river_channel.id）")
    private Long id;

    @Schema(description = "关联基础表主键ID（yz_water_facility_base.id）")
    private Long facilityId;

    @Schema(description = "河道名称")
    private String riverName;

    @Schema(description = "河流级别（zd_hljb 的中文标签）")
    private String riverLevel;
}

