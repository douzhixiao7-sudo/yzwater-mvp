package com.sydigit.yzwater.module.iot.framework.realtimedata.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 点位实时数据拉取配置类
 */
@Configuration
@EnableConfigurationProperties(IotRealtimeDataProperties.class)
public class IotRealtimeDataConfiguration {
}
