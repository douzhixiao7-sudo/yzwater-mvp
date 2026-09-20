package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大屏统计 - 灌区总览（按行政区划筛选，包含子级）
 */
@Data
public class BigScreenIrrigationDistrictAreaOverviewRespVO {

    @Schema(description = "灌区总数", example = "12")
    private Long totalCount;

    @Schema(description = "总实际空间面积(km²)", example = "123.45")
    private BigDecimal totalActualIrrigableArea;

    @Schema(description = "总实际基本农田面积(km²)", example = "67.89")
    private BigDecimal totalBasicFarmlandAreaKm2;

    @Schema(description = "总干渠长度(m)", example = "12345.67")
    private BigDecimal totalMainCanalLengthM;

    @Schema(description = "灌区列表（不分页）")
    private List<Item> list;

    @Data
    public static class Item {

        @Schema(description = "灌区业务表ID", example = "20001")
        private Long irrigationDistrictId;

        @Schema(description = "设施基础表ID(用于地图高亮)", example = "10001")
        private Long facilityBaseId;

        @Schema(description = "灌区名称", example = "灌区1")
        private String irrigationDistrictName;

        @Schema(description = "实际空间面积(k㎡)", example = "12.34")
        private BigDecimal actualIrrigableArea;

        @Schema(description = "实际基本农田面积(k㎡)", example = "5.67")
        private BigDecimal basicFarmlandAreaKm2;

        @Schema(description = "GeoJSON 字符串（仅 geometry，用于地图展示与点击）")
        private String geometryGeoJson;
    }
}

