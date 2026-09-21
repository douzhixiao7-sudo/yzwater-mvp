package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大屏统计 - 设施几何查询返回
 */
@Data
public class BigScreenFacilityGeomRespVO {

    @Schema(description = "设施类别（zd_sslb 的 value，例如 river、river_section 等）")
    private String facilityType;

    @Schema(description = "基础表主键ID（yz_water_facility_base.id）")
    private Long facilityBaseId;

    @Schema(description = "设备主键ID（对应各设施业务表主键）")
    private Long deviceId;

    @Schema(description = "河流级别(字典: zd_hljb)")
    private String riverLevel;


    @Schema(description = "设备名称（对应各设施业务表名称字段）")
    private String deviceName;

    @Schema(description = "几何数据 WKT（来自 yz_water_facility_base.geom）")
    private String geomWkt;

    @Schema(description = "是否存在待办问题（通过公示牌扫码反馈，status != 4）")
    private Boolean hasTodoProblem;
}
