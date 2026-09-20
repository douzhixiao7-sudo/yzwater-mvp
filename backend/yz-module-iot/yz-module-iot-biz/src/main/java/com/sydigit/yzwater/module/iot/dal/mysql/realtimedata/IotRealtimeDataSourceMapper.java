package com.sydigit.yzwater.module.iot.dal.mysql.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.source.IotRealtimeDataSourcePageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataSourceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 实时数据采集源 Mapper
 */
@Mapper
public interface IotRealtimeDataSourceMapper extends BaseMapperX<IotRealtimeDataSourceDO> {

    default PageResult<IotRealtimeDataSourceDO> selectPage(IotRealtimeDataSourcePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotRealtimeDataSourceDO>()
                .likeIfPresent(IotRealtimeDataSourceDO::getName, reqVO.getName())
                .eqIfPresent(IotRealtimeDataSourceDO::getCode, reqVO.getCode())
                .eqIfPresent(IotRealtimeDataSourceDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(IotRealtimeDataSourceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotRealtimeDataSourceDO::getId));
    }

    default IotRealtimeDataSourceDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<IotRealtimeDataSourceDO>()
                .eq(IotRealtimeDataSourceDO::getCode, code));
    }

    default List<IotRealtimeDataSourceDO> selectListByEnabled(Boolean enabled) {
        return selectList(IotRealtimeDataSourceDO::getEnabled, enabled);
    }

}