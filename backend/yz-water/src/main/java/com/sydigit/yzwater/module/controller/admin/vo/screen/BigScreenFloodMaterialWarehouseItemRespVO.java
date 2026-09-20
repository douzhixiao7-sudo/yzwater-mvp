package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 大屏统计 - 防汛物资按储备单位查询响应
 */
@Data
public class BigScreenFloodMaterialWarehouseItemRespVO {

    @Schema(description = "序号")
    private Integer serialNo;

    @Schema(description = "品名")
    private String materialName;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "储备单位/仓库地址展示名")
    private String warehouseName;

    @Schema(description = "仓库地址")
    private String warehouseAddress;

    @Schema(description = "防汛物资 ID")
    private String materialId;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
