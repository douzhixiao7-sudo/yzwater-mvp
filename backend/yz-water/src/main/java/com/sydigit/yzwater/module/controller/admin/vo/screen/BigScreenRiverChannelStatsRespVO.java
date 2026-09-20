package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 大屏统计 - 河道统计返回
 */
@Data
@Schema(description = "仪征管理后台 - 大屏统计 - 河道统计返回")
public class BigScreenRiverChannelStatsRespVO {

    @Schema(description = "河道总数")
    private Long totalCount;

    @Schema(description = "河道总长度(km)")
    private BigDecimal totalLengthKm;

    @Schema(description = "流域面积总和(km2)")
    private BigDecimal totalCatchmentKm2;

    @Schema(description = "按河流级别(字典: zd_hljb)统计")
    private List<BigScreenDictCountItemVO> levelStats;
}

