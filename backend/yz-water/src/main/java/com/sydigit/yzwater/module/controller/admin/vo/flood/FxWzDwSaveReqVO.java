package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理后台 - 防汛物资单位保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 防汛物资单位保存 Request VO")
public class FxWzDwSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private String id;

    @Schema(description = "单位名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "单位名称不能为空")
    private String unitName;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "位置 GeoJSON（仅 Point）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "位置信息不能为空")
    private String geometryGeoJson;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "是否代储(0-否 1-是)")
    private Integer isDelegateStorage;
}
