package com.sydigit.yzwater.module.iot.gateway.config;

import com.sydigit.yzwater.module.iot.core.biz.IotDeviceCommonApi;
import com.sydigit.yzwater.module.iot.core.messagebus.core.IotMessageBus;
import com.sydigit.yzwater.module.iot.gateway.protocol.emqx.IotEmqxAuthEventProtocol;
import com.sydigit.yzwater.module.iot.gateway.protocol.emqx.IotEmqxDownstreamSubscriber;
import com.sydigit.yzwater.module.iot.gateway.protocol.emqx.IotEmqxUpstreamProtocol;
import com.sydigit.yzwater.module.iot.gateway.protocol.genesis.IotGenesisHttpProtocol;
import com.sydigit.yzwater.module.iot.gateway.protocol.http.IotHttpDownstreamSubscriber;
import com.sydigit.yzwater.module.iot.gateway.protocol.http.IotHttpUpstreamProtocol;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqtt.IotMqttDownstreamSubscriber;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqtt.IotMqttUpstreamProtocol;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqtt.manager.IotMqttConnectionManager;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqtt.router.IotMqttDownstreamHandler;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttsource.IotMqttSourceProtocol;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttws.IotMqttWsDownstreamSubscriber;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttws.IotMqttWsUpstreamProtocol;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttws.manager.IotMqttWsConnectionManager;
import com.sydigit.yzwater.module.iot.gateway.protocol.mqttws.router.IotMqttWsDownstreamHandler;
import com.sydigit.yzwater.module.iot.gateway.protocol.tcp.IotTcpDownstreamSubscriber;
import com.sydigit.yzwater.module.iot.gateway.protocol.tcp.IotTcpUpstreamProtocol;
import com.sydigit.yzwater.module.iot.gateway.protocol.tcp.manager.IotTcpConnectionManager;
import com.sydigit.yzwater.module.iot.gateway.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IotGatewayProperties.class)
@Slf4j
public class IotGatewayConfiguration {

    /**
     * IoT 网关 HTTP 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yz.iot.gateway.protocol.http", name = "enabled", havingValue = "true")
    @Slf4j
    public static class HttpProtocolConfiguration {

        @Bean
        public IotHttpUpstreamProtocol iotHttpUpstreamProtocol(IotGatewayProperties gatewayProperties) {
            return new IotHttpUpstreamProtocol(gatewayProperties.getProtocol().getHttp());
        }

        @Bean
        public IotHttpDownstreamSubscriber iotHttpDownstreamSubscriber(IotHttpUpstreamProtocol httpUpstreamProtocol,
                                                                       IotMessageBus messageBus) {
            return new IotHttpDownstreamSubscriber(httpUpstreamProtocol, messageBus);
        }
    }

    /**
     * IoT 网关 GENESIS64 HTTP 主动采集协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yz.iot.gateway.protocol.genesis64-http", name = "enabled", havingValue = "true")
    @Slf4j
    public static class Genesis64ProtocolConfiguration {

        @Bean(destroyMethod = "close")
        public Vertx genesisVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotGenesisHttpProtocol iotGenesisHttpProtocol(IotGatewayProperties gatewayProperties,
                                                             IotDeviceCommonApi deviceCommonApi,
                                                             IotDeviceMessageService messageService,
                                                             Vertx genesisVertx) {
            return new IotGenesisHttpProtocol(gatewayProperties.getProtocol().getGenesis64Http(),
                    deviceCommonApi, messageService, genesisVertx);
        }
    }

    /**
     * IoT 网关 MQTT Source 固定主题采集协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yz.iot.gateway.protocol.mqtt-source", name = "enabled", havingValue = "true")
    @Slf4j
    public static class MqttSourceProtocolConfiguration {

        @Bean(destroyMethod = "close")
        public Vertx mqttSourceVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotMqttSourceProtocol iotMqttSourceProtocol(IotGatewayProperties gatewayProperties,
                                                           IotDeviceCommonApi deviceCommonApi,
                                                           IotDeviceMessageService messageService,
                                                           Vertx mqttSourceVertx) {
            return new IotMqttSourceProtocol(gatewayProperties.getProtocol().getMqttSource(),
                    deviceCommonApi, messageService, mqttSourceVertx);
        }
    }

    /**
     * IoT 网关 EMQX 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yz.iot.gateway.protocol.emqx", name = "enabled", havingValue = "true")
    @Slf4j
    public static class EmqxProtocolConfiguration {

        @Bean(destroyMethod = "close")
        public Vertx emqxVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotEmqxAuthEventProtocol iotEmqxAuthEventProtocol(IotGatewayProperties gatewayProperties,
                                                                 Vertx emqxVertx) {
            return new IotEmqxAuthEventProtocol(gatewayProperties.getProtocol().getEmqx(), emqxVertx);
        }

        @Bean
        public IotEmqxUpstreamProtocol iotEmqxUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                               Vertx emqxVertx) {
            return new IotEmqxUpstreamProtocol(gatewayProperties.getProtocol().getEmqx(), emqxVertx);
        }

        @Bean
        public IotEmqxDownstreamSubscriber iotEmqxDownstreamSubscriber(IotEmqxUpstreamProtocol mqttUpstreamProtocol,
                                                                       IotMessageBus messageBus) {
            return new IotEmqxDownstreamSubscriber(mqttUpstreamProtocol, messageBus);
        }
    }

    /**
     * IoT 网关 TCP 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yz.iot.gateway.protocol.tcp", name = "enabled", havingValue = "true")
    @Slf4j
    public static class TcpProtocolConfiguration {

        @Bean(destroyMethod = "close")
        public Vertx tcpVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotTcpUpstreamProtocol iotTcpUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                             IotDeviceService deviceService,
                                                             IotDeviceMessageService messageService,
                                                             IotTcpConnectionManager connectionManager,
                                                             Vertx tcpVertx) {
            return new IotTcpUpstreamProtocol(gatewayProperties.getProtocol().getTcp(),
                    deviceService, messageService, connectionManager, tcpVertx);
        }

        @Bean
        public IotTcpDownstreamSubscriber iotTcpDownstreamSubscriber(IotTcpUpstreamProtocol protocolHandler,
                                                                     IotDeviceMessageService messageService,
                                                                     IotDeviceService deviceService,
                                                                     IotTcpConnectionManager connectionManager,
                                                                     IotMessageBus messageBus) {
            return new IotTcpDownstreamSubscriber(protocolHandler, messageService, deviceService, connectionManager,
                    messageBus);
        }

    }

    /**
     * IoT 网关 MQTT 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yz.iot.gateway.protocol.mqtt", name = "enabled", havingValue = "true")
    @Slf4j
    public static class MqttProtocolConfiguration {

        @Bean(destroyMethod = "close")
        public Vertx mqttVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotMqttUpstreamProtocol iotMqttUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                               IotDeviceMessageService messageService,
                                                               IotMqttConnectionManager connectionManager,
                                                               Vertx mqttVertx) {
            return new IotMqttUpstreamProtocol(gatewayProperties.getProtocol().getMqtt(), messageService,
                    connectionManager, mqttVertx);
        }

        @Bean
        public IotMqttDownstreamHandler iotMqttDownstreamHandler(IotDeviceMessageService messageService,
                                                                 IotMqttConnectionManager connectionManager) {
            return new IotMqttDownstreamHandler(messageService, connectionManager);
        }

        @Bean
        public IotMqttDownstreamSubscriber iotMqttDownstreamSubscriber(IotMqttUpstreamProtocol mqttUpstreamProtocol,
                                                                       IotMqttDownstreamHandler downstreamHandler,
                                                                       IotMessageBus messageBus) {
            return new IotMqttDownstreamSubscriber(mqttUpstreamProtocol, downstreamHandler, messageBus);
        }

    }

    /**
     * IoT 网关 MQTT WebSocket 协议配置类
     */
    @Configuration
    @ConditionalOnProperty(prefix = "yz.iot.gateway.protocol.mqtt-ws", name = "enabled", havingValue = "true")
    @Slf4j
    public static class MqttWsProtocolConfiguration {

        @Bean(destroyMethod = "close")
        public Vertx mqttWsVertx() {
            return Vertx.vertx();
        }

        @Bean
        public IotMqttWsUpstreamProtocol iotMqttWsUpstreamProtocol(IotGatewayProperties gatewayProperties,
                                                                   IotDeviceMessageService messageService,
                                                                   IotMqttWsConnectionManager connectionManager,
                                                                   Vertx mqttWsVertx) {
            return new IotMqttWsUpstreamProtocol(gatewayProperties.getProtocol().getMqttWs(),
                    messageService, connectionManager, mqttWsVertx);
        }

        @Bean
        public IotMqttWsDownstreamHandler iotMqttWsDownstreamHandler(IotDeviceMessageService messageService,
                                                                     IotDeviceService deviceService,
                                                                     IotMqttWsConnectionManager connectionManager) {
            return new IotMqttWsDownstreamHandler(messageService, deviceService, connectionManager);
        }

        @Bean
        public IotMqttWsDownstreamSubscriber iotMqttWsDownstreamSubscriber(
                IotMqttWsUpstreamProtocol mqttWsUpstreamProtocol,
                IotMqttWsDownstreamHandler downstreamHandler,
                IotMessageBus messageBus) {
            return new IotMqttWsDownstreamSubscriber(mqttWsUpstreamProtocol, downstreamHandler, messageBus);
        }

    }

}
