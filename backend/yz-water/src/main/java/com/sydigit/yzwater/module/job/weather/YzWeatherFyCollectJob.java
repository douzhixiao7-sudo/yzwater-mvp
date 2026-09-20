package com.sydigit.yzwater.module.job.weather;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.quartz.core.handler.JobHandler;
import com.sydigit.yzwater.module.service.weather.YzWeatherRealtimeFyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * FY 实时图层采集任务
 */
@Component("yzWeatherFyCollectJob")
@Slf4j
@RequiredArgsConstructor
public class YzWeatherFyCollectJob implements JobHandler {

    private final YzWeatherRealtimeFyService weatherRealtimeFyService;

    @Override
    public String execute(String param) {
        Map<String, Object> result = weatherRealtimeFyService.collectRealtimeFy(null);
        log.info("[execute][FY 图层采集完成][result={}]", result);
        return StrUtil.format("weather fy collect done: {}", result);
    }
}

