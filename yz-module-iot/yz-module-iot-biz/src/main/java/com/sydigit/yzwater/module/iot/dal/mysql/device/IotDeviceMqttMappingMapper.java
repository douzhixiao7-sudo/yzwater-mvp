package com.sydigit.yzwater.module.iot.dal.mysql.device;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttMappingPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttMappingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IoT 设备 MQTT 属性映射 Mapper
 */
@Mapper
public interface IotDeviceMqttMappingMapper extends BaseMapperX<IotDeviceMqttMappingDO> {

    default PageResult<IotDeviceMqttMappingDO> selectPage(IotDeviceMqttMappingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceMqttMappingDO>()
                .eqIfPresent(IotDeviceMqttMappingDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(IotDeviceMqttMappingDO::getIdentifier, reqVO.getIdentifier())
                .likeIfPresent(IotDeviceMqttMappingDO::getName, reqVO.getName())
                .likeIfPresent(IotDeviceMqttMappingDO::getPayloadKey, reqVO.getPayloadKey())
                .eqIfPresent(IotDeviceMqttMappingDO::getStatus, reqVO.getStatus())
                .orderByAsc(IotDeviceMqttMappingDO::getSort)
                .orderByDesc(IotDeviceMqttMappingDO::getId));
    }

    default List<IotDeviceMqttMappingDO> selectListByDeviceIdsAndStatus(Collection<Long> deviceIds, Integer status) {
        return selectList(new LambdaQueryWrapperX<IotDeviceMqttMappingDO>()
                .inIfPresent(IotDeviceMqttMappingDO::getDeviceId, deviceIds)
                .eqIfPresent(IotDeviceMqttMappingDO::getStatus, status)
                .orderByAsc(IotDeviceMqttMappingDO::getSort)
                .orderByAsc(IotDeviceMqttMappingDO::getId));
    }

    default IotDeviceMqttMappingDO selectByDeviceIdAndThingModelId(Long deviceId, Long thingModelId) {
        return selectOne(IotDeviceMqttMappingDO::getDeviceId, deviceId,
                IotDeviceMqttMappingDO::getThingModelId, thingModelId);
    }

    default IotDeviceMqttMappingDO selectByDeviceIdAndPayloadKey(Long deviceId, String payloadKey) {
        return selectOne(IotDeviceMqttMappingDO::getDeviceId, deviceId,
                IotDeviceMqttMappingDO::getPayloadKey, payloadKey);
    }

    default void updateByThingModelId(Long thingModelId, IotDeviceMqttMappingDO updateObj) {
        update(updateObj, new LambdaQueryWrapperX<IotDeviceMqttMappingDO>()
                .eq(IotDeviceMqttMappingDO::getThingModelId, thingModelId));
    }

    default void disableByDeviceIdsAndIdentifier(Collection<Long> deviceIds, String identifier, Integer status) {
        IotDeviceMqttMappingDO updateObj = new IotDeviceMqttMappingDO();
        updateObj.setStatus(status);
        update(updateObj, new LambdaQueryWrapperX<IotDeviceMqttMappingDO>()
                .inIfPresent(IotDeviceMqttMappingDO::getDeviceId, deviceIds)
                .eq(IotDeviceMqttMappingDO::getIdentifier, identifier));
    }

}
