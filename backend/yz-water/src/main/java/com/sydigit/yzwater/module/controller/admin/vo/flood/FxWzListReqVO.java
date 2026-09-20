package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 物资管理列表 Request VO
 */
@Data
@Schema(description = "管理后台 - 物资管理列表 Request VO")
public class FxWzListReqVO {

    @Schema(description = "品名")
    private String materialName;

    @Schema(description = "物资类型")
    private String materialType;

    @Schema(description = "单位")
    private String unit;
}
