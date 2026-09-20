package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 大屏统计 - 风险隐患点路线终点仓库下的防汛物资
 */
@Data
public class BigScreenFxRiskHazardRouteMaterialItemRespVO {

    @Schema(description = "物资 ID")
    private String materialId;

    @Schema(description = "品名")
    private String materialName;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "单位字典值")
    private String unit;

    @Schema(description = "储备单位")
    private String storageUnit;

    @Schema(description = "仓库地址")
    private String warehouseAddress;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
