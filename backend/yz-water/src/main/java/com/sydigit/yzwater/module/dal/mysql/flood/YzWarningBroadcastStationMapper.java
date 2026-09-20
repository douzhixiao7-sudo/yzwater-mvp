package com.sydigit.yzwater.module.dal.mysql.flood;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.module.controller.admin.vo.flood.WarningBroadcastStationListReqVO;
import com.sydigit.yzwater.module.dal.dataobject.flood.YzWarningBroadcastStationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预警广播站 Mapper
 */
@Mapper
public interface YzWarningBroadcastStationMapper extends BaseMapperX<YzWarningBroadcastStationDO> {

    /**
     * 按查询条件获取列表
     */
    default List<YzWarningBroadcastStationDO> selectListByCondition(WarningBroadcastStationListReqVO reqVO) {
        String name = StrUtil.trimToNull(reqVO.getName());
        String code = StrUtil.trimToNull(reqVO.getCode());
        String adminDivision = StrUtil.trimToNull(reqVO.getAdminDivision());
        return selectList(new LambdaQueryWrapper<YzWarningBroadcastStationDO>()
                .like(StrUtil.isNotBlank(name), YzWarningBroadcastStationDO::getName, name)
                .like(StrUtil.isNotBlank(code), YzWarningBroadcastStationDO::getCode, code)
                .eq(StrUtil.isNotBlank(adminDivision), YzWarningBroadcastStationDO::getAdminDivision, adminDivision)
                .orderByAsc(YzWarningBroadcastStationDO::getSort)
                .orderByAsc(YzWarningBroadcastStationDO::getName)
                .orderByDesc(YzWarningBroadcastStationDO::getCreateTime));
    }
}
