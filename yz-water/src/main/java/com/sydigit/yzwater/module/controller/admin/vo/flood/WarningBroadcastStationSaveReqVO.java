package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台 - 预警广播站保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 预警广播站保存 Request VO")
public class WarningBroadcastStationSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "行政区划ID（system_area.id）")
    private String adminDivision;

    @Schema(description = "代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "代码不能为空")
    private String code;

    @Schema(description = "数量")
    @Min(value = 0, message = "数量不能小于 0")
    private Integer quantity;

    @Schema(description = "排序号")
    private Integer sort;
}
