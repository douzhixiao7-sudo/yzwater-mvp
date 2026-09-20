package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大屏统计 - 公示牌总览（按行政区划筛选，包含子级）
 */
@Data
public class BigScreenSignboardAreaOverviewRespVO {

    @Schema(description = "河道总数", example = "12")
    private Long riverCount;

    @Schema(description = "水库总数", example = "8")
    private Long reservoirCount;

    @Schema(description = "公示牌总数", example = "20")
    private Long totalCount;

    @Schema(description = "问题总数", example = "88")
    private Long problemTotalCount;

    @Schema(description = "公示牌列表（不分页）")
    private List<Item> list;

    @Data
    public static class Item {

        @Schema(description = "公示牌ID", example = "10001")
        private Long signboardId;

        @Schema(description = "公示牌名称", example = "公示牌1")
        private String signboardName;

        @Schema(description = "公示牌类型(字典值)", example = "type_a")
        private String signboardType;

        @Schema(description = "公示牌类型(字典标签)", example = "河道公示牌")
        private String signboardTypeLabel;

        @Schema(description = "关联对象类型（river：河道；river_section：河段；reservoir：水库）", example = "river")
        private String referenceType;

        @Schema(description = "关联对象类型(中文)", example = "河道")
        private String referenceTypeLabel;

        @Schema(description = "关联对象ID", example = "20001")
        private Long referenceId;

        @Schema(description = "关联对象名称", example = "仪征河道")
        private String referenceName;

        @Schema(description = "具体位置", example = "仪征市某某路")
        private String specificLocation;

        @Schema(description = "维护单位(中文展示，多个用顿号分隔)", example = "仪征市水利局、某某单位")
        private String maintenanceUnit;

        @Schema(description = "经度", example = "119.1845")
        private BigDecimal longitude;

        @Schema(description = "纬度", example = "32.2720")
        private BigDecimal latitude;
    }
}

