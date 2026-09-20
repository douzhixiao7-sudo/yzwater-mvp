package com.sydigit.yzwater.module.iot.dal.mysql.dispatchplan;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchplan.vo.IotDispatchPlanPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 调度方案 Mapper
 */
@Mapper
public interface IotDispatchPlanMapper extends BaseMapperX<IotDispatchPlanDO> {

    /**
     * 分页查询调度方案
     */
    default PageResult<IotDispatchPlanDO> selectPage(IotDispatchPlanPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO)
                .orderByDesc(IotDispatchPlanDO::getPrepareTime)
                .orderByDesc(IotDispatchPlanDO::getId));
    }

    /**
     * 导出查询调度方案
     */
    default List<IotDispatchPlanDO> selectListByReqVO(IotDispatchPlanPageReqVO reqVO) {
        return selectList(buildQueryWrapper(reqVO)
                .orderByDesc(IotDispatchPlanDO::getPrepareTime)
                .orderByDesc(IotDispatchPlanDO::getId));
    }

    /**
     * 根据名称查询（用于唯一校验）
     */
    default IotDispatchPlanDO selectByPlanName(String planName) {
        if (StrUtil.isBlank(planName)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IotDispatchPlanDO>()
                .eq(IotDispatchPlanDO::getPlanName, planName));
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapperX<IotDispatchPlanDO> buildQueryWrapper(IotDispatchPlanPageReqVO reqVO) {
        return new LambdaQueryWrapperX<IotDispatchPlanDO>()
                .likeIfPresent(IotDispatchPlanDO::getPlanNo, reqVO.getPlanNo())
                .likeIfPresent(IotDispatchPlanDO::getPlanName, reqVO.getPlanName())
                .eqIfPresent(IotDispatchPlanDO::getPlanType, reqVO.getPlanType())
                .eqIfPresent(IotDispatchPlanDO::getStationId, reqVO.getStationId())
                .eqIfPresent(IotDispatchPlanDO::getPlanStatus, reqVO.getPlanStatus())
                .likeIfPresent(IotDispatchPlanDO::getPrepareUserName, reqVO.getPrepareUserName())
                .betweenIfPresent(IotDispatchPlanDO::getPrepareTime, reqVO.getPrepareTime());
    }
}
