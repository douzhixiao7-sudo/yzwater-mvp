package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 防汛物资仓库新增/编辑入参
 */
@Data
@Schema(description = "管理后台 - 防汛物资仓库新增/编辑 Request VO")
public class FloodPreventionMaterialWarehouseSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private Long id;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;

    @Schema(description = "具体位置")
    private String specificLocation;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "归属单位")
    private String belongUnit;

    @Schema(description = "负责人姓名")
    private String leaderName;

    @Schema(description = "负责人电话")
    private String leaderPhone;

    @Schema(description = "物资种类")
    private String materialType;

    @Schema(description = "是否代储(0-否 1-是)")
    private Integer isDelegateStorage;

    @Schema(description = "仓库图片（text[]）")
    private List<String> warehouseImages;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "行政划分（text[]）")
    private List<String> divisionCode;
}
