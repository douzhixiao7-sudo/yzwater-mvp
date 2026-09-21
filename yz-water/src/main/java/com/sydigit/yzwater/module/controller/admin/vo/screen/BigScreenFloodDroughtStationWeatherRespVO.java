package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 大屏统计-防汛抗旱 站点天气响应
 */
@Data
public class BigScreenFloodDroughtStationWeatherRespVO {

    @Schema(description = "站点名称")
    private String stationName;

    @Schema(description = "站点编码")
    private String stationCode;

    @Schema(description = "实时天气采集时间")
    private String createTime;

    @Schema(description = "实时天气JSON")
    private Map<String, Object> weather;
}
