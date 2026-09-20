package com.sydigit.yzwater.module.iot.gateway.service.device.remote;

import cn.hutool.core.lang.Assert;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.iot.core.biz.IotDeviceCommonApi;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.gateway.config.IotGatewayProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.sydigit.yzwater.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;

/**
 * Iot 设备信息 Service 实现类：调用远程的 device http 接口，进行设备认证、设备获取等
 *
 * @author lijun
 */
@Service
@Slf4j
public class IotDeviceApiImpl implements IotDeviceCommonApi {

    @Resource
    private IotGatewayProperties gatewayProperties;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        IotGatewayProperties.RpcProperties rpc = gatewayProperties.getRpc();
        restTemplate = new RestTemplateBuilder()
                .rootUri(rpc.getUrl())
                .readTimeout(rpc.getReadTimeout())
                .connectTimeout(rpc.getConnectTimeout())
                .build();
    }

    @Override
    public CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO) {
        return doPost("/rpc-api/iot/device/auth", authReqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<IotDeviceRespDTO> getDevice(IotDeviceGetReqDTO getReqDTO) {
        return doPost("/rpc-api/iot/device/get", getReqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<List<IotGenesisDeviceConfigRespDTO>> getGenesisDeviceConfigList(
            IotGenesisDeviceConfigListReqDTO listReqDTO) {
        return doPost("/rpc-api/iot/genesis/config-list", listReqDTO, new ParameterizedTypeReference<>() { });
    }

    @Override
    public CommonResult<List<IotMqttDeviceConfigRespDTO>> getMqttDeviceConfigList(
            IotMqttDeviceConfigListReqDTO listReqDTO) {
        return doPost("/rpc-api/iot/mqtt/config-list", listReqDTO, new ParameterizedTypeReference<>() { });
    }

    private <T, R> CommonResult<R> doPost(String url, T body,
                                          ParameterizedTypeReference<CommonResult<R>> responseType) {
        try {
            // 请求
            HttpEntity<T> requestEntity = new HttpEntity<>(body);
            ResponseEntity<CommonResult<R>> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, responseType);
            // 响应
            CommonResult<R> result = response.getBody();
            Assert.notNull(result, "请求结果不能为空");
            return result;
        } catch (Exception e) {
            log.error("[doPost][url({}) body({}) 发生异常]", url, body, e);
            return CommonResult.error(INTERNAL_SERVER_ERROR);
        }
    }

}
