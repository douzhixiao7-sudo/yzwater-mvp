package com.sydigit.yzwater.module.iot.api.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.enums.RpcConstants;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.iot.core.biz.IotDeviceCommonApi;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisPointRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttPointRespDTO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceGenesisConfigDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceGenesisPointDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttConfigDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttMappingDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotMqttSourceDO;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceGenesisConfigService;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceGenesisPointService;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceMqttConfigService;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceMqttMappingService;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.product.IotProductService;
import com.sydigit.yzwater.module.iot.service.realtimedata.IotMqttSourceService;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * IoT 设备 API 实现类
 *
 * @author haohao
 */
@RestController
@Validated
@Primary // 保证优先匹配，因为 yz-iot-gateway 也有 IotDeviceCommonApi 的实现，并且也可能会被 biz 引入
public class IoTDeviceApiImpl implements IotDeviceCommonApi {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotProductService productService;
    @Resource
    @Lazy
    private IotDeviceGenesisConfigService genesisConfigService;
    @Resource
    @Lazy
    private IotDeviceGenesisPointService genesisPointService;
    @Resource
    @Lazy
    private IotMqttSourceService mqttSourceService;
    @Resource
    @Lazy
    private IotDeviceMqttConfigService deviceMqttConfigService;
    @Resource
    @Lazy
    private IotDeviceMqttMappingService deviceMqttMappingService;

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/auth")
    @PermitAll
    public CommonResult<Boolean> authDevice(@RequestBody IotDeviceAuthReqDTO authReqDTO) {
        return success(deviceService.authDevice(authReqDTO));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/get") // 特殊：方便调用，暂时使用 POST，实际更推荐 GET
    @PermitAll
    public CommonResult<IotDeviceRespDTO> getDevice(@RequestBody IotDeviceGetReqDTO getReqDTO) {
        IotDeviceDO device = getReqDTO.getId() != null ? deviceService.getDeviceFromCache(getReqDTO.getId())
                : deviceService.getDeviceFromCache(getReqDTO.getProductKey(), getReqDTO.getDeviceName());
        return success(BeanUtils.toBean(device, IotDeviceRespDTO.class, deviceDTO -> {
            IotProductDO product = productService.getProductFromCache(deviceDTO.getProductId());
            if (product != null) {
                deviceDTO.setCodecType(product.getCodecType());
            }
        }));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/genesis/config-list")
    @PermitAll
    @TenantIgnore
    public CommonResult<List<IotGenesisDeviceConfigRespDTO>> getGenesisDeviceConfigList(
            @RequestBody IotGenesisDeviceConfigListReqDTO listReqDTO) {
        List<IotDeviceGenesisConfigDO> configList = genesisConfigService.getDeviceGenesisConfigList(listReqDTO);
        if (CollUtil.isEmpty(configList)) {
            return success(List.of());
        }

        var deviceIds = convertSet(configList, IotDeviceGenesisConfigDO::getDeviceId);
        var deviceMap = deviceService.getDeviceMap(deviceIds);
        var pointMap = genesisPointService.getEnabledDeviceGenesisPointMapByDeviceIds(deviceIds);
        List<IotGenesisDeviceConfigRespDTO> result = new ArrayList<>(configList.size());
        for (IotDeviceGenesisConfigDO config : configList) {
            IotDeviceDO device = deviceMap.get(config.getDeviceId());
            if (device == null) {
                continue;
            }
            if (StrUtil.isNotEmpty(listReqDTO.getProtocolType())) {
                IotProductDO product = productService.getProductFromCache(device.getProductId());
                if (product == null || ObjUtil.notEqual(listReqDTO.getProtocolType(), product.getProtocolType())) {
                    continue;
                }
            }
            List<IotDeviceGenesisPointDO> pointList = pointMap.get(config.getDeviceId());
            if (CollUtil.isEmpty(pointList)) {
                continue;
            }
            IotGenesisDeviceConfigRespDTO configDTO = BeanUtils.toBean(config, IotGenesisDeviceConfigRespDTO.class,
                    o -> o.setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName())
                            .setPoints(BeanUtils.toBean(pointList, IotGenesisPointRespDTO.class)));
            result.add(configDTO);
        }
        return success(result);
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/mqtt/config-list")
    @PermitAll
    @TenantIgnore
    public CommonResult<List<IotMqttDeviceConfigRespDTO>> getMqttDeviceConfigList(
            @RequestBody IotMqttDeviceConfigListReqDTO listReqDTO) {
        List<IotDeviceMqttConfigDO> configList = deviceMqttConfigService.getDeviceMqttConfigList(listReqDTO);
        if (CollUtil.isEmpty(configList)) {
            return success(List.of());
        }

        var sourceIds = convertSet(configList, IotDeviceMqttConfigDO::getSourceId);
        var deviceIds = convertSet(configList, IotDeviceMqttConfigDO::getDeviceId);
        var sourceMap = mqttSourceService.getSourceMap(sourceIds);
        var deviceMap = deviceService.getDeviceMap(deviceIds);
        var mappingMap = deviceMqttMappingService.getEnabledDeviceMqttMappingMapByDeviceIds(deviceIds);
        List<IotMqttDeviceConfigRespDTO> result = new ArrayList<>(configList.size());
        for (IotDeviceMqttConfigDO config : configList) {
            IotMqttSourceDO source = sourceMap.get(config.getSourceId());
            IotDeviceDO device = deviceMap.get(config.getDeviceId());
            if (source == null || device == null) {
                continue;
            }
            if (Boolean.TRUE.equals(listReqDTO.getSourceEnabled()) && !Boolean.TRUE.equals(source.getEnabled())) {
                continue;
            }
            if (StrUtil.isNotEmpty(listReqDTO.getProtocolType())) {
                IotProductDO product = productService.getProductFromCache(device.getProductId());
                if (product == null || ObjUtil.notEqual(listReqDTO.getProtocolType(), product.getProtocolType())) {
                    continue;
                }
            }
            List<IotDeviceMqttMappingDO> mappingList = mappingMap.get(config.getDeviceId());
            if (CollUtil.isEmpty(mappingList)) {
                continue;
            }
            IotMqttDeviceConfigRespDTO configDTO = BeanUtils.toBean(config, IotMqttDeviceConfigRespDTO.class,
                    o -> o.setSourceId(source.getId())
                            .setSourceName(source.getName())
                            .setBrokerHost(source.getBrokerHost())
                            .setBrokerPort(source.getBrokerPort())
                            .setUsername(source.getUsername())
                            .setPassword(source.getPassword())
                            .setClientId(source.getClientId())
                            .setQos(source.getQos())
                            .setCleanSession(source.getCleanSession())
                            .setKeepAliveIntervalSeconds(source.getKeepAliveIntervalSeconds())
                            .setConnectTimeoutSeconds(source.getConnectTimeoutSeconds())
                            .setReconnectDelayMs(source.getReconnectDelayMs())
                            .setSslEnabled(source.getSslEnabled())
                            .setProductKey(device.getProductKey())
                            .setDeviceName(device.getDeviceName())
                            .setPoints(BeanUtils.toBean(mappingList, IotMqttPointRespDTO.class)));
            result.add(configDTO);
        }
        return success(result);
    }

}
