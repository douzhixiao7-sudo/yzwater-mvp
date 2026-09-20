package com.sydigit.yzwater.module.dal.mysql.flood;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZbbDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 值班表头 Mapper
 */
@Mapper
public interface YzFxZbbMapper extends BaseMapperX<YzFxZbbDO> {

    /**
     * 按日期倒序获取列表
     */
    default List<YzFxZbbDO> selectListOrderByDateDesc() {
        return selectList(new LambdaQueryWrapper<YzFxZbbDO>()
                .orderByDesc(YzFxZbbDO::getStartDate)
                .orderByDesc(YzFxZbbDO::getEndDate)
                .orderByDesc(YzFxZbbDO::getCreateTime));
    }
}
