package com.sydigit.yzwater.module.dal.mysql.flood;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxQxdwDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 市级防汛抢险队伍 Mapper
 */
@Mapper
public interface YzFxQxdwMapper extends BaseMapperX<YzFxQxdwDO> {

    /**
     * 按排序号查询
     */
    default List<YzFxQxdwDO> selectListOrderBySort() {
        return selectList(new LambdaQueryWrapper<YzFxQxdwDO>()
                .orderByAsc(YzFxQxdwDO::getSort)
                .orderByAsc(YzFxQxdwDO::getUnitName)
                .orderByDesc(YzFxQxdwDO::getCreateTime));
    }
}
