package com.sydigit.yzwater.module.controller.admin.vo.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 天气采集请求
 */
@Data
@Schema(description = "管理后台 - 天气采集请求")
public class WeatherCollectReqVO {

    @Schema(description = "采集日期，格式 yyyy-MM-dd；为空默认当天", example = "2026-03-23")
    private String synTime;
}
