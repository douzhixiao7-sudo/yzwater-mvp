package com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo.IotInspectionStandardPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IotInspectionStandardMapper extends BaseMapperX<IotInspectionStandardDO> {

    /**
     * 分页查询巡检标准。
     */
    default PageResult<IotInspectionStandardDO> selectPage(IotInspectionStandardPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotInspectionStandardDO>()
                .inIfPresent(IotInspectionStandardDO::getId, reqVO.getStandardIds())
                .likeIfPresent(IotInspectionStandardDO::getStandardName, reqVO.getStandardName())
                .eqIfPresent(IotInspectionStandardDO::getInspectionType, reqVO.getInspectionType())
                .eqIfPresent(IotInspectionStandardDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotInspectionStandardDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotInspectionStandardDO::getId));
    }

    /**
     * 按标准名称查询，用于名称唯一性校验。
     */
    default IotInspectionStandardDO selectByName(String standardName) {
        if (StrUtil.isBlank(standardName)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IotInspectionStandardDO>()
                .eq(IotInspectionStandardDO::getStandardName, standardName));
    }

}
