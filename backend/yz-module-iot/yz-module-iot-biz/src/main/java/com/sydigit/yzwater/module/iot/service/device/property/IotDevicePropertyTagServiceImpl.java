package com.sydigit.yzwater.module.iot.service.device.property;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyTagSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDevicePropertyTagDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDevicePropertyTagMapper;
import com.sydigit.yzwater.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_PROPERTY_TAG_IDENTIFIER_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_PROPERTY_TAG_NAME_DUPLICATE;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_PROPERTY_TAG_NAME_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_PROPERTY_TAG_PROPERTY_EMPTY;

/**
 * IoT 设备属性标签 Service 实现
 */
@Service
@Validated
public class IotDevicePropertyTagServiceImpl implements IotDevicePropertyTagService {

    @Resource
    private IotDevicePropertyTagMapper tagMapper;
    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotThingModelService thingModelService;

    @Override
    public Map<String, String> getTagNameMap(Long deviceId) {
        if (deviceId == null) {
            return Map.of();
        }
        List<IotDevicePropertyTagDO> tags = tagMapper.selectListByDeviceId(deviceId);
        if (CollUtil.isEmpty(tags)) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        for (IotDevicePropertyTagDO tag : tags) {
            if (tag == null) {
                continue;
            }
            String identifier = StrUtil.trimToNull(tag.getIdentifier());
            String tagName = StrUtil.trimToNull(tag.getTagName());
            if (identifier == null || tagName == null) {
                continue;
            }
            result.put(identifier, tagName);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTagNames(IotDevicePropertyTagSaveReqVO reqVO) {
        if (reqVO == null) {
            return;
        }
        Long deviceId = reqVO.getDeviceId();
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
        if (CollUtil.isEmpty(thingModels)) {
            throw exception(DEVICE_PROPERTY_TAG_PROPERTY_EMPTY);
        }
        Set<String> identifiers = new HashSet<>();
        for (IotThingModelDO thingModel : thingModels) {
            String identifier = StrUtil.trimToNull(thingModel.getIdentifier());
            if (identifier != null) {
                identifiers.add(identifier);
            }
        }
        List<IotDevicePropertyTagSaveReqVO.Item> items = reqVO.getItems();
        if (CollUtil.isEmpty(items)) {
            return;
        }
        List<IotDevicePropertyTagDO> existTags = tagMapper.selectListByDeviceId(deviceId);
        Map<String, IotDevicePropertyTagDO> existTagMap = new HashMap<>();
        Map<String, String> existTagNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(existTags)) {
            for (IotDevicePropertyTagDO tag : existTags) {
                if (tag == null) {
                    continue;
                }
                String identifier = StrUtil.trimToNull(tag.getIdentifier());
                if (identifier != null) {
                    existTagMap.put(identifier, tag);
                }
                String tagName = StrUtil.trimToNull(tag.getTagName());
                if (identifier != null && tagName != null) {
                    existTagNameMap.put(tagName, identifier);
                }
            }
        }
        Set<String> identifierSet = new HashSet<>();
        Set<String> tagNameSet = new HashSet<>();
        List<IotDevicePropertyTagDO> toSave = new ArrayList<>(items.size());
        for (IotDevicePropertyTagSaveReqVO.Item item : items) {
            if (item == null) {
                continue;
            }
            String identifier = StrUtil.trimToNull(item.getIdentifier());
            if (identifier == null || !identifiers.contains(identifier) || !identifierSet.add(identifier)) {
                throw exception(DEVICE_PROPERTY_TAG_IDENTIFIER_INVALID);
            }
            String tagName = StrUtil.trimToNull(item.getTagName());
            if (tagName == null) {
                throw exception(DEVICE_PROPERTY_TAG_NAME_EMPTY);
            }
            IotDevicePropertyTagDO tag = new IotDevicePropertyTagDO();
            tag.setDeviceId(deviceId);
            tag.setIdentifier(identifier);
            tag.setTagName(tagName);
            toSave.add(tag);
        }
        for (IotDevicePropertyTagDO tag : toSave) {
            String tagName = tag.getTagName();
            if (!tagNameSet.add(tagName)) {
                throw exception(DEVICE_PROPERTY_TAG_NAME_DUPLICATE);
            }
            String existIdentifier = existTagNameMap.get(tagName);
            if (existIdentifier != null
                    && !existIdentifier.equals(tag.getIdentifier())
                    && !identifierSet.contains(existIdentifier)) {
                throw exception(DEVICE_PROPERTY_TAG_NAME_DUPLICATE);
            }
        }
        if (toSave.size() == identifiers.size()) {
            tagMapper.deleteByDeviceId(deviceId);
            tagMapper.insertBatch(toSave);
            return;
        }
        for (IotDevicePropertyTagDO tag : toSave) {
            IotDevicePropertyTagDO exist = existTagMap.get(tag.getIdentifier());
            if (exist == null) {
                tagMapper.insert(tag);
                continue;
            }
            if (!StrUtil.equals(exist.getTagName(), tag.getTagName())) {
                IotDevicePropertyTagDO updateObj = new IotDevicePropertyTagDO();
                updateObj.setTagName(tag.getTagName());
                tagMapper.update(updateObj, new LambdaQueryWrapperX<IotDevicePropertyTagDO>()
                        .eq(IotDevicePropertyTagDO::getDeviceId, deviceId)
                        .eq(IotDevicePropertyTagDO::getIdentifier, tag.getIdentifier()));
            }
        }
    }

}
