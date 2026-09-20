package com.sydigit.yzwater.module.iot.controller.admin.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.http.IotDeviceHttpProtocolTestReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.http.IotDeviceHttpProtocolTestRespVO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import com.sydigit.yzwater.module.iot.core.util.IotDeviceAuthUtils;
import com.sydigit.yzwater.module.iot.service.realtimedata.dto.IotRealtimeDataPointDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备联调")
@RestController
@RequestMapping("/iot/device/http-test")
@Validated
public class IotDeviceHttpProtocolTestController {

    @Resource
    private RestTemplate restTemplate;

    @PostMapping("/pull-and-post")
    @Operation(summary = "拉取外部点位并上报属性")
    @PermitAll
    public CommonResult<IotDeviceHttpProtocolTestRespVO> pullAndPost(
            @Valid @RequestBody IotDeviceHttpProtocolTestReqVO reqVO) {
        String pointName = StrUtil.trimToNull(reqVO.getPointName());
        String identifier = StrUtil.trimToNull(reqVO.getIdentifier());
        String dataSourceResponse = requestRealtimeData(reqVO, pointName);
        List<IotRealtimeDataPointDTO> points = JsonUtils.parseArray(dataSourceResponse, IotRealtimeDataPointDTO.class);
        Object value = resolvePointValue(points, pointName);

        String gatewayUrl = normalizeBaseUrl(reqVO.getGatewayUrl());
        String token = requestToken(gatewayUrl, reqVO.getProductKey(), reqVO.getDeviceName(), reqVO.getDeviceSecret());
        String postResponse = postProperty(gatewayUrl, token, reqVO.getProductKey(), reqVO.getDeviceName(),
                identifier, value);

        IotDeviceHttpProtocolTestRespVO respVO = new IotDeviceHttpProtocolTestRespVO();
        respVO.setPointName(pointName);
        respVO.setIdentifier(identifier);
        respVO.setPointValue(value);
        respVO.setToken(token);
        respVO.setDataSourceResponse(dataSourceResponse);
        respVO.setPropertyPostResponse(postResponse);
        return success(respVO);
    }

    private String requestRealtimeData(IotDeviceHttpProtocolTestReqVO reqVO, String pointName) {
        String url = StrUtil.trimToNull(reqVO.getDataSourceUrl());
        if (url == null) {
            throw invalidParamException("外部数据源地址不能为空");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StrUtil.isNotBlank(reqVO.getDataSourceUsername())) {
            headers.setBasicAuth(reqVO.getDataSourceUsername(),
                    StrUtil.trimToEmpty(reqVO.getDataSourcePassword()), StandardCharsets.UTF_8);
        }
        String requestBody = JsonUtils.toJsonString(Map.of("PointName", List.of(pointName)));
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(requestBody, headers), String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw invalidParamException("外部数据源请求失败，status={}", response.getStatusCode());
        }
        String body = response.getBody();
        if (StrUtil.isBlank(body)) {
            throw invalidParamException("外部数据源响应为空");
        }
        return body;
    }

    private Object resolvePointValue(List<IotRealtimeDataPointDTO> points, String pointName) {
        if (CollUtil.isEmpty(points)) {
            throw invalidParamException("外部数据源未返回任何点位");
        }
        Object value = points.stream()
                .filter(item -> StrUtil.equals(pointName, StrUtil.trimToNull(item.getPointName())))
                .map(IotRealtimeDataPointDTO::getValue)
                .findFirst()
                .orElse(null);
        if (value == null) {
            throw invalidParamException("未匹配到 PointName={}", pointName);
        }
        return value;
    }

    private String requestToken(String gatewayUrl, String productKey, String deviceName, String deviceSecret) {
        IotDeviceAuthUtils.AuthInfo authInfo = IotDeviceAuthUtils.getAuthInfo(productKey, deviceName, deviceSecret);
        IotDeviceAuthReqDTO reqDTO = new IotDeviceAuthReqDTO();
        reqDTO.setClientId(authInfo.getClientId());
        reqDTO.setUsername(authInfo.getUsername());
        reqDTO.setPassword(authInfo.getPassword());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(gatewayUrl + "/auth", HttpMethod.POST,
                new HttpEntity<>(JsonUtils.toJsonString(reqDTO), headers), String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw invalidParamException("获取 token 失败，status={}", response.getStatusCode());
        }
        String body = response.getBody();
        if (StrUtil.isBlank(body)) {
            throw invalidParamException("获取 token 失败，响应为空");
        }
        JsonNode tokenNode = JsonUtils.parseTree(body).path("data").path("token");
        String token = tokenNode.asText();
        if (StrUtil.isBlank(token)) {
            throw invalidParamException("获取 token 失败，响应={}", body);
        }
        return token;
    }

    private String postProperty(String gatewayUrl, String token, String productKey, String deviceName,
                                String identifier, Object value) {
        String url = String.format("%s/topic/sys/%s/%s/thing/property/post",
                gatewayUrl, productKey, deviceName);
        Map<String, Object> payload = Map.of(
                "id", IdUtil.fastSimpleUUID(),
                "method", "thing.property.post",
                "version", "1.0",
                "params", Map.of(identifier, value)
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(JsonUtils.toJsonString(payload), headers), String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw invalidParamException("属性上报失败，status={}", response.getStatusCode());
        }
        return StrUtil.trimToEmpty(response.getBody());
    }

    private String normalizeBaseUrl(String baseUrl) {
        String trimmed = StrUtil.trimToNull(baseUrl);
        if (trimmed == null) {
            throw invalidParamException("网关地址不能为空");
        }
        return StrUtil.removeSuffix(trimmed, "/");
    }

}
