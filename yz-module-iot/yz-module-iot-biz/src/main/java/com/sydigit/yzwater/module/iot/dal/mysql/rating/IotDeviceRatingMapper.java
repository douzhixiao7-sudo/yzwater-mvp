package com.sydigit.yzwater.module.iot.dal.mysql.rating;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo.IotDeviceRatingPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.rating.IotDeviceRatingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 设备评级 Mapper
 */
@Mapper
public interface IotDeviceRatingMapper extends BaseMapperX<IotDeviceRatingDO> {

    default PageResult<IotDeviceRatingDO> selectPage(IotDeviceRatingPageReqVO reqVO) {
        return selectPage(reqVO, buildWrapper(reqVO));
    }

    default List<IotDeviceRatingDO> selectList(IotDeviceRatingPageReqVO reqVO) {
        return selectList(buildWrapper(reqVO));
    }

    default long selectCountByDeviceId(Long deviceId) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceRatingDO>()
                .eqIfPresent(IotDeviceRatingDO::getDeviceId, deviceId));
    }

    private LambdaQueryWrapperX<IotDeviceRatingDO> buildWrapper(IotDeviceRatingPageReqVO reqVO) {
        return new LambdaQueryWrapperX<IotDeviceRatingDO>()
                .eqIfPresent(IotDeviceRatingDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IotDeviceRatingDO::getRatingResult, reqVO.getRatingResult())
                .likeIfPresent(IotDeviceRatingDO::getRatingUserName, reqVO.getRatingUserName())
                .betweenIfPresent(IotDeviceRatingDO::getRatingTime, reqVO.getRatingTime())
                .orderByDesc(IotDeviceRatingDO::getRatingTime)
                .orderByDesc(IotDeviceRatingDO::getId);
    }
}
