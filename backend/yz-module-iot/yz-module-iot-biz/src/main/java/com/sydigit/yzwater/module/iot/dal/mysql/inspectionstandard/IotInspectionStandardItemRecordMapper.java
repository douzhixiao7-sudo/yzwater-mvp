package com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardItemRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface IotInspectionStandardItemRecordMapper extends BaseMapperX<IotInspectionStandardItemRecordDO> {

    /**
     * 根据检查项 ID 集合查询记录模板列表。
     *
     * @param itemIds 检查项 ID 集合
     * @return 记录模板列表
     */
    default List<IotInspectionStandardItemRecordDO> selectListByStandardItemIds(Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionStandardItemRecordDO>()
                .in(IotInspectionStandardItemRecordDO::getStandardItemId, itemIds)
                .orderByAsc(IotInspectionStandardItemRecordDO::getSort)
                .orderByAsc(IotInspectionStandardItemRecordDO::getId));
    }

    /**
     * 根据检查项 ID 集合删除记录模板。
     *
     * @param itemIds 检查项 ID 集合
     */
    default void deleteByStandardItemIds(Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotInspectionStandardItemRecordDO>()
                .in(IotInspectionStandardItemRecordDO::getStandardItemId, itemIds));
    }

}
