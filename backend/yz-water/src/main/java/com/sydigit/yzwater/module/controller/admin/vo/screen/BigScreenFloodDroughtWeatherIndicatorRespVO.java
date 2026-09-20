package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 大屏统计-防汛抗旱 天气指标响应
 */
@Data
public class BigScreenFloodDroughtWeatherIndicatorRespVO {

    @Schema(description = "温度")
    private BigDecimal temperature;

    @Schema(description = "湿度（百分比）")
    private BigDecimal humidity;

    @Schema(description = "风力/风速")
    private BigDecimal windSpeed;

    @Schema(description = "PM2.5")
    private BigDecimal pm25;

    @Schema(description = "空气质量")
    private String airQuality;
}
