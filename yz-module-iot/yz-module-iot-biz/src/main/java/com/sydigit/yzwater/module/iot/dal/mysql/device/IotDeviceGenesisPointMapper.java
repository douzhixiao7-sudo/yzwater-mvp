package com.sydigit.yzwater.module.iot.dal.mysql.device;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisPointPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceGenesisPointDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IoT 设备 GENESIS64 点位配置 Mapper
 */
@Mapper
public interface IotDeviceGenesisPointMapper extends BaseMapperX<IotDeviceGenesisPointDO> {

    default PageResult<IotDeviceGenesisPointDO> selectPage(IotDeviceGenesisPointPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceGenesisPointDO>()
                .eqIfPresent(IotDeviceGenesisPointDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(IotDeviceGenesisPointDO::getIdentifier, reqVO.getIdentifier())
                .likeIfPresent(IotDeviceGenesisPointDO::getName, reqVO.getName())
                .likeIfPresent(IotDeviceGenesisPointDO::getPointName, reqVO.getPointName())
                .eqIfPresent(IotDeviceGenesisPointDO::getStatus, reqVO.getStatus())
                .orderByAsc(IotDeviceGenesisPointDO::getSort)
                .orderByDesc(IotDeviceGenesisPointDO::getId));
    }

    default List<IotDeviceGenesisPointDO> selectListByDeviceIdsAndStatus(Collection<Long> deviceIds, Integer status) {
        return selectList(new LambdaQueryWrapperX<IotDeviceGenesisPointDO>()
                .in(IotDeviceGenesisPointDO::getDeviceId, deviceIds)
                .eq(IotDeviceGenesisPointDO::getStatus, status)
                .orderByAsc(IotDeviceGenesisPointDO::getSort)
                .orderByAsc(IotDeviceGenesisPointDO::getId));
    }

    default IotDeviceGenesisPointDO selectByDeviceIdAndIdentifier(Long deviceId, String identifier) {
        return selectOne(IotDeviceGenesisPointDO::getDeviceId, deviceId,
                IotDeviceGenesisPointDO::getIdentifier, identifier);
    }

    default IotDeviceGenesisPointDO selectByDeviceIdAndPointName(Long deviceId, String pointName) {
        return selectOne(IotDeviceGenesisPointDO::getDeviceId, deviceId,
                IotDeviceGenesisPointDO::getPointName, pointName);
    }

    default void updateByThingModelId(Long thingModelId, IotDeviceGenesisPointDO updateObj) {
        update(updateObj, new LambdaQueryWrapperX<IotDeviceGenesisPointDO>()
                .eq(IotDeviceGenesisPointDO::getThingModelId, thingModelId));
    }

}
