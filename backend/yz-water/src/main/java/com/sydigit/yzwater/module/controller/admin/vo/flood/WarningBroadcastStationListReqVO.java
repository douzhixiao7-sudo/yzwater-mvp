package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 预警广播站列表查询 Request VO
 */
@Data
@Schema(description = "管理后台 - 预警广播站列表查询 Request VO")
public class WarningBroadcastStationListReqVO {

    @Schema(description = "名称（模糊查询）")
    private String name;

    @Schema(description = "代码（模糊查询）")
    private String code;

    @Schema(description = "行政区划ID（system_area.id）")
    private String adminDivision;
}
