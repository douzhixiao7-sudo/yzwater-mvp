package com.sydigit.yzwater.framework.mq.redis.config;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import com.sydigit.yzwater.framework.common.enums.DocumentEnum;
import com.sydigit.yzwater.framework.mq.redis.core.RedisMQTemplate;
import com.sydigit.yzwater.framework.mq.redis.core.job.RedisPendingMessageResendJob;
import com.sydigit.yzwater.framework.mq.redis.core.job.RedisStreamMessageCleanupJob;
import com.sydigit.yzwater.framework.mq.redis.core.pubsub.AbstractRedisChannelMessageListener;
import com.sydigit.yzwater.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import com.sydigit.yzwater.framework.redis.config.YzRedisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.Properties;

/**
 * Redis 消息队列 Consumer 配置
 */
@Slf4j
@EnableScheduling // 启用定时任务，用于 RedisPendingMessageResendJob 重发消息
@AutoConfiguration(after = YzRedisAutoConfiguration.class)
public class YzRedisMQConsumerAutoConfiguration {

    /**
     * 创建 Redis Pub/Sub 广播消费容器
     */
    @Bean
    @ConditionalOnBean(AbstractRedisChannelMessageListener.class)
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractRedisChannelMessageListener<?>> listeners) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisMQTemplate.getRedisTemplate().getRequiredConnectionFactory());
        listeners.forEach(listener -> {
            listener.setRedisMQTemplate(redisMQTemplate);
            container.addMessageListener(listener, new ChannelTopic(listener.getChannel()));
            log.info("[redisMessageListenerContainer][注册 Channel({}) 对应监听器({})]",
                    listener.getChannel(), listener.getClass().getName());
        });
        return container;
    }

    /**
     * 创建 Redis Stream 待确认消息重发任务
     */
    @Bean
    @ConditionalOnBean(AbstractRedisStreamMessageListener.class)
    public RedisPendingMessageResendJob redisPendingMessageResendJob(List<AbstractRedisStreamMessageListener<?>> listeners,
                                                                     RedisMQTemplate redisTemplate,
                                                                     RedissonClient redissonClient) {
        return new RedisPendingMessageResendJob(listeners, redisTemplate, redissonClient);
    }

    /**
     * 创建 Redis Stream 消息清理任务
     */
    @Bean
    @ConditionalOnBean(AbstractRedisStreamMessageListener.class)
    public RedisStreamMessageCleanupJob redisStreamMessageCleanupJob(List<AbstractRedisStreamMessageListener<?>> listeners,
                                                                     RedisMQTemplate redisTemplate,
                                                                     RedissonClient redissonClient) {
        return new RedisStreamMessageCleanupJob(listeners, redisTemplate, redissonClient);
    }

    /**
     * 创建 Redis Stream 消费容器
     *
     * 基础知识：https://www.geek-book.com/src/docs/redis/redis/redis.io/commands/xreadgroup.html
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnBean(AbstractRedisStreamMessageListener.class)
    public StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisStreamMessageListenerContainer(
            RedisMQTemplate redisMQTemplate, List<AbstractRedisStreamMessageListener<?>> listeners) {
        RedisTemplate<String, ?> redisTemplate = redisMQTemplate.getRedisTemplate();
        checkRedisVersion(redisTemplate);
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(10)
                        .targetType(String.class)
                        .build();
        StreamMessageListenerContainer<String, ObjectRecord<String, String>> container =
                StreamMessageListenerContainer.create(redisMQTemplate.getRedisTemplate().getRequiredConnectionFactory(), containerOptions);

        String consumerName = buildConsumerName();
        listeners.parallelStream().forEach(listener -> {
            log.info("[redisStreamMessageListenerContainer][开始注册 StreamKey({}) 对应监听器({})]",
                    listener.getStreamKey(), listener.getClass().getName());
            try {
                ensureConsumerGroup(redisTemplate, listener.getStreamKey(), listener.getGroup());
            } catch (Exception ignore) {
            }
            listener.setRedisMQTemplate(redisMQTemplate);
            Consumer consumer = Consumer.from(listener.getGroup(), consumerName);
            StreamOffset<String> streamOffset = StreamOffset.create(listener.getStreamKey(), ReadOffset.lastConsumed());
            StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                    .builder(streamOffset).consumer(consumer)
                    .autoAcknowledge(false)
                    .cancelOnError(throwable -> false);
            container.register(builder.build(), listener);
            log.info("[redisStreamMessageListenerContainer][完成注册 StreamKey({}) 对应监听器({})]",
                    listener.getStreamKey(), listener.getClass().getName());
        });
        return container;
    }

    /**
     * 构建消费者名称，使用本地 IP + 进程号
     */
    public static String buildConsumerName() {
        return String.format("%s@%d", SystemUtil.getHostInfo().getAddress(), SystemUtil.getCurrentPID());
    }

    /**
     * 校验 Redis 版本，要求 >= 5.0.0
     */
    public static void checkRedisVersion(RedisTemplate<String, ?> redisTemplate) {
        Properties info = redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
        String version = MapUtil.getStr(info, "redis_version");
        int majorVersion = Integer.parseInt(StrUtil.subBefore(version, '.', false));
        if (majorVersion < 5) {
            throw new IllegalStateException(StrUtil.format("当前 Redis 版本为 {}，小于最低要求 5.0.0，请参考 {} 文档进行安装。",
                    version, DocumentEnum.REDIS_INSTALL.getUrl()));
        }
    }

    private static void ensureConsumerGroup(RedisTemplate<String, ?> redisTemplate, String streamKey, String group) {
        if (StrUtil.isBlank(streamKey) || StrUtil.isBlank(group)) {
            return;
        }
        try {
            redisTemplate.execute((RedisCallback<String>) connection -> {
                byte[] rawKey = redisTemplate.getStringSerializer().serialize(streamKey);
                if (rawKey == null) {
                    return null;
                }
                return connection.streamCommands().xGroupCreate(rawKey, group, ReadOffset.latest(), true);
            });
        } catch (Exception ignore) {
            // 已存在或创建失败时不抛出，避免影响启动流程
        }
    }

}