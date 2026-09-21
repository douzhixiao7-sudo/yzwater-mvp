package com.sydigit.yzwater.module.controller.admin.vo.pond;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "坑塘统计总览")
@Data
public class WaterPondStatsRespVO {

    @Schema(description = "坑塘总数")
    private Long totalCount = 0L;

    @Schema(description = "实测面积合计（㎡）")
    private BigDecimal totalAreaSqm = BigDecimal.ZERO;

    @Schema(description = "面积合计（亩）")
    private BigDecimal totalAreaMu = BigDecimal.ZERO;

    @Schema(description = "覆盖镇/街道数")
    private Long townCount = 0L;

    @Schema(description = "覆盖村/社区数")
    private Long villageCount = 0L;

    @Schema(description = "按镇统计（数量降序）")
    private List<NameCountItem> townStats = new ArrayList<>();

    @Schema(description = "按村统计 TOP")
    private List<NameCountItem> villageStats = new ArrayList<>();

    @Schema(description = "按土地权属统计")
    private List<NameCountItem> ownershipStats = new ArrayList<>();

    @Schema(description = "按使用状态统计")
    private List<NameCountItem> usageStatusStats = new ArrayList<>();

    @Schema(description = "按资源类型统计")
    private List<NameCountItem> resourceTypeStats = new ArrayList<>();

    @Data
    @Schema(description = "名称-数量-面积项")
    public static class NameCountItem {
        @Schema(description = "区划编码（镇/村统计时有值）")
        private String code;
        @Schema(description = "名称")
        private String name;
        @Schema(description = "数量")
        private Long count = 0L;
        @Schema(description = "实测面积合计（㎡）")
        private BigDecimal areaSqm = BigDecimal.ZERO;
    }
}
