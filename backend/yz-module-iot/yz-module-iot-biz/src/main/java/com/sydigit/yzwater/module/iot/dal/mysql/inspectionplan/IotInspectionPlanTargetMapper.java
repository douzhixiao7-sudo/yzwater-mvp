package com.sydigit.yzwater.module.iot.dal.mysql.inspectionplan;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanTargetDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface IotInspectionPlanTargetMapper extends BaseMapperX<IotInspectionPlanTargetDO> {

    /**
     * 根据计划 ID 查询对象明细。
     */
    default List<IotInspectionPlanTargetDO> selectListByPlanId(Long planId) {
        if (planId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionPlanTargetDO>()
                .eq(IotInspectionPlanTargetDO::getPlanId, planId)
                .orderByAsc(IotInspectionPlanTargetDO::getTargetSort)
                .orderByAsc(IotInspectionPlanTargetDO::getId));
    }

    /**
     * 根据计划 ID 集合查询对象明细。
     */
    default List<IotInspectionPlanTargetDO> selectListByPlanIds(Collection<Long> planIds) {
        if (planIds == null || planIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionPlanTargetDO>()
                .in(IotInspectionPlanTargetDO::getPlanId, planIds)
                .orderByAsc(IotInspectionPlanTargetDO::getTargetSort)
                .orderByAsc(IotInspectionPlanTargetDO::getId));
    }

    /**
     * 根据计划 ID 删除对象明细。
     */
    default void deleteByPlanId(Long planId) {
        if (planId == null) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotInspectionPlanTargetDO>()
                .eq(IotInspectionPlanTargetDO::getPlanId, planId));
    }

}
