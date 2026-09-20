package com.sydigit.yzwater.module.controller.admin.vo.reservoir;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "水库河长职责同步 Request VO")
@Data
public class ReservoirManagementResponsibilitiesSyncReqVO {

    @Schema(description = "河长职责")
    private String responsibilities;
}
