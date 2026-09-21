package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "仪征管理后台 - 总河长 Response VO")
@Data
public class RiverChiefInfoTotalChiefRespVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "河长姓名")
    private String headName;

    @Schema(description = "河长级别")
    private String headLevel;

    @Schema(description = "河长职务")
    private String headPosition;
}
