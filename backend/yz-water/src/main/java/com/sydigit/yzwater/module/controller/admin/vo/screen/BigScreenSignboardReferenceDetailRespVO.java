package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大屏统计 - 公示牌关联设施详情返回
 */
@Data
public class BigScreenSignboardReferenceDetailRespVO {

    @Schema(description = "关联对象类型（river：河道；river_section：河段；reservoir：水库）")
    private String referenceType;

    @Schema(description = "关联对象ID（对应各设施业务表主键）")
    private Long referenceId;

    @Schema(description = "设施名称")
    private String name;

    @Schema(description = "几何数据 WKT（来自 yz_water_facility_base.geom）")
    private String geomWkt;
}

