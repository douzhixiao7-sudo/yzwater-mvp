package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 防汛物资仓库地图点位（含坐标，用于险工路径关联）
 */
@Data
@Schema(description = "防汛物资仓库地图点位")
public class FloodPreventionMaterialWarehouseMapPointRespVO {

    @Schema(description = "仓库 ID")
    private Long id;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
