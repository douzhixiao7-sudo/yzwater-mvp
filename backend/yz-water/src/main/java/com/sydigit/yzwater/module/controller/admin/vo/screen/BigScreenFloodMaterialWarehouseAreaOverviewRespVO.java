package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大屏统计 - 防汛物资仓库总览（按行政区划筛选，包含子级）
 */
@Data
public class BigScreenFloodMaterialWarehouseAreaOverviewRespVO {

    @Schema(description = "仓库总数", example = "12")
    private Long totalCount;

    @Schema(description = "物资种类（多个用、分割）", example = "沙袋、木桩")
    private String materialTypes;

    @Schema(description = "仓库列表（不分页）")
    private List<Item> list;

    @Data
    public static class Item {

        @Schema(description = "仓库ID", example = "20001")
        private Long warehouseId;

        @Schema(description = "仓库名称", example = "防汛物资仓库1")
        private String warehouseName;

        @Schema(description = "归属单位", example = "某某单位")
        private String belongUnit;

        @Schema(description = "经度", example = "119.1845")
        private BigDecimal longitude;

        @Schema(description = "纬度", example = "32.2720")
        private BigDecimal latitude;
    }
}

