package com.sydigit.yzwater.module.iot.dal.mysql.spare;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareIoDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 备件出入库记录 Mapper
 */
@Mapper
public interface IotSpareIoMapper extends BaseMapperX<IotSpareIoDO> {

    default PageResult<IotSpareIoDO> selectPage(IotSpareIoPageReqVO reqVO) {
        LambdaQueryWrapperX<IotSpareIoDO> wrapper = new LambdaQueryWrapperX<IotSpareIoDO>()
                .eqIfPresent(IotSpareIoDO::getSpareId, reqVO.getSpareId())
                .eqIfPresent(IotSpareIoDO::getIoType, reqVO.getIoType())
                .eqIfPresent(IotSpareIoDO::getAuditStatus, reqVO.getAuditStatus())
                .eqIfPresent(IotSpareIoDO::getUsageType, reqVO.getUsageType())
                .likeIfPresent(IotSpareIoDO::getOperatorName, reqVO.getOperatorName())
                .betweenIfPresent(IotSpareIoDO::getIoTime, reqVO.getIoTime())
                .betweenIfPresent(IotSpareIoDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotSpareIoDO::getIoTime)
                .orderByDesc(IotSpareIoDO::getId);
        if (reqVO.getDeviceId() != null) {
            wrapper.apply("spare_id in (select id from yz_equipment_spare where device_id = {0})",
                    reqVO.getDeviceId());
        } else if (StrUtil.isNotBlank(reqVO.getDeviceType()) && NumberUtil.isInteger(reqVO.getDeviceType())) {
            wrapper.apply(
                    "spare_id in (select s.id from yz_equipment_spare s " +
                            "left join iot_device d on s.device_id = d.id where d.device_type = {0})",
                    Integer.valueOf(reqVO.getDeviceType())
            );
        }
        return selectPage(reqVO, wrapper);
    }

    default long selectCountBySpareId(Long spareId) {
        return selectCount(IotSpareIoDO::getSpareId, spareId);
    }

    default long selectCountByUsage(String usageType, Long usageId) {
        return selectCount(new LambdaQueryWrapperX<IotSpareIoDO>()
                .eqIfPresent(IotSpareIoDO::getUsageType, usageType)
                .eqIfPresent(IotSpareIoDO::getUsageId, usageId));
    }

}
