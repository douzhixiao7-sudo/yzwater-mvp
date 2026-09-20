package com.sydigit.yzwater.module.iot.dal.mysql.dispatchplan;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanObjectParamDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 调度方案对象参数 Mapper
 */
@Mapper
public interface IotDispatchPlanObjectParamMapper extends BaseMapperX<IotDispatchPlanObjectParamDO> {

    /**
     * 根据方案 ID 查询参数列表
     */
    default List<IotDispatchPlanObjectParamDO> selectListByPlanId(Long planId) {
        if (planId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotDispatchPlanObjectParamDO>()
                .eq(IotDispatchPlanObjectParamDO::getPlanId, planId)
                .orderByAsc(IotDispatchPlanObjectParamDO::getObjectId)
                .orderByAsc(IotDispatchPlanObjectParamDO::getParamSort)
                .orderByAsc(IotDispatchPlanObjectParamDO::getId));
    }

    /**
     * 根据方案 ID 集合查询参数列表
     */
    default List<IotDispatchPlanObjectParamDO> selectListByPlanIds(Collection<Long> planIds) {
        if (planIds == null || planIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotDispatchPlanObjectParamDO>()
                .in(IotDispatchPlanObjectParamDO::getPlanId, planIds)
                .orderByAsc(IotDispatchPlanObjectParamDO::getObjectId)
                .orderByAsc(IotDispatchPlanObjectParamDO::getParamSort)
                .orderByAsc(IotDispatchPlanObjectParamDO::getId));
    }

    /**
     * 根据对象 ID 集合查询参数列表
     */
    default List<IotDispatchPlanObjectParamDO> selectListByObjectIds(Collection<Long> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotDispatchPlanObjectParamDO>()
                .in(IotDispatchPlanObjectParamDO::getObjectId, objectIds)
                .orderByAsc(IotDispatchPlanObjectParamDO::getParamSort)
                .orderByAsc(IotDispatchPlanObjectParamDO::getId));
    }

    /**
     * 根据方案 ID 删除参数
     */
    default void deleteByPlanId(Long planId) {
        if (planId == null) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotDispatchPlanObjectParamDO>()
                .eq(IotDispatchPlanObjectParamDO::getPlanId, planId));
    }

    /**
     * 根据方案 ID 集合删除参数
     */
    default void deleteByPlanIds(Collection<Long> planIds) {
        if (planIds == null || planIds.isEmpty()) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotDispatchPlanObjectParamDO>()
                .in(IotDispatchPlanObjectParamDO::getPlanId, planIds));
    }
}

