package com.sydigit.yzwater.module.iot.dal.mysql.doc;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo.IotDeviceDocPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.doc.IotDeviceDocDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备技术资料 Mapper
 */
@Mapper
public interface IotDeviceDocMapper extends BaseMapperX<IotDeviceDocDO> {

    default PageResult<IotDeviceDocDO> selectPage(IotDeviceDocPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceDocDO>()
                .eqIfPresent(IotDeviceDocDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(IotDeviceDocDO::getDeviceCode, reqVO.getDeviceCode())
                .likeIfPresent(IotDeviceDocDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IotDeviceDocDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(IotDeviceDocDO::getDocType, reqVO.getDocType())
                .likeIfPresent(IotDeviceDocDO::getDocName, reqVO.getDocName())
                .eqIfPresent(IotDeviceDocDO::getFileFormat, reqVO.getFileFormat())
                .betweenIfPresent(IotDeviceDocDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotDeviceDocDO::getCreateTime)
                .orderByDesc(IotDeviceDocDO::getId));
    }

    default long selectCountByDeviceId(Long deviceId) {
        return selectCount(new LambdaQueryWrapperX<IotDeviceDocDO>()
                .eqIfPresent(IotDeviceDocDO::getDeviceId, deviceId));
    }
}
