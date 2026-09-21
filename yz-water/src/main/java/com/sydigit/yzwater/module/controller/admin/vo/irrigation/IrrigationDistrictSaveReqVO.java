package com.sydigit.yzwater.module.controller.admin.vo.irrigation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 灌区新增/编辑入参
 */
@Data
@Schema(description = "管理后台 - 灌区新增/编辑 Request VO")
public class IrrigationDistrictSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private Long id;

    @Schema(description = "关联基础表 yz_water_facility_base.id（系统自动维护）")
    private Long facilityId;

    @Schema(description = "GIS几何数据（GeoJSON，仅 geometry，不包含 Feature）")
    private String geometryGeoJson;

    @Schema(description = "GIS几何类型（POINT/LINESTRING/POLYGON 等，返回值字段）")
    private String geomType;

    @Schema(description = "GIS坐标系 SRID（为空默认 4490）")
    private Integer srid;

    @Schema(description = "灌区编码（系统自动维护，前端无需传入）")
    private String irrigationDistrictCode;

    @Schema(description = "灌区名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "灌区名称不能为空")
    private String irrigationDistrictName;

    @Schema(description = "灌区图片（text[]）")
    private List<String> irrigationDistrictImages;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "所在流域")
    private String basinCode;

    @Schema(description = "行政区划（text[]）")
    private List<String> divisionCode;

    @Schema(description = "设计灌溉面积（万亩）")
    private BigDecimal designIrrigationArea;

    @Schema(description = "实际可灌面积")
    private BigDecimal actualIrrigableArea;

    @Schema(description = "实际基本农田面积(k㎡)")
    private BigDecimal basicFarmlandAreaKm2;

    @Schema(description = "是否生态红线(0 否 1 是)")
    private Integer isEcologicalRedLine;

    @Schema(description = "是否开发边界(0 否 1 是)")
    private Integer isDevelopmentBoundary;

    @Schema(description = "干渠长度(单位 m)")
    private BigDecimal mainCanalLengthM;

    @Schema(description = "负责人")
    private String leaderName;

    @Schema(description = "联系电话")
    private String leaderPhone;

    @Schema(description = "管理单位（text[]，字典：zd_gldw）")
    private List<String> managementUnit;

    @Schema(description = "灌区类型（字典：zd_gqlx）")
    private String irrigationDistrictType;
}
