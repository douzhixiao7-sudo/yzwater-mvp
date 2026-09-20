package com.sydigit.yzwater.module.job.weather;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.quartz.core.handler.JobHandler;
import com.sydigit.yzwater.module.service.weather.YzWeatherStationSpiderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 天气站点实时采集任务
 */
@Component("yzWeatherStationCollectJob")
@Slf4j
@RequiredArgsConstructor
public class YzWeatherStationCollectJob implements JobHandler {

    private final YzWeatherStationSpiderService stationSpiderService;

    @Override
    public String execute(String param) {
        Map<String, Object> result = stationSpiderService.collectStationRealtime(null);
        log.info("[execute][天气站点采集完成][result={}]", result);
        return StrUtil.format("weather station collect done: {}", result);
    }
}

