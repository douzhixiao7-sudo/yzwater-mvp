package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "仪征管理后台 - 总河长新增/编辑 Request VO")
@Data
public class RiverChiefInfoTotalChiefSaveReqVO {

    @Schema(description = "ID（编辑时必填）")
    private Long id;

    @Schema(description = "河长姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "河长姓名不能为空")
    private String headName;

    @Schema(description = "河长级别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "河长级别不能为空")
    private String headLevel;

    @Schema(description = "河长职务")
    private String headPosition;
}
