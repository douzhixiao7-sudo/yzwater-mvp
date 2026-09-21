package com.sydigit.yzwater.module.system.framework.web.config;

import com.sydigit.yzwater.framework.swagger.config.YzSwaggerAutoConfiguration;
import com.sydigit.yzwater.module.system.framework.sso.config.UserCenterSsoProperties;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * system 模块的 web 组件的 Configuration
 *
 *
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(UserCenterSsoProperties.class)
public class SystemWebConfiguration {

    /**
     * system 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi systemGroupedOpenApi() {
        return YzSwaggerAutoConfiguration.buildGroupedOpenApi("system");
    }

}
