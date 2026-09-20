package com.sydigit.yzwater.module.dal.mysql.flood;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.flood.FxZhbPageReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzFxZhbDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 防汛指挥部 Mapper
 */
@Mapper
public interface YzFxZhbMapper extends BaseMapperX<YzFxZhbDO> {

    default LambdaQueryWrapper<YzFxZhbDO> buildQueryWrapper(FxZhbPageReqVO reqVO) {
        return new LambdaQueryWrapper<YzFxZhbDO>()
                .like(StrUtil.isNotBlank(reqVO.getName()), YzFxZhbDO::getName, reqVO.getName())
                .eq(StrUtil.isNotBlank(reqVO.getAreaCode()), YzFxZhbDO::getAreaCode, reqVO.getAreaCode())
                .orderByDesc(YzFxZhbDO::getCreateTime);
    }
}
