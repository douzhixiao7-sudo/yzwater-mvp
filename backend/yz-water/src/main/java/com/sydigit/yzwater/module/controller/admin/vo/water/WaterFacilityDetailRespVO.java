package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 水利设施详情返回
 */
@Schema(description = "仪征管理后台 - 水利设施详情返回")
@Data
public class WaterFacilityDetailRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "设施编码")
    private String facilityCode;

    @Schema(description = "设施名称")
    private String facilityName;

    @Schema(description = "设施类别标签")
    private String facilityType;

    @Schema(description = "设施类别字典值")
    private String facilityTypeValue;

    @Schema(description = "行政区域名称")
    private String adminRegion;

    @Schema(description = "行政区域编码")
    private String adminRegionCode;

    @Schema(description = "管理单位")
    private String manageUnit;

    @Schema(description = "数据来源")
    private String sourceType;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "扩展属性")
    private Map<String, Object> attributes;

    @Schema(description = "GeoJSON 字符串")
    private String geometryGeoJson;

    @Schema(description = "几何类型")
    private String geomType;

    @Schema(description = "SRID")
    private Integer srid;
}
