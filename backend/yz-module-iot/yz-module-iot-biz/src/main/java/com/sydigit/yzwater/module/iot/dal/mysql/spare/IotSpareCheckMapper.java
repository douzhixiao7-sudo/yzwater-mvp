package com.sydigit.yzwater.module.iot.dal.mysql.spare;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareCheckDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 备件盘点记录 Mapper
 */
@Mapper
public interface IotSpareCheckMapper extends BaseMapperX<IotSpareCheckDO> {

    default PageResult<IotSpareCheckDO> selectPage(IotSpareCheckPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotSpareCheckDO>()
                .eqIfPresent(IotSpareCheckDO::getSpareId, reqVO.getSpareId())
                .eqIfPresent(IotSpareCheckDO::getResultStatus, reqVO.getResultStatus())
                .eqIfPresent(IotSpareCheckDO::getApplied, reqVO.getApplied())
                .betweenIfPresent(IotSpareCheckDO::getCheckTime, reqVO.getCheckTime())
                .betweenIfPresent(IotSpareCheckDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotSpareCheckDO::getId));
    }

    default long selectCountBySpareId(Long spareId) {
        return selectCount(IotSpareCheckDO::getSpareId, spareId);
    }

}
