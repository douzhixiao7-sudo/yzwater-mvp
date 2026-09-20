package com.sydigit.yzwater.module.iot.dal.mysql.dispatchplan;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanObjectDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 调度方案操作对象 Mapper
 */
@Mapper
public interface IotDispatchPlanObjectMapper extends BaseMapperX<IotDispatchPlanObjectDO> {

    /**
     * 根据方案 ID 查询对象列表
     */
    default List<IotDispatchPlanObjectDO> selectListByPlanId(Long planId) {
        if (planId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotDispatchPlanObjectDO>()
                .eq(IotDispatchPlanObjectDO::getPlanId, planId)
                .orderByAsc(IotDispatchPlanObjectDO::getObjectSort)
                .orderByAsc(IotDispatchPlanObjectDO::getId));
    }

    /**
     * 根据方案 ID 集合查询对象列表
     */
    default List<IotDispatchPlanObjectDO> selectListByPlanIds(Collection<Long> planIds) {
        if (planIds == null || planIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotDispatchPlanObjectDO>()
                .in(IotDispatchPlanObjectDO::getPlanId, planIds)
                .orderByAsc(IotDispatchPlanObjectDO::getObjectSort)
                .orderByAsc(IotDispatchPlanObjectDO::getId));
    }

    /**
     * 根据方案 ID 删除对象
     */
    default void deleteByPlanId(Long planId) {
        if (planId == null) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotDispatchPlanObjectDO>()
                .eq(IotDispatchPlanObjectDO::getPlanId, planId));
    }

    /**
     * 根据方案 ID 集合删除对象
     */
    default void deleteByPlanIds(Collection<Long> planIds) {
        if (planIds == null || planIds.isEmpty()) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotDispatchPlanObjectDO>()
                .in(IotDispatchPlanObjectDO::getPlanId, planIds));
    }
}

