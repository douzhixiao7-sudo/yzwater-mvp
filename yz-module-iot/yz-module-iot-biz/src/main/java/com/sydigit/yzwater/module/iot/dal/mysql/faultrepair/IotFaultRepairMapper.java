package com.sydigit.yzwater.module.iot.dal.mysql.faultrepair;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.faultrepair.IotFaultRepairDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 故障维修工单 Mapper
 */
@Mapper
public interface IotFaultRepairMapper extends BaseMapperX<IotFaultRepairDO> {

    default PageResult<IotFaultRepairDO> selectPage(IotFaultRepairPageReqVO reqVO) {
        return selectPage(reqVO, buildWrapper(reqVO));
    }

    default List<IotFaultRepairDO> selectList(IotFaultRepairPageReqVO reqVO) {
        return selectList(buildWrapper(reqVO));
    }

    default long selectCountByDeviceId(Long deviceId) {
        return selectCount(new LambdaQueryWrapperX<IotFaultRepairDO>()
                .eqIfPresent(IotFaultRepairDO::getDeviceId, deviceId));
    }

    default String selectMaxOrderNo(String prefix) {
        List<Object> list = selectObjs(new LambdaQueryWrapperX<IotFaultRepairDO>()
                .select(IotFaultRepairDO::getOrderNo)
                .likeRight(IotFaultRepairDO::getOrderNo, prefix)
                .orderByDesc(IotFaultRepairDO::getOrderNo)
                .last("LIMIT 1"));
        if (list == null || list.isEmpty() || list.get(0) == null) {
            return null;
        }
        return String.valueOf(list.get(0));
    }

    private LambdaQueryWrapperX<IotFaultRepairDO> buildWrapper(IotFaultRepairPageReqVO reqVO) {
        return new LambdaQueryWrapperX<IotFaultRepairDO>()
                .eqIfPresent(IotFaultRepairDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(IotFaultRepairDO::getDeviceName, reqVO.getDeviceName())
                .likeIfPresent(IotFaultRepairDO::getDeviceType, reqVO.getDeviceType())
                .likeIfPresent(IotFaultRepairDO::getReporterName, reqVO.getReporterName())
                .likeIfPresent(IotFaultRepairDO::getRepairName, reqVO.getRepairName())
                .eqIfPresent(IotFaultRepairDO::getRepairUserId, reqVO.getRepairUserId())
                .eqIfPresent(IotFaultRepairDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotFaultRepairDO::getFaultTime, reqVO.getFaultTime())
                .betweenIfPresent(IotFaultRepairDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotFaultRepairDO::getFaultTime)
                .orderByDesc(IotFaultRepairDO::getId);
    }
}
