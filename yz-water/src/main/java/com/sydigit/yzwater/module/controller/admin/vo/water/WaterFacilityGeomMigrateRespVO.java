package com.sydigit.yzwater.module.controller.admin.vo.water;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 几何数据迁移结果
 */
@Data
public class WaterFacilityGeomMigrateRespVO {

    @Schema(description = "成功迁移数量")
    private int successCount;

    @Schema(description = "失败数量")
    private int failedCount;

    @Schema(description = "失败原因")
    private String message;
}
