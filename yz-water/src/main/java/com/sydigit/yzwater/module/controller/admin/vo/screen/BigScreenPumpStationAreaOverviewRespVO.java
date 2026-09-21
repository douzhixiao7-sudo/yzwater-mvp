package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 大屏统计 - 泵站总览（按行政区划筛选，包含子级）
 */
@Data
public class BigScreenPumpStationAreaOverviewRespVO {

    @Schema(description = "泵站总数", example = "12")
    private Long totalCount;

    @Schema(description = "总自排流量(m³/s)", example = "123.45")
    private BigDecimal totalSelfFlow;

    @Schema(description = "总抽引流量(m³/s)", example = "234.56")
    private BigDecimal totalInstalledFlow;

    @Schema(description = "总抽排流量(m³/s)", example = "345.67")
    private BigDecimal totalPumpingFlow;

    @Schema(description = "泵站类型数量统计（key 为 zd_bzlx.value，value 为数量）")
    private Map<String, Long> pumpStationTypeCountMap;

    @Schema(description = "泵站列表（不分页）")
    private List<Item> list;

    @Data
    public static class Item {

        @Schema(description = "泵站业务表ID", example = "20001")
        private Long pumpStationId;

        @Schema(description = "设施基础表ID(用于地图高亮)", example = "10001")
        private Long facilityBaseId;

        @Schema(description = "泵站名称", example = "陈庄泵站")
        private String pumpStationName;

        @Schema(description = "泵站类型(字典值)", example = "supply")
        private String pumpStationType;

        @Schema(description = "工程等别(字典值)", example = "level_1")
        private String engineeringGrade;

        @Schema(description = "经度", example = "119.1845")
        private BigDecimal longitude;

        @Schema(description = "纬度", example = "32.2720")
        private BigDecimal latitude;
    }
}

