package com.sydigit.yzwater.module.iot.framework.realtimedata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 点位实时数据拉取配置
 */
@ConfigurationProperties(prefix = "yz.iot.realtime-data")
@Data
public class IotRealtimeDataProperties {

    /**
     * 是否启用
     */
    private Boolean enabled = false;

    /**
     * 拉取地址
     */
    private String url;

    /**
     * Basic Auth 用户名
     */
    private String username;

    /**
     * Basic Auth 密码
     */
    private String password;

    /**
     * 请求体内容（对应 r.json，可为空）
     */
    private String requestBody = "";

    /**
     * 调度 Cron 表达式（默认每 5 分钟）
     */
    private String cron = "0 */5 * * * ?";

    /**
     * 点位映射配置
     */
    private List<PointMapping> mappings = new ArrayList<>();

    @Data
    public static class PointMapping {

        /**
         * 点位名称（PointName）
         */
        private String pointName;

        /**
         * 设备编号
         */
        private Long deviceId;

        /**
         * 物模型标识符
         */
        private String identifier;

        /**
         * 租户编号
         */
        private Long tenantId;
    }
}
