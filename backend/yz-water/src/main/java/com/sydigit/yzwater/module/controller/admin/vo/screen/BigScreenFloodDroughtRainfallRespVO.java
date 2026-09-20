package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 大屏统计-防汛抗旱 降雨量响应
 */
@Data
public class BigScreenFloodDroughtRainfallRespVO {

    @Schema(description = "未来1小时降雨量")
    private BigDecimal rainfallNext1h;

    @Schema(description = "未来12小时降雨量")
    private BigDecimal rainfallNext12h;

    @Schema(description = "未来24小时降雨量")
    private BigDecimal rainfallNext24h;
}
