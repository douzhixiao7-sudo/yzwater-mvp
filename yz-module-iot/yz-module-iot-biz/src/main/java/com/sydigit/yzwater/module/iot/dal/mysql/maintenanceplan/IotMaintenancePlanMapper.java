package com.sydigit.yzwater.module.iot.dal.mysql.maintenanceplan;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.maintenanceplan.IotMaintenancePlanDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 养护计划 Mapper
 */
@Mapper
public interface IotMaintenancePlanMapper extends BaseMapperX<IotMaintenancePlanDO> {

    default PageResult<IotMaintenancePlanDO> selectPage(IotMaintenancePlanPageReqVO reqVO, Collection<Long> deviceIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotMaintenancePlanDO>()
                .inIfPresent(IotMaintenancePlanDO::getDeviceId, deviceIds)
                .eqIfPresent(IotMaintenancePlanDO::getStationId, reqVO.getStationId())
                .eqIfPresent(IotMaintenancePlanDO::getStatus, reqVO.getStatus())
                .eqIfPresent(IotMaintenancePlanDO::getMaintainType, reqVO.getMaintainType())
                .betweenIfPresent(IotMaintenancePlanDO::getPlanDate, reqVO.getPlanDate())
                .orderByDesc(IotMaintenancePlanDO::getPlanDate)
                .orderByDesc(IotMaintenancePlanDO::getId));
    }

    default List<IotMaintenancePlanDO> selectListByDeviceIdsAndPlanDateRange(Collection<Long> deviceIds,
                                                                             LocalDate startDate,
                                                                             LocalDate endDate) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate[] range = startDate != null && endDate != null ? new LocalDate[]{startDate, endDate} : null;
        return selectList(new LambdaQueryWrapperX<IotMaintenancePlanDO>()
                .inIfPresent(IotMaintenancePlanDO::getDeviceId, deviceIds)
                .betweenIfPresent(IotMaintenancePlanDO::getPlanDate, range));
    }

    default long selectCountByDeviceIdAndPlanDate(Long deviceId, LocalDate planDate) {
        return selectCount(new LambdaQueryWrapperX<IotMaintenancePlanDO>()
                .eqIfPresent(IotMaintenancePlanDO::getDeviceId, deviceId)
                .eqIfPresent(IotMaintenancePlanDO::getPlanDate, planDate));
    }

    default long selectCountByDeviceId(Long deviceId) {
        return selectCount(new LambdaQueryWrapperX<IotMaintenancePlanDO>()
                .eqIfPresent(IotMaintenancePlanDO::getDeviceId, deviceId));
    }
}
