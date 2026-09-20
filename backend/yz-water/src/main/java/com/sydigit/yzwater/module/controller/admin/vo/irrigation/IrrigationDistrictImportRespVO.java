package com.sydigit.yzwater.module.controller.admin.vo.irrigation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 灌区导入结果
 */
@Data
public class IrrigationDistrictImportRespVO {

    @Schema(description = "导入设施基础记录数")
    private Integer facilityCount;

    @Schema(description = "导入灌区记录数")
    private Integer irrigationDistrictCount;

    @Schema(description = "几何写入成功数")
    private Integer geometryCount;

    @Schema(description = "导入结果说明")
    private String message;
}

