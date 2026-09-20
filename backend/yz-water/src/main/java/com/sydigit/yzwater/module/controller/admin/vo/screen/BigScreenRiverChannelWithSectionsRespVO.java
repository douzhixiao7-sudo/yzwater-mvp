package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大屏统计 - 河道列表（包含河段名称）
 */
@Data
public class BigScreenRiverChannelWithSectionsRespVO {

    @Schema(description = "河道主键ID")
    private Long id;

    @Schema(description = "河道名称")
    private String riverName;

    @Schema(description = "河道级别（字典 zd_hljb 的 value）")
    private String riverLevel;

    @Schema(description = "河道级别（字典标签）")
    private String riverLevelLabel;

    @Schema(description = "河道长度(km)")
    private BigDecimal lengthKm;

    @Schema(description = "河段名称列表（河道未划分河段时为空列表）")
    private List<String> sectionNames;

    @Schema(description = "关联公示牌坐标列表")
    private List<SignboardLocationItem> signboardLocations;

    @Data
    public static class SignboardLocationItem {

        @Schema(description = "公示牌主键ID")
        private Long signboardId;

        @Schema(description = "公示牌经度")
        private BigDecimal longitude;

        @Schema(description = "公示牌纬度")
        private BigDecimal latitude;

        @Schema(description = "归属类型（river：河道；river_section：河段）")
        private String ownerType;

        @Schema(description = "归属主键ID")
        private Long ownerId;

        @Schema(description = "归属名称")
        private String ownerName;
    }
}
