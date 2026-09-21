package com.sydigit.yzwater.module.iot.dal.mysql.device;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceLocationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备位置 Mapper
 */
@Mapper
public interface IotDeviceLocationMapper extends BaseMapperX<IotDeviceLocationDO> {

    default List<IotDeviceLocationDO> selectAll() {
        return selectList(new LambdaQueryWrapperX<IotDeviceLocationDO>()
                .orderByAsc(IotDeviceLocationDO::getSort)
                .orderByAsc(IotDeviceLocationDO::getId));
    }

    default Long selectCountByParentId(Long parentId) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceLocationDO>()
                .eq(IotDeviceLocationDO::getParentId, parentId));
    }

}
