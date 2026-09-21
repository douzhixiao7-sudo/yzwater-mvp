package com.sydigit.yzwater.module.iot.dal.mysql.device;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceGenesisConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备 GENESIS64 连接配置 Mapper
 */
@Mapper
public interface IotDeviceGenesisConfigMapper extends BaseMapperX<IotDeviceGenesisConfigDO> {

    default IotDeviceGenesisConfigDO selectByDeviceId(Long deviceId) {
        return selectOne(IotDeviceGenesisConfigDO::getDeviceId, deviceId);
    }

    default List<IotDeviceGenesisConfigDO> selectList(IotGenesisDeviceConfigListReqDTO reqDTO) {
        return selectList(new LambdaQueryWrapperX<IotDeviceGenesisConfigDO>()
                .eqIfPresent(IotDeviceGenesisConfigDO::getStatus, reqDTO.getStatus())
                .inIfPresent(IotDeviceGenesisConfigDO::getDeviceId, reqDTO.getDeviceIds()));
    }

}
