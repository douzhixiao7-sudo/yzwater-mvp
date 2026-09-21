package com.sydigit.yzwater.module.controller.admin.vo.weather;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 天气站点实时采集请求
 */
@Data
@Schema(description = "管理后台 - 天气站点实时采集请求")
public class WeatherStationCollectReqVO {

    @Schema(description = "指定采集站点编码列表；为空时采集全部站点")
    private List<String> stationCodes;

}

