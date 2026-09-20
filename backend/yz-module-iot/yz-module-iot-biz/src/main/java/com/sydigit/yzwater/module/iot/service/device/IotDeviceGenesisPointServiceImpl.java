package com.sydigit.yzwater.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisPointPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisPointSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceGenesisPointDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceGenesisPointMapper;
import com.sydigit.yzwater.module.iot.enums.thingmodel.IotThingModelTypeEnum;
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
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.filterList;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_GENESIS_POINT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_GENESIS_POINT_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.THING_MODEL_NOT_EXISTS;

/**
 * IoT 设备 GENESIS64 点位配置 Service 实现类
 */
@Service
@Validated
public class IotDeviceGenesisPointServiceImpl implements IotDeviceGenesisPointService {

    @Resource
    private IotDeviceGenesisPointMapper genesisPointMapper;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotThingModelService thingModelService;

    @Override
    public Long createDeviceGenesisPoint(IotDeviceGenesisPointSaveReqVO createReqVO) {
        deviceService.validateDeviceExists(createReqVO.getDeviceId());
        IotThingModelDO thingModel = validateThingModelExists(createReqVO.getThingModelId());
        validateDeviceGenesisPointUnique(createReqVO.getDeviceId(), thingModel.getIdentifier(),
                createReqVO.getPointName(), null);

        IotDeviceGenesisPointDO genesisPoint = BeanUtils.toBean(createReqVO, IotDeviceGenesisPointDO.class,
                o -> o.setIdentifier(thingModel.getIdentifier()).setName(thingModel.getName()));
        genesisPointMapper.insert(genesisPoint);
        return genesisPoint.getId();
    }

    @Override
    public void updateDeviceGenesisPoint(IotDeviceGenesisPointSaveReqVO updateReqVO) {
        validateDeviceGenesisPointExists(updateReqVO.getId());
        deviceService.validateDeviceExists(updateReqVO.getDeviceId());
        IotThingModelDO thingModel = validateThingModelExists(updateReqVO.getThingModelId());
        validateDeviceGenesisPointUnique(updateReqVO.getDeviceId(), thingModel.getIdentifier(),
                updateReqVO.getPointName(), updateReqVO.getId());

        IotDeviceGenesisPointDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceGenesisPointDO.class,
                o -> o.setIdentifier(thingModel.getIdentifier()).setName(thingModel.getName()));
        genesisPointMapper.updateById(updateObj);
    }

    @Override
    public void updateDeviceGenesisPointByThingModel(Long thingModelId, String identifier, String name) {
        IotDeviceGenesisPointDO updateObj = new IotDeviceGenesisPointDO().setIdentifier(identifier).setName(name);
        genesisPointMapper.updateByThingModelId(thingModelId, updateObj);
    }

    @Override
    public void deleteDeviceGenesisPoint(Long id) {
        validateDeviceGenesisPointExists(id);
        genesisPointMapper.deleteById(id);
    }

    @Override
    public IotDeviceGenesisPointDO getDeviceGenesisPoint(Long id) {
        return genesisPointMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceGenesisPointDO> getDeviceGenesisPointPage(IotDeviceGenesisPointPageReqVO pageReqVO) {
        return genesisPointMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotThingModelDO> getDeviceGenesisPointAvailableThingModelList(IotDeviceGenesisPointPageReqVO reqVO) {
        IotDeviceDO device = deviceService.validateDeviceExists(reqVO.getDeviceId());
        List<IotThingModelDO> thingModelList = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<IotDeviceGenesisPointDO> configuredPointList = genesisPointMapper.selectPage(reqVO).getList();
        Set<Long> configuredThingModelIds = new HashSet<>();
        configuredPointList.forEach(item -> {
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
    public Map<Long, List<IotDeviceGenesisPointDO>> getEnabledDeviceGenesisPointMapByDeviceIds(
            Collection<Long> deviceIds) {
        if (CollUtil.isEmpty(deviceIds)) {
            return Collections.emptyMap();
        }
        List<IotDeviceGenesisPointDO> pointList = genesisPointMapper.selectListByDeviceIdsAndStatus(deviceIds,
                CommonStatusEnum.ENABLE.getStatus());
        return convertMultiMap(pointList, IotDeviceGenesisPointDO::getDeviceId);
    }

    private IotThingModelDO validateThingModelExists(Long id) {
        IotThingModelDO thingModel = thingModelService.getThingModel(id);
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        return thingModel;
    }

    private void validateDeviceGenesisPointExists(Long id) {
        IotDeviceGenesisPointDO point = genesisPointMapper.selectById(id);
        if (point == null) {
            throw exception(DEVICE_GENESIS_POINT_NOT_EXISTS);
        }
    }

    private void validateDeviceGenesisPointUnique(Long deviceId, String identifier, String pointName, Long excludeId) {
        IotDeviceGenesisPointDO pointByIdentifier = genesisPointMapper.selectByDeviceIdAndIdentifier(deviceId,
                identifier);
        if (pointByIdentifier != null && ObjUtil.notEqual(pointByIdentifier.getId(), excludeId)) {
            throw exception(DEVICE_GENESIS_POINT_EXISTS);
        }
        IotDeviceGenesisPointDO pointByPointName = genesisPointMapper.selectByDeviceIdAndPointName(deviceId, pointName);
        if (pointByPointName != null && ObjUtil.notEqual(pointByPointName.getId(), excludeId)) {
            throw exception(DEVICE_GENESIS_POINT_EXISTS);
        }
    }

}
