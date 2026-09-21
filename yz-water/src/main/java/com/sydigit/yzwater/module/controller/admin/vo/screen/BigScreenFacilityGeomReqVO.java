package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 大屏统计 - 设施几何查询入参
 */
@Data
public class BigScreenFacilityGeomReqVO {

    @Schema(description = "设施类别值列表（zd_sslb 的 value，可多选）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设施类别不能为空")
    private List<String> value;

    @Schema(description = "河道级别（zd_hljb 的 value，仅当 value 包含 river 时生效）")
    private String riverLevel;
}
