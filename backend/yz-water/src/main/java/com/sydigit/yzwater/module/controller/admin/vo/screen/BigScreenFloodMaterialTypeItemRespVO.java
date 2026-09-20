package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 大屏统计 - 防汛物资按类型查询响应
 */
@Data
public class BigScreenFloodMaterialTypeItemRespVO {

    @Schema(description = "品名")
    private String materialName;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "单位")
    private String unitLabel;

    @Schema(description = "是否代储(0-否 1-是)")
    private Integer isDelegateStorage;

    @Schema(description = "储备单位")
    private String storageUnit;

    @Schema(description = "负责人姓名")
    private String leaderName;

    @Schema(description = "负责人电话")
    private String leaderPhone;
}
