package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 首页地图 - 水利设施点位（用于展示与定位）
 */
@Data
public class WaterFacilityMapItemRespVO {

    @Schema(description = "设施基础表主键", example = "10001")
    private Long id;

    @Schema(description = "设施编码", example = "HD-0001")
    private String facilityCode;

    @Schema(description = "设施名称", example = "香蒲河")
    private String facilityName;

    @Schema(description = "设施类别（字典值：zd_sslb.value）", example = "river")
    private String facilityType;

    @Schema(description = "行政区划编码（对应 /system/area/tree 的 id）", example = "321082")
    private String adminRegionCode;

    @Schema(description = "点位经度（从几何质心计算）", example = "119.1845")
    private BigDecimal longitude;

    @Schema(description = "点位纬度（从几何质心计算）", example = "32.2720")
    private BigDecimal latitude;

    @Schema(description = "几何类型（POINT/LINESTRING/POLYGON 等）", example = "POINT")
    private String geomType;
}

