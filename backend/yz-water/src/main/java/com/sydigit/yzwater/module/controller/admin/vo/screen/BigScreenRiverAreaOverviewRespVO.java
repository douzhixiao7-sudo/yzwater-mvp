package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 大屏统计 - 河道总览（不分页，统计全量河道）
 */
@Data
public class BigScreenRiverAreaOverviewRespVO {

    @Schema(description = "河道总数", example = "12")
    private Long totalCount;

    @Schema(description = "总流域面积(km²)", example = "188.88")
    private BigDecimal totalCatchmentKm2;

    @Schema(description = "总长度(km)", example = "1888.8")
    private BigDecimal totalLengthKm;

    @Schema(description = "河道级别数量统计（key 为 zd_hljb.value，value 为数量）")
    private Map<String, Long> riverLevelCountMap;

    @Schema(description = "河道列表（不分页）")
    private List<Item> list;

    @Data
    public static class Item {

        @Schema(description = "河道业务表ID", example = "20001")
        private Long riverId;

        @Schema(description = "设施基础表ID(用于地图高亮)", example = "10001")
        private Long facilityBaseId;

        @Schema(description = "河道名称", example = "香蒲河")
        private String riverName;

        @Schema(description = "河道级别(字典标签)", example = "6级")
        private String riverLevelLabel;

        @Schema(description = "河道长度(km)", example = "17.8")
        private BigDecimal lengthKm;

        @Schema(description = "中心点经度", example = "119.1845")
        private BigDecimal longitude;

        @Schema(description = "中心点纬度", example = "32.2720")
        private BigDecimal latitude;
    }
}
