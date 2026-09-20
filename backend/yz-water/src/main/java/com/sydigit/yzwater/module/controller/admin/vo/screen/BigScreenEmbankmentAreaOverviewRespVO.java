package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大屏统计 - 提防总览（按行政区划筛选，包含子级）
 */
@Data
public class BigScreenEmbankmentAreaOverviewRespVO {

    @Schema(description = "提防总数", example = "12")
    private Long totalCount;

    @Schema(description = "总堤防长度(m)", example = "12345.67")
    private BigDecimal totalLengthM;

    @Schema(description = "标准长度(m)", example = "23456.78")
    private BigDecimal totalStandardLengthM;

    @Schema(description = "提防列表（不分页）")
    private List<Item> list;

    @Data
    public static class Item {

        @Schema(description = "提防业务表ID", example = "20001")
        private Long embankmentId;

        @Schema(description = "设施基础表ID(用于地图高亮)", example = "10001")
        private Long facilityBaseId;

        @Schema(description = "提防名称", example = "某某提防")
        private String embankmentName;

        @Schema(description = "提防形式(字典值)", example = "soil")
        private String embankmentForm;

        @Schema(description = "经度", example = "119.1845")
        private BigDecimal longitude;

        @Schema(description = "纬度", example = "32.2720")
        private BigDecimal latitude;
    }
}

