package com.sydigit.yzwater.module.iot.core.messagebus.core.redis;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.iot.core.messagebus.core.IotMessageBus;
import com.sydigit.yzwater.module.iot.core.messagebus.core.IotMessageSubscriber;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.sydigit.yzwater.framework.mq.redis.config.YzRedisMQConsumerAutoConfiguration.buildConsumerName;
import static com.sydigit.yzwater.framework.mq.redis.config.YzRedisMQConsumerAutoConfiguration.checkRedisVersion;

/**
 * Redis 的 {@link IotMessageBus} 实现类
 *
 * @author lijun
 */
@Slf4j
public class IotRedisMessageBus implements IotMessageBus {

    private final RedisTemplate<String, ?> redisTemplate;

    private final StreamMessageListenerContainer<String, ObjectRecord<String, String>> redisStreamMessageListenerContainer;

    @Getter
    private final List<IotMessageSubscriber<?>> subscribers = new ArrayList<>();

    public IotRedisMessageBus(RedisTemplate<String, ?> redisTemplate) {
        this.redisTemplate = redisTemplate;
        checkRedisVersion(redisTemplate);
        // 创建 options 配置
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, String>> containerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(10) // 一次性最多拉取多少条消息
                        .targetType(String.class) // 目标类型，统一用 String，自行反序列化
                        .build();
        // 创建 container 对象
        this.redisStreamMessageListenerContainer =
                StreamMessageListenerContainer.create(redisTemplate.getRequiredConnectionFactory(), containerOptions);
    }

    @PostConstruct
    public void init() {
        this.redisStreamMessageListenerContainer.start();
    }

    @PreDestroy
    public void destroy() {
        this.redisStreamMessageListenerContainer.stop();
    }

    @Override
    public void post(String topic, Object message) {
        redisTemplate.opsForStream().add(StreamRecords.newRecord()
                .ofObject(JsonUtils.toJsonString(message)) // 设置内容
                .withStreamKey(topic)); // 设置 stream key
    }

    @Override
    public void register(IotMessageSubscriber<?> subscriber) {
        Type type = TypeUtil.getTypeArgument(subscriber.getClass(), 0);
        if (type == null) {
            throw new IllegalStateException(String.format("类型(%s) 需要设置消息类型", getClass().getName()));
        }

        // 创建 listener 对应的消费者分组，避免 Stream 不存在时报 NOGROUP
        ensureConsumerGroup(subscriber.getTopic(), subscriber.getGroup());
        // 创建 Consumer 对象
        String consumerName = buildConsumerName();
        Consumer consumer = Consumer.from(subscriber.getGroup(), consumerName);
        // 设置 Consumer 消费进度，以最小消费进度为准
        StreamOffset<String> streamOffset = StreamOffset.create(subscriber.getTopic(), ReadOffset.lastConsumed());
        // 设置 Consumer 监听
        StreamMessageListenerContainer.StreamReadRequestBuilder<String> builder = StreamMessageListenerContainer.StreamReadRequest
                .builder(streamOffset).consumer(consumer)
                .autoAcknowledge(false) // 不自动 ack
                .cancelOnError(throwable -> false); // 发生异常不取消消费
        redisStreamMessageListenerContainer.register(builder.build(), message -> {
            // 消费消息
            subscriber.onMessage(JsonUtils.parseObject(message.getValue(), type));
            // ack 消息消费完成
            redisTemplate.opsForStream().acknowledge(subscriber.getGroup(), message);
        });
        this.subscribers.add(subscriber);
    }

    private void ensureConsumerGroup(String streamKey, String group) {
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