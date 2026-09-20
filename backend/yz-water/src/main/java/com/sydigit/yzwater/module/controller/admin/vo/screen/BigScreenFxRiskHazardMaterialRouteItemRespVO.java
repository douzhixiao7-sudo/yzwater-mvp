package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大屏统计 - 风险隐患点抢险物资调运路线（单条）
 */
@Data
public class BigScreenFxRiskHazardMaterialRouteItemRespVO {

    @Schema(description = "路线序号（从 0 起）")
    private Integer routeIndex;

    @Schema(description = "关联防汛物资 ID（路线终点 linkedWarehouseId）")
    private String materialId;

    @Schema(description = "终点仓库名称（优先仓库地址，其次储备单位）")
    private String warehouseName;

    @Schema(description = "仓库地址")
    private String warehouseAddress;

    @Schema(description = "储备单位")
    private String storageUnit;

    @Schema(description = "终点经度（EPSG:4490，优先取防汛物资坐标）")
    private BigDecimal endLongitude;

    @Schema(description = "终点纬度（EPSG:4490，优先取防汛物资坐标）")
    private BigDecimal endLatitude;

    @Schema(description = "该仓库地址下的防汛物资列表")
    private List<BigScreenFxRiskHazardRouteMaterialItemRespVO> materials;

    @Schema(description = "路线折点坐标序列 [[经度, 纬度], ...]，含隐患点起点与仓库终点")
    private List<List<BigDecimal>> coordinates;
}
