package com.sydigit.yzwater.module.iot.service.thingmodel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.iot.controller.admin.thingmodel.vo.IotThingModelListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.thingmodel.vo.IotThingModelPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.thingmodel.vo.IotThingModelSaveReqVO;
import com.sydigit.yzwater.module.iot.convert.thingmodel.IotThingModelConvert;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataMappingDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.sydigit.yzwater.module.iot.dal.mysql.realtimedata.IotRealtimeDataMappingMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.thingmodel.IotThingModelMapper;
import com.sydigit.yzwater.module.iot.dal.redis.RedisKeyConstants;
import com.sydigit.yzwater.module.iot.enums.product.IotProductStatusEnum;
import com.sydigit.yzwater.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceGenesisPointService;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceMqttMappingService;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.product.IotProductService;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertSet;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 产品物模型 Service 实现类
 *
 * @author lijun
 */
@Service
@Validated
@Slf4j
public class IotThingModelServiceImpl implements IotThingModelService {

    @Resource
    private IotThingModelMapper thingModelMapper;

    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotProductService productService;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotRealtimeDataMappingMapper realtimeDataMappingMapper;
    @Resource
    @Lazy
    private IotDeviceGenesisPointService deviceGenesisPointService;
    @Resource
    @Lazy
    private IotDeviceMqttMappingService deviceMqttMappingService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createThingModel(IotThingModelSaveReqVO createReqVO) {
        // 1.1 校验功能标识符在同一产品下是否唯一
        validateIdentifierUnique(null, createReqVO.getProductId(), createReqVO.getIdentifier());
        // 1.2 功能名称在同一产品下是否唯一
        validateNameUnique(createReqVO.getProductId(), createReqVO.getName());
        // 1.3 校验产品状态，发布状态下，不允许新增功能
        validateProductStatus(createReqVO.getProductId());

        // 2. 插入数据库
        IotThingModelDO thingModel = IotThingModelConvert.INSTANCE.convert(createReqVO);
        thingModelMapper.insert(thingModel);

        // 3. 删除缓存
        deleteThingModelListCache(createReqVO.getProductId());
        return thingModel.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThingModel(IotThingModelSaveReqVO updateReqVO) {
        // 1.1 校验功能是否存在
        IotThingModelDO existThingModel = thingModelMapper.selectById(updateReqVO.getId());
        if (existThingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        // 1.2 校验功能标识符是否唯一
        validateIdentifierUnique(updateReqVO.getId(), updateReqVO.getProductId(), updateReqVO.getIdentifier());
        // 1.3 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(updateReqVO.getProductId());

        // 2. 更新数据库
        IotThingModelDO thingModel = IotThingModelConvert.INSTANCE.convert(updateReqVO);
        thingModelMapper.updateById(thingModel);

        // 3. 删除缓存
        deleteThingModelListCache(updateReqVO.getProductId());

        // 4. 同步更新 GENESIS64 点位冗余字段
        deviceGenesisPointService.updateDeviceGenesisPointByThingModel(updateReqVO.getId(),
                updateReqVO.getIdentifier(), updateReqVO.getName());
        deviceMqttMappingService.updateDeviceMqttMappingByThingModel(updateReqVO.getId(),
                updateReqVO.getIdentifier(), updateReqVO.getName(), resolveThingModelValueType(thingModel));

        // 5. 标识符变更时，禁用实时数据点位映射和 MQTT 映射
        if (Objects.equals(existThingModel.getType(), IotThingModelTypeEnum.PROPERTY.getType())
                && !StrUtil.equals(existThingModel.getIdentifier(), updateReqVO.getIdentifier())) {
            disableRealtimeDataMappings(updateReqVO.getProductId(), existThingModel.getIdentifier(),
                    "物模型标识符已变更");
            deviceMqttMappingService.disableDeviceMqttMappings(updateReqVO.getProductId(),
                    existThingModel.getIdentifier());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThingModel(Long id) {
        // 1.1 校验功能是否存在
        IotThingModelDO thingModel = thingModelMapper.selectById(id);
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        // 1.2 校验产品状态，发布状态下，不允许操作功能
        validateProductStatus(thingModel.getProductId());

        // 2. 删除功能
        thingModelMapper.deleteById(id);

        // 3. 删除缓存
        deleteThingModelListCache(thingModel.getProductId());

        // 4. 删除属性时，禁用实时数据点位映射和 MQTT 映射
        if (Objects.equals(thingModel.getType(), IotThingModelTypeEnum.PROPERTY.getType())) {
            disableRealtimeDataMappings(thingModel.getProductId(), thingModel.getIdentifier(),
                    "物模型已删除");
            deviceMqttMappingService.disableDeviceMqttMappings(thingModel.getProductId(), thingModel.getIdentifier());
        }
    }

    @Override
    public IotThingModelDO getThingModel(Long id) {
        return thingModelMapper.selectById(id);
    }

    @Override
    public List<IotThingModelDO> getThingModelListByProductId(Long productId) {
        return thingModelMapper.selectListByProductId(productId);
    }

    @Override
    public List<IotThingModelDO> getThingModelListByProductIdAndIdentifiers(Long productId, Collection<String> identifiers) {
        return thingModelMapper.selectListByProductIdAndIdentifiers(productId, identifiers);
    }

    @Override
    public List<IotThingModelDO> getThingModelListByProductIdAndType(Long productId, Integer type) {
        return thingModelMapper.selectListByProductIdAndType(productId, type);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.THING_MODEL_LIST, key = "#productId")
    @TenantIgnore // 忽略租户信息
    public List<IotThingModelDO> getThingModelListByProductIdFromCache(Long productId) {
        return thingModelMapper.selectListByProductId(productId);
    }

    @Override
    public PageResult<IotThingModelDO> getProductThingModelPage(IotThingModelPageReqVO pageReqVO) {
        return thingModelMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotThingModelDO> getThingModelList(IotThingModelListReqVO reqVO) {
        return thingModelMapper.selectList(reqVO);
    }

    @Override
    public void validateThingModelListExists(Long productId, Set<String> identifiers) {
        if (CollUtil.isEmpty(identifiers)) {
            return;
        }
        List<IotThingModelDO> thingModels = thingModelMapper.selectListByProductIdAndIdentifiers(
            productId, identifiers);
        Set<String> foundIdentifiers = convertSet(thingModels, IotThingModelDO::getIdentifier);
        for (String identifier : identifiers) {
            if (!foundIdentifiers.contains(identifier)) {
                throw exception(THING_MODEL_NOT_EXISTS);
            }
        }
    }

    private void validateIdentifierUnique(Long id, Long productId, String identifier) {
        // 1. 情况一：创建时校验
        if (id == null) {
            // 1.1 系统保留字段，不能用于标识符定义
            if (StrUtil.equalsAny(identifier, "set", "get", "post", "property", "event", "time", "value")) {
                throw exception(THING_MODEL_IDENTIFIER_INVALID);
            }
            // 1.2 校验唯一
            IotThingModelDO thingModel = thingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
            if (thingModel != null) {
                throw exception(THING_MODEL_IDENTIFIER_EXISTS);
            }
            return;
        }

        // 2. 情况二：更新时校验
        IotThingModelDO thingModel = thingModelMapper.selectByProductIdAndIdentifier(productId, identifier);
        if (thingModel != null && ObjectUtil.notEqual(thingModel.getId(), id)) {
            throw exception(THING_MODEL_IDENTIFIER_EXISTS);
        }
    }

    private void validateProductStatus(Long createReqVO) {
        IotProductDO product = productService.validateProductExists(createReqVO);
        if (Objects.equals(product.getStatus(), IotProductStatusEnum.PUBLISHED.getStatus())) {
            throw exception(PRODUCT_STATUS_NOT_ALLOW_THING_MODEL);
        }
    }

    private void validateNameUnique(Long productId, String name) {
        IotThingModelDO thingModel = thingModelMapper.selectByProductIdAndName(productId, name);
        if (thingModel != null) {
            throw exception(THING_MODEL_NAME_EXISTS);
        }
    }

    private void deleteThingModelListCache(Long productId) {
        // 保证 Spring AOP 触发
        getSelf().deleteThingModelListCache0(productId);
    }

    private void disableRealtimeDataMappings(Long productId, String identifier, String reason) {
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
        IotRealtimeDataMappingDO update = new IotRealtimeDataMappingDO();
        update.setEnabled(Boolean.FALSE);
        update.setSyncStatus("NOT_FOUND");
        update.setSyncMessage(reason);
        update.setLastSyncTime(LocalDateTime.now());
        realtimeDataMappingMapper.update(update, new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .in(IotRealtimeDataMappingDO::getDeviceId, deviceIds)
                .eq(IotRealtimeDataMappingDO::getIdentifier, normalizedIdentifier));
    }

    private String resolveThingModelValueType(IotThingModelDO thingModel) {
        if (thingModel == null || thingModel.getProperty() == null) {
            return "";
        }
        return StrUtil.blankToDefault(thingModel.getProperty().getDataType(), "");
    }

    @CacheEvict(value = RedisKeyConstants.THING_MODEL_LIST, key = "#productId")
    @TenantIgnore // 忽略租户信息
    public void deleteThingModelListCache0(Long productId) {
    }

    private IotThingModelServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
