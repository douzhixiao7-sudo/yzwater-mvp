package com.sydigit.yzwater.module.dal.mysql.flood;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZzbcyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 防汛抗旱组织部成员 Mapper
 */
@Mapper
public interface YzFxZzbcyMapper extends BaseMapperX<YzFxZzbcyDO> {

    /**
     * 按岗位正序获取列表
     */
    default List<YzFxZzbcyDO> selectListOrderByPosition() {
        return selectListOrderByPosition(null, null);
    }

    /**
     * 按岗位正序获取列表（支持姓名、职务模糊查询）
     */
    default List<YzFxZzbcyDO> selectListOrderByPosition(String name, String title) {
        return selectList(new LambdaQueryWrapper<YzFxZzbcyDO>()
                .like(StrUtil.isNotBlank(name), YzFxZzbcyDO::getName, name)
                .like(StrUtil.isNotBlank(title), YzFxZzbcyDO::getTitle, title)
                .orderByAsc(YzFxZzbcyDO::getPosition)
                .orderByAsc(YzFxZzbcyDO::getSort)
                .orderByDesc(YzFxZzbcyDO::getCreateTime));
    }
}
