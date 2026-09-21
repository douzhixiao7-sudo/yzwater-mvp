package com.sydigit.yzwater.module.controller.admin.vo.pond;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 坑塘 GeoJSON 导入结果
 */
@Data
public class WaterPondImportRespVO {

    @Schema(description = "文件内 Feature 总数")
    private Integer totalCount;

    @Schema(description = "成功写入基础表条数")
    private Integer facilityCount;

    @Schema(description = "成功写入坑塘表条数（新增 + 更新）")
    private Integer pondCount;

    @Schema(description = "新增条数")
    private Integer insertedCount;

    @Schema(description = "按 CHBH 覆盖更新条数")
    private Integer updatedCount;

    @Schema(description = "几何写入成功条数（基础表）")
    private Integer geometryCount;

    @Schema(description = "文件内重复 CHBH 跳过条数")
    private Integer skippedCount;

    @Schema(description = "导入结果说明")
    private String message;
}
