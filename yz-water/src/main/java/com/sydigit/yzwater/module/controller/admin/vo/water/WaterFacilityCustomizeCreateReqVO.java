package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 自定义图层新增请求
 */
@Schema(description = "仪征管理后台 - 自定义图层新增请求")
@Data
public class WaterFacilityCustomizeCreateReqVO {

    @Schema(description = "设施名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "设施名称不能为空")
    private String facilityName;

    @Schema(description = "行政区域名称")
    private String adminRegion;

    @Schema(description = "行政区域代码（对应 /system/area/tree 的 id）")
    private String adminRegionCode;

    @Schema(description = "管理单位")
    private String manageUnit;

    @Schema(description = "扩展属性（自定义 key/value）")
    private Map<String, Object> attributes;

    @Schema(description = "GeoJSON 几何字符串（仅 geometry，不包含 Feature）")
    private String geometryGeoJson;
}

