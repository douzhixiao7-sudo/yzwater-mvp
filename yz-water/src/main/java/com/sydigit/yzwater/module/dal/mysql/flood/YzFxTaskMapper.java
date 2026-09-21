package com.sydigit.yzwater.module.dal.mysql.flood;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 风险隐患点 Mapper
 */
@Mapper
public interface YzFxTaskMapper extends BaseMapperX<YzFxTaskDO> {

    /**
     * 按排序号查询
     */
    default List<YzFxTaskDO> selectListOrderBySort() {
        return selectList(new LambdaQueryWrapper<YzFxTaskDO>()
                .orderByAsc(YzFxTaskDO::getSort)
                .orderByDesc(YzFxTaskDO::getCreateTime));
    }

    /**
     * 查询已标绘几何的任务（大屏图层）
     */
    default List<YzFxTaskDO> selectListWithGeomOrderBySort() {
        return selectList(new LambdaQueryWrapper<YzFxTaskDO>()
                .isNotNull(YzFxTaskDO::getGeom)
                .orderByAsc(YzFxTaskDO::getSort)
                .orderByDesc(YzFxTaskDO::getCreateTime));
    }
}
