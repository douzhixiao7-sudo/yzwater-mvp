package com.sydigit.yzwater.module.dal.mysql.flood;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZbbListDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 值班表体 Mapper
 */
@Mapper
public interface YzFxZbbListMapper extends BaseMapperX<YzFxZbbListDO> {

    /**
     * 根据表头 ID 查询明细
     */
    default List<YzFxZbbListDO> selectListByZbbId(String zbbId) {
        return selectList(new LambdaQueryWrapper<YzFxZbbListDO>()
                .eq(YzFxZbbListDO::getZbbId, zbbId)
                .orderByAsc(YzFxZbbListDO::getWeekDay)
                .orderByAsc(YzFxZbbListDO::getSort)
                .orderByDesc(YzFxZbbListDO::getCreateTime));
    }

    /**
     * 根据表头 ID 列表查询明细
     */
    default List<YzFxZbbListDO> selectListByZbbIds(Collection<String> zbbIds) {
        if (zbbIds == null || zbbIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<YzFxZbbListDO>()
                .in(YzFxZbbListDO::getZbbId, zbbIds)
                .orderByAsc(YzFxZbbListDO::getZbbId)
                .orderByAsc(YzFxZbbListDO::getWeekDay)
                .orderByAsc(YzFxZbbListDO::getSort)
                .orderByDesc(YzFxZbbListDO::getCreateTime));
    }
}
