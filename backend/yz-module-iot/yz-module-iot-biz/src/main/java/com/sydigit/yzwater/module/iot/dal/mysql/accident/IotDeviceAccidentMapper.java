package com.sydigit.yzwater.module.iot.dal.mysql.accident;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo.IotDeviceAccidentPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.accident.IotDeviceAccidentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 设备事故 Mapper
 */
@Mapper
public interface IotDeviceAccidentMapper extends BaseMapperX<IotDeviceAccidentDO> {

    default PageResult<IotDeviceAccidentDO> selectPage(IotDeviceAccidentPageReqVO reqVO) {
        return selectPage(reqVO, buildWrapper(reqVO));
    }

    default List<IotDeviceAccidentDO> selectList(IotDeviceAccidentPageReqVO reqVO) {
        return selectList(buildWrapper(reqVO));
    }

    default long selectCountByDeviceId(Long deviceId) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceAccidentDO>()
                .eqIfPresent(IotDeviceAccidentDO::getDeviceId, deviceId));
    }

    private LambdaQueryWrapperX<IotDeviceAccidentDO> buildWrapper(IotDeviceAccidentPageReqVO reqVO) {
        return new LambdaQueryWrapperX<IotDeviceAccidentDO>()
                .eqIfPresent(IotDeviceAccidentDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotDeviceAccidentDO::getAccidentType, reqVO.getAccidentType())
                .likeIfPresent(IotDeviceAccidentDO::getResponsibleName, reqVO.getResponsibleName())
                .betweenIfPresent(IotDeviceAccidentDO::getAccidentTime, reqVO.getAccidentTime())
                .orderByDesc(IotDeviceAccidentDO::getAccidentTime)
                .orderByDesc(IotDeviceAccidentDO::getId);
    }
}
