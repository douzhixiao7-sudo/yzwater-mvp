package com.sydigit.yzwater.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttMappingPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttMappingSaveReqVO;
import com.sydigit.yzwater.module.iot.core.enums.IotProtocolTypeEnum;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttConfigDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttMappingDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMqttMappingMapper;
import com.sydigit.yzwater.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import com.sydigit.yzwater.module.iot.service.product.IotProductService;
import com.sydigit.yzwater.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertSet;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.filterList;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_MQTT_MAPPING_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_MQTT_MAPPING_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.THING_MODEL_NOT_EXISTS;

/**
 * IoT 设备 MQTT 属性映射 Service 实现类
 */
@Service
@Validated
public class IotDeviceMqttMappingServiceImpl implements IotDeviceMqttMappingService {

    @Resource
    private IotDeviceMqttMappingMapper deviceMqttMappingMapper;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotProductService productService;

    @Resource
    private IotThingModelService thingModelService;

    @Resource
    private IotDeviceMqttConfigService deviceMqttConfigService;

    @Override
    public Long createDeviceMqttMapping(IotDeviceMqttMappingSaveReqVO createReqVO) {
        validateDeviceMqttReady(createReqVO.getDeviceId());
        IotThingModelDO thingModel = validateThingModelExists(createReqVO.getThingModelId());
        validateDeviceMqttMappingUnique(createReqVO.getDeviceId(), createReqVO.getThingModelId(),
                createReqVO.getPayloadKey(), null);

        IotDeviceMqttMappingDO mapping = BeanUtils.toBean(createReqVO, IotDeviceMqttMappingDO.class,
                o -> o.setIdentifier(thingModel.getIdentifier())
                        .setName(thingModel.getName())
                        .setValueType(resolveThingModelValueType(thingModel)));
        deviceMqttMappingMapper.insert(mapping);
        return mapping.getId();
    }

    @Override
    public void updateDeviceMqttMapping(IotDeviceMqttMappingSaveReqVO updateReqVO) {
        validateDeviceMqttMappingExists(updateReqVO.getId());
        validateDeviceMqttReady(updateReqVO.getDeviceId());
        IotThingModelDO thingModel = validateThingModelExists(updateReqVO.getThingModelId());
        validateDeviceMqttMappingUnique(updateReqVO.getDeviceId(), updateReqVO.getThingModelId(),
                updateReqVO.getPayloadKey(), updateReqVO.getId());

        IotDeviceMqttMappingDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceMqttMappingDO.class,
                o -> o.setIdentifier(thingModel.getIdentifier())
                        .setName(thingModel.getName())
                        .setValueType(resolveThingModelValueType(thingModel)));
        deviceMqttMappingMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeviceMqttMapping(Long id) {
        validateDeviceMqttMappingExists(id);
        deviceMqttMappingMapper.deleteById(id);
    }

    @Override
    public IotDeviceMqttMappingDO getDeviceMqttMapping(Long id) {
        return deviceMqttMappingMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceMqttMappingDO> getDeviceMqttMappingPage(IotDeviceMqttMappingPageReqVO pageReqVO) {
        return deviceMqttMappingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotThingModelDO> getDeviceMqttMappingAvailableThingModelList(IotDeviceMqttMappingPageReqVO reqVO) {
        IotDeviceDO device = deviceService.validateDeviceExists(reqVO.getDeviceId());
        List<IotThingModelDO> thingModelList = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotDeviceMqttMappingDO> configuredList = deviceMqttMappingMapper.selectPage(reqVO).getList();
        Set<Long> configuredThingModelIds = new HashSet<>();
        configuredList.forEach(item -> {
            if (item.getThingModelId() != null) {
                configuredThingModelIds.add(item.getThingModelId());
            }
        });
        return filterList(thingModelList, item ->
                !configuredThingModelIds.contains(item.getId())
                        && (reqVO.getIdentifier() == null
                        || (item.getIdentifier() != null && item.getIdentifier().contains(reqVO.getIdentifier())))
                        && (reqVO.getName() == null
                        || (item.getName() != null && item.getName().contains(reqVO.getName()))));
    }

    @Override
    public Map<Long, List<IotDeviceMqttMappingDO>> getEnabledDeviceMqttMappingMapByDeviceIds(Collection<Long> deviceIds) {
        if (CollUtil.isEmpty(deviceIds)) {
            return Collections.emptyMap();
        }
        List<IotDeviceMqttMappingDO> mappingList = deviceMqttMappingMapper.selectListByDeviceIdsAndStatus(deviceIds,
                CommonStatusEnum.ENABLE.getStatus());
        return convertMultiMap(mappingList, IotDeviceMqttMappingDO::getDeviceId);
    }

    @Override
    public void updateDeviceMqttMappingByThingModel(Long thingModelId, String identifier, String name, String valueType) {
        IotDeviceMqttMappingDO updateObj = new IotDeviceMqttMappingDO()
                .setIdentifier(identifier)
                .setName(name)
                .setValueType(valueType);
        deviceMqttMappingMapper.updateByThingModelId(thingModelId, updateObj);
    }

    @Override
    public void disableDeviceMqttMappings(Long productId, String identifier) {
        String normalizedIdentifier = StrUtil.trimToNull(identifier);
        if (productId == null || normalizedIdentifier == null) {
            return;
        }
        List<IotDeviceDO> devices = deviceService.getDeviceListByProductId(productId);
        if (CollUtil.isEmpty(devices)) {
            return;
        }
        Set<Long> deviceIds = convertSet(devices, IotDeviceDO::getId);
        if (CollUtil.isEmpty(deviceIds)) {
            return;
        }
        deviceMqttMappingMapper.disableByDeviceIdsAndIdentifier(deviceIds, normalizedIdentifier,
                CommonStatusEnum.DISABLE.getStatus());
    }

    private void validateDeviceMqttReady(Long deviceId) {
        IotDeviceDO device = deviceService.validateDeviceExists(deviceId);
        IotProductDO product = productService.getProduct(device.getProductId());
        if (product == null || !IotProtocolTypeEnum.MQTT_SOURCE.getType().equals(product.getProtocolType())) {
            throw new IllegalArgumentException("只有 MQTT Source 协议的设备才允许配置 MQTT 属性映射");
        }
        IotDeviceMqttConfigDO mqttConfig = deviceMqttConfigService.getDeviceMqttConfigByDeviceId(deviceId);
        if (mqttConfig == null) {
            throw new IllegalArgumentException("请先保存设备 MQTT 采集配置，再维护属性映射");
        }
    }

    private IotThingModelDO validateThingModelExists(Long id) {
        IotThingModelDO thingModel = thingModelService.getThingModel(id);
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        return thingModel;
    }

    private void validateDeviceMqttMappingExists(Long id) {
        IotDeviceMqttMappingDO mapping = deviceMqttMappingMapper.selectById(id);
        if (mapping == null) {
            throw exception(DEVICE_MQTT_MAPPING_NOT_EXISTS);
        }
    }

    private void validateDeviceMqttMappingUnique(Long deviceId, Long thingModelId, String payloadKey, Long excludeId) {
        IotDeviceMqttMappingDO mappingByThingModel = deviceMqttMappingMapper.selectByDeviceIdAndThingModelId(deviceId,
                thingModelId);
        if (mappingByThingModel != null && ObjUtil.notEqual(mappingByThingModel.getId(), excludeId)) {
            throw exception(DEVICE_MQTT_MAPPING_EXISTS);
        }
        IotDeviceMqttMappingDO mappingByPayload = deviceMqttMappingMapper.selectByDeviceIdAndPayloadKey(deviceId,
                payloadKey);
        if (mappingByPayload != null && ObjUtil.notEqual(mappingByPayload.getId(), excludeId)) {
            throw exception(DEVICE_MQTT_MAPPING_EXISTS);
        }
    }

    private String resolveThingModelValueType(IotThingModelDO thingModel) {
        if (thingModel.getProperty() == null) {
            return "";
        }
        return StrUtil.blankToDefault(thingModel.getProperty().getDataType(), "");
    }

}
