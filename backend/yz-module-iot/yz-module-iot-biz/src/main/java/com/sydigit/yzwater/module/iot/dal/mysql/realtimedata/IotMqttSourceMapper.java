package com.sydigit.yzwater.module.iot.dal.mysql.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource.IotMqttSourcePageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotMqttSourceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IoT MQTT 数据源 Mapper
 */
@Mapper
public interface IotMqttSourceMapper extends BaseMapperX<IotMqttSourceDO> {

    default PageResult<IotMqttSourceDO> selectPage(IotMqttSourcePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotMqttSourceDO>()
                .likeIfPresent(IotMqttSourceDO::getName, reqVO.getName())
                .eqIfPresent(IotMqttSourceDO::getCode, reqVO.getCode())
                .eqIfPresent(IotMqttSourceDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(IotMqttSourceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotMqttSourceDO::getId));
    }

    default IotMqttSourceDO selectByCode(String code) {
        return selectOne(IotMqttSourceDO::getCode, code);
    }

    default List<IotMqttSourceDO> selectListByEnabled(Boolean enabled) {
        return selectList(new LambdaQueryWrapperX<IotMqttSourceDO>()
                .eqIfPresent(IotMqttSourceDO::getEnabled, enabled)
                .orderByAsc(IotMqttSourceDO::getName)
                .orderByDesc(IotMqttSourceDO::getId));
    }

    default List<IotMqttSourceDO> selectListByIds(Collection<Long> ids) {
        return selectList(new LambdaQueryWrapperX<IotMqttSourceDO>()
                .inIfPresent(IotMqttSourceDO::getId, ids));
    }

}
