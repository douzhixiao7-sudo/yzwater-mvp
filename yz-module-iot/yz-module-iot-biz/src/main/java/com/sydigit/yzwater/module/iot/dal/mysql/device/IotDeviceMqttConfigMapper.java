package com.sydigit.yzwater.module.iot.dal.mysql.device;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备 MQTT 配置 Mapper
 */
@Mapper
public interface IotDeviceMqttConfigMapper extends BaseMapperX<IotDeviceMqttConfigDO> {

    default IotDeviceMqttConfigDO selectByDeviceId(Long deviceId) {
        return selectOne(IotDeviceMqttConfigDO::getDeviceId, deviceId);
    }

    default List<IotDeviceMqttConfigDO> selectList(IotMqttDeviceConfigListReqDTO reqDTO) {
        return selectList(new LambdaQueryWrapperX<IotDeviceMqttConfigDO>()
                .eqIfPresent(IotDeviceMqttConfigDO::getStatus, reqDTO.getStatus())
                .inIfPresent(IotDeviceMqttConfigDO::getSourceId, reqDTO.getSourceIds())
                .inIfPresent(IotDeviceMqttConfigDO::getDeviceId, reqDTO.getDeviceIds())
                .orderByAsc(IotDeviceMqttConfigDO::getSourceId)
                .orderByAsc(IotDeviceMqttConfigDO::getDeviceId));
    }

}
