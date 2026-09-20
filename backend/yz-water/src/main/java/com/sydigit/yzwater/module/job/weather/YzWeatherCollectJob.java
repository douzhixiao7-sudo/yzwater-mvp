package com.sydigit.yzwater.module.job.weather;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.quartz.core.handler.JobHandler;
import com.sydigit.yzwater.module.service.weather.YzWeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 每日天气采集任务
 */
@Component("yzWeatherCollectJob")
@Slf4j
@RequiredArgsConstructor
public class YzWeatherCollectJob implements JobHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final YzWeatherService weatherService;

    @Override
    public String execute(String param) {
        String synTime = StrUtil.trimToNull(param);
        String displayDate = synTime != null ? synTime : LocalDate.now().format(DATE_FORMATTER);
        Map<String, Object> payload = weatherService.collectWeatherBySynTime(synTime);
        int keyCount = payload == null ? 0 : payload.size();
        log.info("[execute][天气采集完成][synTime={}, keyCount={}]", displayDate, keyCount);
        return StrUtil.format("weather collect done, synTime={}, keys={}", displayDate, keyCount);
    }
}

