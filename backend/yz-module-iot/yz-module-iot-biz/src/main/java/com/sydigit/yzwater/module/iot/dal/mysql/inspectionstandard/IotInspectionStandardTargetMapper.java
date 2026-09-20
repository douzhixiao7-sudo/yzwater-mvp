package com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard;

import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardTargetDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface IotInspectionStandardTargetMapper extends BaseMapperX<IotInspectionStandardTargetDO> {

    /**
     * 根据标准ID查询适用对象列表。
     */
    default List<IotInspectionStandardTargetDO> selectListByStandardId(Long standardId) {
        if (standardId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionStandardTargetDO>()
                .eq(IotInspectionStandardTargetDO::getStandardId, standardId)
                .orderByAsc(IotInspectionStandardTargetDO::getSort)
                .orderByAsc(IotInspectionStandardTargetDO::getId));
    }

    /**
     * 根据标准ID集合查询适用对象列表。
     */
    default List<IotInspectionStandardTargetDO> selectListByStandardIds(Collection<Long> standardIds) {
        if (standardIds == null || standardIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionStandardTargetDO>()
                .in(IotInspectionStandardTargetDO::getStandardId, standardIds)
                .orderByAsc(IotInspectionStandardTargetDO::getSort)
                .orderByAsc(IotInspectionStandardTargetDO::getId));
    }

    /**
     * 根据标准ID删除适用对象。
     */
    default void deleteByStandardId(Long standardId) {
        if (standardId == null) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotInspectionStandardTargetDO>()
                .eq(IotInspectionStandardTargetDO::getStandardId, standardId));
    }

    /**
     * 根据条件查询标准ID集合。
     */
    default List<Long> selectStandardIdsByFilter(String stationId, String targetType) {
        List<IotInspectionStandardTargetDO> targets = selectList(new LambdaQueryWrapperX<IotInspectionStandardTargetDO>()
                .eqIfPresent(IotInspectionStandardTargetDO::getStationId, stationId)
                .eqIfPresent(IotInspectionStandardTargetDO::getTargetType, targetType)
                .select(IotInspectionStandardTargetDO::getStandardId));
        return CollectionUtils.convertList(targets, IotInspectionStandardTargetDO::getStandardId);
    }

}
