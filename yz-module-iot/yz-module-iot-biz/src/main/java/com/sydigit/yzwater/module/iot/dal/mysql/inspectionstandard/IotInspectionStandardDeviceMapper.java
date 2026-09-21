package com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard;

import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardDeviceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

@Mapper
public interface IotInspectionStandardDeviceMapper extends BaseMapperX<IotInspectionStandardDeviceDO> {

    /**
     * 根据标准 ID 查询关联设备。
     *
     * @param standardId 标准 ID
     * @return 关联设备列表
     */
    default List<IotInspectionStandardDeviceDO> selectListByStandardId(Long standardId) {
        if (standardId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionStandardDeviceDO>()
                .eq(IotInspectionStandardDeviceDO::getStandardId, standardId)
                .orderByAsc(IotInspectionStandardDeviceDO::getSort)
                .orderByAsc(IotInspectionStandardDeviceDO::getId));
    }

    /**
     * 根据标准 ID 删除关联设备。
     *
     * @param standardId 标准 ID
     */
    default void deleteByStandardId(Long standardId) {
        if (standardId == null) {
            return;
        }
        delete(new LambdaQueryWrapperX<IotInspectionStandardDeviceDO>()
                .eq(IotInspectionStandardDeviceDO::getStandardId, standardId));
    }

}
