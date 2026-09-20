package com.sydigit.yzwater.module.config.video;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 海康视频配置
 */
@Data
@ConfigurationProperties(prefix = "yz.video.hk")
public class YzVideoHkProperties {

    /**
     * 是否启用海康视频能力
     */
    private boolean enabled = true;

    /**
     * 海康网关地址，支持 ip:port 或 https://ip:port
     */
    private String host;

    /**
     * 预览流对外访问基地址，例如 wss://127.0.0.1:9004
     */
    private String previewPublicBaseUrl;

    /**
     * 海康 AppKey
     */
    private String appKey;

    /**
     * 海康 AppSecret
     */
    private String appSecret;

    /**
     * 海康事件回调地址
     */
    private String eventDest;

    /**
     * 事件类型，逗号分隔，如：42200211236000
     */
    private String eventTypes;

    /**
     * 事件级别，逗号分隔，如：0
     */
    private String eventLevels;

    /**
     * 拉取分页大小
     */
    private Integer pageSize = 500;
}
