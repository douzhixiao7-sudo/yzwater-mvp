package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 省级河道导入响应
 */
@Data
public class MainRiverImportRespVO {

    @Schema(description = "基础设施新增数量")
    private Integer facilityCount;

    @Schema(description = "河道新增数量")
    private Integer riverChannelCount;

    @Schema(description = "河段新增数量")
    private Integer riverSectionCount;

    @Schema(description = "提示信息")
    private String message;
}
