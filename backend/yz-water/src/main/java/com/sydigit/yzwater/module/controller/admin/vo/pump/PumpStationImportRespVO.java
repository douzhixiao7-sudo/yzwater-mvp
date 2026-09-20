package com.sydigit.yzwater.module.controller.admin.vo.pump;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 泵站导入结果
 */
@Data
public class PumpStationImportRespVO {

    @Schema(description = "基础表新增数量")
    private Integer facilityCount;

    @Schema(description = "泵站表新增数量")
    private Integer pumpStationCount;

    @Schema(description = "提示信息")
    private String message;
}
