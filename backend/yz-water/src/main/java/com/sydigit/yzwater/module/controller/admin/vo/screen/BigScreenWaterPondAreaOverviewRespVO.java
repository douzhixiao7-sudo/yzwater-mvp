package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大屏统计 - 坑塘总览（按行政区划筛选，包含子级）
 */
@Data
public class BigScreenWaterPondAreaOverviewRespVO {

    @Schema(description = "坑塘总数")
    private Long totalCount;

    @Schema(description = "实测面积合计(㎡)")
    private BigDecimal totalAreaSqm;

    @Schema(description = "面积合计(亩)")
    private BigDecimal totalAreaMu;

    @Schema(description = "坑塘列表（不分页，含面几何 GeoJSON）")
    private List<Item> list;

    @Data
    public static class Item {

        @Schema(description = "坑塘业务表 ID")
        private Long pondId;

        @Schema(description = "设施基础表 ID（用于地图高亮）")
        private Long facilityBaseId;

        @Schema(description = "资源名称")
        private String resourceName;

        @Schema(description = "资源编号")
        private String resourceCode;

        @Schema(description = "土地权属")
        private String ownershipType;

        @Schema(description = "资源类型")
        private String resourceType;

        @Schema(description = "使用状态")
        private String usageStatus;

        @Schema(description = "实测面积(㎡)")
        private BigDecimal areaSqm;

        @Schema(description = "面积(亩)")
        private BigDecimal areaMu;

        @Schema(description = "中心点经度")
        private BigDecimal longitude;

        @Schema(description = "中心点纬度")
        private BigDecimal latitude;

        @Schema(description = "GeoJSON 字符串（仅 geometry，用于地图展示与点击）")
        private String geometryGeoJson;
    }
}
