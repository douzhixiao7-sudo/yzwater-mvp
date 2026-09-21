package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 水利设施几何更新请求
 */
@Schema(description = "仪征管理后台 - 水利设施几何更新请求")
@Data
public class WaterFacilityGeometryUpdateReqVO {

    @Schema(description = "GeoJSON 几何字符串（仅 geometry，不包含 Feature）")
    private String geometryGeoJson;

    @Schema(description = "SRID（为空则默认 4490）")
    private Integer srid;
}

