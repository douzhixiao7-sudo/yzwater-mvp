package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理后台 - 预警广播站列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 预警广播站列表 Response VO")
public class WarningBroadcastStationListRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "行政区划ID（system_area.id）")
    private String adminDivision;

    @Schema(description = "代码")
    private String code;

    @Schema(description = "数量")
    private Integer quantity;
}
