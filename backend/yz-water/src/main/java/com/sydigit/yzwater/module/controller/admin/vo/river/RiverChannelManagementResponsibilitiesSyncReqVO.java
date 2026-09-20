package com.sydigit.yzwater.module.controller.admin.vo.river;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "河道河长职责同步 Request VO")
@Data
public class RiverChannelManagementResponsibilitiesSyncReqVO {

    @Schema(description = "河长职责")
    private String responsibilities;
}
