package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 防汛物资-单位位置迁移结果
 */
@Data
@Schema(description = "防汛物资-单位位置迁移结果")
public class FxWzDwGeomMigrateRespVO {

    @Schema(description = "成功迁移数量")
    private int successCount;

    @Schema(description = "失败数量")
    private int failedCount;

    @Schema(description = "提示信息")
    private String message;
}
