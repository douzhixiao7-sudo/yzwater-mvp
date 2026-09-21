package com.sydigit.yzwater.module.dal.mysql.flood;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxWzDwDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 防汛物资-单位 Mapper
 */
@Mapper
public interface YzFxWzDwMapper extends BaseMapperX<YzFxWzDwDO> {

    /**
     * 按排序查询
     */
    default List<YzFxWzDwDO> selectListOrderBySort() {
        return selectList(new LambdaQueryWrapper<YzFxWzDwDO>()
                .orderByAsc(YzFxWzDwDO::getSort)
                .orderByAsc(YzFxWzDwDO::getUnitName)
                .orderByDesc(YzFxWzDwDO::getCreateTime));
    }

    /**
     * 迁移位置字段用列表
     */
    default List<YzFxWzDwDO> selectListForGeomMigrate() {
        return selectList(new LambdaQueryWrapper<YzFxWzDwDO>()
                .select(YzFxWzDwDO::getId, YzFxWzDwDO::getPos, YzFxWzDwDO::getGeom)
                .orderByAsc(YzFxWzDwDO::getSort)
                .orderByDesc(YzFxWzDwDO::getCreateTime));
    }
}
