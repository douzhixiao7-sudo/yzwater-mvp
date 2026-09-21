package com.sydigit.yzwater.module.dal.mysql.flood;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxYaglDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预案管理 Mapper
 */
@Mapper
public interface YzFxYaglMapper extends BaseMapperX<YzFxYaglDO> {

    /**
     * 按排序号查询
     */
    default List<YzFxYaglDO> selectListOrderBySort() {
        return selectList(new LambdaQueryWrapper<YzFxYaglDO>()
                .orderByAsc(YzFxYaglDO::getSort)
                .orderByDesc(YzFxYaglDO::getCreateTime));
    }
}
