package com.sydigit.yzwater.module.iot.dal.mysql.device;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDevicePropertyTagDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备属性标签 Mapper
 */
@Mapper
public interface IotDevicePropertyTagMapper extends BaseMapperX<IotDevicePropertyTagDO> {

    default List<IotDevicePropertyTagDO> selectListByDeviceId(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<IotDevicePropertyTagDO>()
                .eqIfPresent(IotDevicePropertyTagDO::getDeviceId, deviceId)
                .orderByAsc(IotDevicePropertyTagDO::getId));
    }

    default int deleteByDeviceId(Long deviceId) {
        return delete(new LambdaQueryWrapperX<IotDevicePropertyTagDO>()
                .eqIfPresent(IotDevicePropertyTagDO::getDeviceId, deviceId));
    }

}
