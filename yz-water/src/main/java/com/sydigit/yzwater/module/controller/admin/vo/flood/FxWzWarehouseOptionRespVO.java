package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 物资管理储备单位下拉 Response VO
 */
@Data
@Schema(description = "管理后台 - 物资管理储备单位下拉 Response VO")
public class FxWzWarehouseOptionRespVO {

    @Schema(description = "储备单位 ID")
    private String id;

    @Schema(description = "储备单位名称")
    private String warehouseName;
}
