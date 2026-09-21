package com.sydigit.yzwater.module.dal.mysql.flood;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZrrDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 防汛责任人 Mapper
 */
@Mapper
public interface YzFxZrrMapper extends BaseMapperX<YzFxZrrDO> {

    default List<YzFxZrrDO> selectListByType(String type) {
        if (StrUtil.isBlank(type)) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapper<YzFxZrrDO>()
                .eq(YzFxZrrDO::getType, type)
                .orderByAsc(YzFxZrrDO::getSort)
                .orderByAsc(YzFxZrrDO::getDivisionCode)
                .orderByDesc(YzFxZrrDO::getCreateTime));
    }
}
