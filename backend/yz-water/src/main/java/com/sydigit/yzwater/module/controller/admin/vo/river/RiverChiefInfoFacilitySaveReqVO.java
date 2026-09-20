package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "仪征管理后台 - 河长信息关联设施保存 Request VO")
@Data
public class RiverChiefInfoFacilitySaveReqVO {

    @Schema(description = "关联设施类型（river：河道；river_section：河段；reservoir：水库）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "关联设施类型不能为空")
    private String referenceType;

    @Schema(description = "关联设施ID（河道ID/河段ID/水库ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关联设施ID不能为空")
    private Long referenceId;
}
