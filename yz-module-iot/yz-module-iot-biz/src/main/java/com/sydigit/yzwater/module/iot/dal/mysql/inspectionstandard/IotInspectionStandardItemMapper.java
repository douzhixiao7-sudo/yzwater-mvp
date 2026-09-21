package com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface IotInspectionStandardItemMapper extends BaseMapperX<IotInspectionStandardItemDO> {

    /**
     * 根据标准 ID 查询检查项列表。
     */
    default List<IotInspectionStandardItemDO> selectListByStandardId(Long standardId) {
        if (standardId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionStandardItemDO>()
                .eq(IotInspectionStandardItemDO::getStandardId, standardId)
                .orderByAsc(IotInspectionStandardItemDO::getSort)
                .orderByAsc(IotInspectionStandardItemDO::getId));
    }

    /**
     * 根据适用对象 ID 集合查询检查项。
     */
    default List<IotInspectionStandardItemDO> selectListByTargetRefIds(Collection<Long> targetRefIds) {
        if (targetRefIds == null || targetRefIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionStandardItemDO>()
                .in(IotInspectionStandardItemDO::getTargetRefId, targetRefIds)
                .orderByAsc(IotInspectionStandardItemDO::getSort)
                .orderByAsc(IotInspectionStandardItemDO::getId));
    }

    /**
     * 根据标准 ID 删除检查项。
     */
    default void deleteByStandardId(Long standardId) {
        if (standardId == null) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotInspectionStandardItemDO>()
                .eq(IotInspectionStandardItemDO::getStandardId, standardId));
    }

}
