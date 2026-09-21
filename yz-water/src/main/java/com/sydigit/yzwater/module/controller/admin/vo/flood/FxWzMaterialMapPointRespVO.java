package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 防汛物资地图点位（含仓库地址坐标，供风险隐患点路线关联）
 */
@Data
@Schema(description = "防汛物资地图点位")
public class FxWzMaterialMapPointRespVO {

    @Schema(description = "物资 ID")
    private String id;

    @Schema(description = "品名")
    private String materialName;

    @Schema(description = "储备单位名称")
    private String storageUnit;

    @Schema(description = "仓库地址")
    private String warehouseAddress;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
