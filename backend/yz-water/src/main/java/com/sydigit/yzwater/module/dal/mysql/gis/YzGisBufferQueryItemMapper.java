package com.sydigit.yzwater.module.dal.mysql.gis;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.gis.YzGisBufferQueryItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YzGisBufferQueryItemMapper extends BaseMapperX<YzGisBufferQueryItemDO> {

    default List<YzGisBufferQueryItemDO> selectByBufferId(Long bufferId) {
        return selectList(new LambdaQueryWrapper<YzGisBufferQueryItemDO>()
                .eq(YzGisBufferQueryItemDO::getBufferId, bufferId)
                .orderByAsc(YzGisBufferQueryItemDO::getFacilityName)
                .orderByAsc(YzGisBufferQueryItemDO::getId));
    }
}
