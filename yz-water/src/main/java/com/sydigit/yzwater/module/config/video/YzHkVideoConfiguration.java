package com.sydigit.yzwater.module.config.video;

import cn.hutool.core.util.StrUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 海康视频 SDK 配置
 */
@Configuration
@EnableConfigurationProperties({YzVideoHkProperties.class, YzVideoScreenExcludeProperties.class})
public class YzHkVideoConfiguration {

    @Bean
    public ArtemisConfig yzVideoArtemisConfig(YzVideoHkProperties properties) {
        String host = normalizeHost(properties.getHost());
        String appKey = StrUtil.trimToEmpty(properties.getAppKey());
        String appSecret = StrUtil.trimToEmpty(properties.getAppSecret());
        ArtemisConfig config = new ArtemisConfig();
        config.setHost(host);
        config.setAppKey(appKey);
        config.setAppSecret(appSecret);
        applyLegacyStaticConfig(host, appKey, appSecret);
        return config;
    }

    private String normalizeHost(String host) {
        String value = StrUtil.trimToEmpty(host);
        if (StrUtil.isBlank(value)) {
            return value;
        }
        value = StrUtil.removePrefixIgnoreCase(value, "http://");
        value = StrUtil.removePrefixIgnoreCase(value, "https://");
        return StrUtil.removeSuffix(value, "/");
    }

    /**
     * 兼容 Artemis 老版本：host/appKey/appSecret 是静态字段。
     */
    private void applyLegacyStaticConfig(String host, String appKey, String appSecret) {
        trySetStaticField("host", host);
        trySetStaticField("appKey", appKey);
        trySetStaticField("appSecret", appSecret);
    }

    private void trySetStaticField(String fieldName, String fieldValue) {
        try {
            Field field = ArtemisConfig.class.getDeclaredField(fieldName);
            if (!Modifier.isStatic(field.getModifiers())) {
                return;
            }
            field.setAccessible(true);
            field.set(null, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // 新版本字段为实例属性，忽略即可
        }
    }
}
