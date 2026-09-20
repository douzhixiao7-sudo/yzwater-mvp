package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台 - 物资管理保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 物资管理保存 Request VO")
public class FxWzSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private String id;

    @Schema(description = "品名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "品名不能为空")
    private String materialName;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "单位不能为空")
    private String unit;

    @Schema(description = "物资类型")
    private String materialType;

    @Schema(description = "是否代储(0-否 1-是)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否代储不能为空")
    private Integer isDelegateStorage;

    @Schema(description = "储备单位 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "储备单位不能为空")
    private String warehouseId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "仓库地址")
    private String warehouseAddress;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系方式")
    private String contactInfo;
}
