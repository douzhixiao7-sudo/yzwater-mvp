package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大屏统计 - 抢险队伍计划响应
 */
@Data
public class BigScreenFloodMaterialRescuePlanRespVO {

    @Schema(description = "单位")
    private String unitName;

    @Schema(description = "人数")
    private Integer planCount;
}
