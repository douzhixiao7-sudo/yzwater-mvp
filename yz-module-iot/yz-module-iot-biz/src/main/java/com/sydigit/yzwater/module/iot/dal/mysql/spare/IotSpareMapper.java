package com.sydigit.yzwater.module.iot.dal.mysql.spare;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSparePageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 备件台账 Mapper
 */
@Mapper
public interface IotSpareMapper extends BaseMapperX<IotSpareDO> {

    default PageResult<IotSpareDO> selectPage(IotSparePageReqVO reqVO) {
        LambdaQueryWrapperX<IotSpareDO> wrapper = new LambdaQueryWrapperX<IotSpareDO>()
                .likeIfPresent(IotSpareDO::getSpareName, reqVO.getSpareName())
                .likeIfPresent(IotSpareDO::getSpareSpec, reqVO.getSpareSpec())
                .likeIfPresent(IotSpareDO::getSpareModel, reqVO.getSpareModel())
                .eqIfPresent(IotSpareDO::getSpareType, reqVO.getSpareType())
                .likeIfPresent(IotSpareDO::getStorageLocation, reqVO.getStorageLocation())
                .likeIfPresent(IotSpareDO::getKeeperName, reqVO.getKeeperName())
                .betweenIfPresent(IotSpareDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotSpareDO::getId);
        if (reqVO.getDeviceId() != null) {
            wrapper.eq(IotSpareDO::getDeviceId, reqVO.getDeviceId());
        } else if (StrUtil.isNotBlank(reqVO.getDeviceType()) && NumberUtil.isInteger(reqVO.getDeviceType())) {
            wrapper.apply("device_id in (select id from iot_device where device_type = {0})",
                    Integer.valueOf(reqVO.getDeviceType()));
        }
        if (Boolean.TRUE.equals(reqVO.getWarning())) {
            wrapper.apply("stock_qty <= min_stock");
        }
        return selectPage(reqVO, wrapper);
    }

    default IotSpareDO selectBasicById(Long id) {
        if (id == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IotSpareDO>()
                .select(IotSpareDO::getId, IotSpareDO::getSpareName, IotSpareDO::getSpareSpec,
                        IotSpareDO::getSpareModel, IotSpareDO::getStorageLocation, IotSpareDO::getMinStock)
                .eq(IotSpareDO::getId, id));
    }

    default long selectCountByDeviceId(Long deviceId) {
        return selectCount(new LambdaQueryWrapperX<IotSpareDO>()
                .eqIfPresent(IotSpareDO::getDeviceId, deviceId));
    }
}
