package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台 - 物资管理列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 物资管理列表 Response VO")
public class FxWzListRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "品名")
    private String materialName;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "物资类型")
    private String materialType;

    @Schema(description = "储备单位")
    private String storageUnit;

    @Schema(description = "是否代储(0-否 1-是)")
    private Integer isDelegateStorage;

    @Schema(description = "储备单位 ID")
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
