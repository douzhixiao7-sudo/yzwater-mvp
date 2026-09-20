package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 大屏统计 - 水库总览（按行政区划筛选，包含子级）
 */
@Data
public class BigScreenReservoirAreaOverviewRespVO {

    @Schema(description = "水库总数", example = "12")
    private Long totalCount;

    @Schema(description = "总库容(m3)", example = "1888888.88")
    private BigDecimal totalCapacity;

    @Schema(description = "总坝顶长度(m)", example = "1234.56")
    private BigDecimal totalDamTopLength;

    @Schema(description = "总兴利库容(m3)", example = "888888.88")
    private BigDecimal totalActiveCapacity;

    @Schema(description = "水库规模数量统计（key 为 zd_skgm.value，value 为数量）")
    private Map<String, Long> reservoirScaleCountMap;

    @Schema(description = "水库列表（不分页）")
    private List<Item> list;

    @Data
    public static class Item {

        @Schema(description = "水库业务表ID", example = "20001")
        private Long reservoirId;

        @Schema(description = "设施基础表ID(用于地图高亮)", example = "10001")
        private Long facilityBaseId;

        @Schema(description = "水库名称", example = "月塘水库")
        private String reservoirName;

        @Schema(description = "水库规模(字典值)", example = "small_1")
        private String reservoirScale;

        @Schema(description = "总库容(m3)", example = "178000.00")
        private BigDecimal totalCapacity;

        @Schema(description = "中心点经度", example = "119.1845")
        private BigDecimal longitude;

        @Schema(description = "中心点纬度", example = "32.2720")
        private BigDecimal latitude;

        @Schema(description = "GeoJSON 字符串（仅 geometry，用于地图展示与点击）")
        private String geometryGeoJson;
    }
}

