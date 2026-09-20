package com.sydigit.yzwater.module.iot.gateway.protocol.genesis.client;

import cn.hutool.core.collection.CollUtil;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisPointRespDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;

/**
 * GENESIS64 HTTP 客户端
 */
public class IotGenesisHttpClient {

    public List<IotGenesisRealtimeDataItem> queryRealtimeData(IotGenesisDeviceConfigRespDTO config) {
        if (CollUtil.isEmpty(config.getPoints())) {
            return List.of();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(config.getUsername(), config.getPassword(), StandardCharsets.UTF_8);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(
                Map.of("PointName", convertList(config.getPoints(), IotGenesisPointRespDTO::getPointName)), headers);
        return createRestTemplate(config.getTimeout()).exchange(config.getBaseUrl(), HttpMethod.POST, requestEntity,
                new ParameterizedTypeReference<List<IotGenesisRealtimeDataItem>>() {
                }).getBody();
    }

    private RestTemplate createRestTemplate(Integer timeout) {
        int timeoutMs = timeout == null || timeout <= 0 ? 5000 : timeout;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        return new RestTemplate(requestFactory);
    }

}
