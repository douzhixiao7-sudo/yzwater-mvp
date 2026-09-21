package com.sydigit.yzwater.module.iot.dal.mysql.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataMappingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IoT 实时数据点位映射 Mapper
 */
@Mapper
public interface IotRealtimeDataMappingMapper extends BaseMapperX<IotRealtimeDataMappingDO> {

    default PageResult<IotRealtimeDataMappingDO> selectPage(IotRealtimeDataMappingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .eqIfPresent(IotRealtimeDataMappingDO::getSourceId, reqVO.getSourceId())
                .likeIfPresent(IotRealtimeDataMappingDO::getPointName, reqVO.getPointName())
                .eqIfPresent(IotRealtimeDataMappingDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(IotRealtimeDataMappingDO::getIdentifier, reqVO.getIdentifier())
                .eqIfPresent(IotRealtimeDataMappingDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(IotRealtimeDataMappingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotRealtimeDataMappingDO::getId));
    }

    default PageResult<IotRealtimeDataMappingDO> selectPage(IotRealtimeDataMappingPageReqVO reqVO, Collection<Long> deviceIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .eqIfPresent(IotRealtimeDataMappingDO::getSourceId, reqVO.getSourceId())
                .likeIfPresent(IotRealtimeDataMappingDO::getPointName, reqVO.getPointName())
                .eqIfPresent(IotRealtimeDataMappingDO::getDeviceId, reqVO.getDeviceId())
                .inIfPresent(IotRealtimeDataMappingDO::getDeviceId, deviceIds)
                .likeIfPresent(IotRealtimeDataMappingDO::getIdentifier, reqVO.getIdentifier())
                .eqIfPresent(IotRealtimeDataMappingDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(IotRealtimeDataMappingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotRealtimeDataMappingDO::getId));
    }

    default IotRealtimeDataMappingDO selectBySourceIdAndPointName(Long sourceId, String pointName) {
        return selectOne(new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .eq(IotRealtimeDataMappingDO::getSourceId, sourceId)
                .eq(IotRealtimeDataMappingDO::getPointName, pointName));
    }

    default IotRealtimeDataMappingDO selectBySourceIdAndDeviceIdAndPointName(Long sourceId, Long deviceId, String pointName) {
        return selectOne(new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .eq(IotRealtimeDataMappingDO::getSourceId, sourceId)
                .eq(IotRealtimeDataMappingDO::getDeviceId, deviceId)
                .eq(IotRealtimeDataMappingDO::getPointName, pointName));
    }

    default List<IotRealtimeDataMappingDO> selectListBySourceId(Long sourceId) {
        return selectList(new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .eq(IotRealtimeDataMappingDO::getSourceId, sourceId)
                .orderByAsc(IotRealtimeDataMappingDO::getId));
    }

    default List<IotRealtimeDataMappingDO> selectListBySourceIdAndEnabled(Long sourceId, Boolean enabled) {
        return selectList(new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .eq(IotRealtimeDataMappingDO::getSourceId, sourceId)
                .eqIfPresent(IotRealtimeDataMappingDO::getEnabled, enabled)
                .orderByAsc(IotRealtimeDataMappingDO::getSort)
                .orderByAsc(IotRealtimeDataMappingDO::getId));
    }

    default List<IotRealtimeDataMappingDO> selectListBySourceIdAndDeviceId(Long sourceId, Long deviceId) {
        return selectList(new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .eq(IotRealtimeDataMappingDO::getSourceId, sourceId)
                .eq(IotRealtimeDataMappingDO::getDeviceId, deviceId)
                .orderByAsc(IotRealtimeDataMappingDO::getId));
    }

    default List<IotRealtimeDataMappingDO> selectListByDeviceId(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .eq(IotRealtimeDataMappingDO::getDeviceId, deviceId)
                .orderByAsc(IotRealtimeDataMappingDO::getId));
    }

    default int deleteBySourceIdAndDeviceId(Long sourceId, Long deviceId) {
        return delete(new LambdaQueryWrapperX<IotRealtimeDataMappingDO>()
                .eqIfPresent(IotRealtimeDataMappingDO::getSourceId, sourceId)
                .eqIfPresent(IotRealtimeDataMappingDO::getDeviceId, deviceId));
    }

}
