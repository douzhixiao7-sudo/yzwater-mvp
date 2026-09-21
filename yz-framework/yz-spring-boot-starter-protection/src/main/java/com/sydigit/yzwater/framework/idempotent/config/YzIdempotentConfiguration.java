package com.sydigit.yzwater.framework.idempotent.config;

import com.sydigit.yzwater.framework.idempotent.core.aop.IdempotentAspect;
import com.sydigit.yzwater.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import com.sydigit.yzwater.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import com.sydigit.yzwater.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import com.sydigit.yzwater.framework.idempotent.core.keyresolver.impl.UserIdempotentKeyResolver;
import com.sydigit.yzwater.framework.idempotent.core.redis.IdempotentRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import com.sydigit.yzwater.framework.redis.config.YzRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration(after = YzRedisAutoConfiguration.class)
public class YzIdempotentConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentRedisDAO(stringRedisTemplate);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public UserIdempotentKeyResolver userIdempotentKeyResolver() {
        return new UserIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
