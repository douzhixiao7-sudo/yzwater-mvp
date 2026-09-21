package com.sydigit.yzwater.module.iot.dal.mysql.inspectionplan;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IotInspectionPlanMapper extends BaseMapperX<IotInspectionPlanDO> {

    /**
     * 分页查询巡检计划。
     */
    default PageResult<IotInspectionPlanDO> selectPage(IotInspectionPlanPageReqVO reqVO,
                                                       LocalDateTime cycleMonthStart,
                                                       LocalDateTime cycleMonthEnd) {
        LambdaQueryWrapperX<IotInspectionPlanDO> queryWrapper = new LambdaQueryWrapperX<IotInspectionPlanDO>()
                .likeIfPresent(IotInspectionPlanDO::getPlanName, reqVO.getPlanName())
                .eqIfPresent(IotInspectionPlanDO::getInspectionType, reqVO.getInspectionType())
                .eqIfPresent(IotInspectionPlanDO::getObjectType, reqVO.getObjectType())
                .eqIfPresent(IotInspectionPlanDO::getCycleUnit, reqVO.getCycleUnit())
                .eqIfPresent(IotInspectionPlanDO::getPlanStatus, reqVO.getPlanStatus())
                .orderByDesc(IotInspectionPlanDO::getId);
        if (cycleMonthStart != null && cycleMonthEnd != null) {
            queryWrapper.le(IotInspectionPlanDO::getPlanStartTime, cycleMonthEnd)
                    .ge(IotInspectionPlanDO::getPlanEndTime, cycleMonthStart);
        }
        return selectPage(reqVO, queryWrapper);
    }

    /**
     * 按计划名称查询，用于唯一性校验。
     */
    default IotInspectionPlanDO selectByName(String planName) {
        if (StrUtil.isBlank(planName)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IotInspectionPlanDO>()
                .eq(IotInspectionPlanDO::getPlanName, planName));
    }

    /**
     * 查询到期需要生成任务的启用计划。
     */
    default List<IotInspectionPlanDO> selectDueGeneratePlans(LocalDateTime now, int limit) {
        int safeLimit = Math.max(limit, 1);
        return selectList(new LambdaQueryWrapperX<IotInspectionPlanDO>()
                .eq(IotInspectionPlanDO::getEnableStatus, 0)
                .isNotNull(IotInspectionPlanDO::getPlanStartTime)
                .isNotNull(IotInspectionPlanDO::getPlanEndTime)
                .and(wrapper -> wrapper.le(IotInspectionPlanDO::getNextGenerateTime, now)
                        .or()
                        .isNull(IotInspectionPlanDO::getNextGenerateTime))
                .orderByAsc(IotInspectionPlanDO::getNextGenerateTime)
                .orderByAsc(IotInspectionPlanDO::getId)
                .last("LIMIT " + safeLimit));
    }

    /**
     * 推进计划任务生成游标。通过 expectedNextGenerateTime 做乐观并发控制。
     */
    default int updateGenerateCursor(Long planId,
                                     LocalDateTime expectedNextGenerateTime,
                                     LocalDateTime lastGenerateTime,
                                     LocalDateTime nextGenerateTime) {
        if (planId == null) {
            return 0;
        }
        LambdaUpdateWrapper<IotInspectionPlanDO> wrapper = new LambdaUpdateWrapper<IotInspectionPlanDO>()
                .eq(IotInspectionPlanDO::getId, planId)
                .set(IotInspectionPlanDO::getLastGenerateTime, lastGenerateTime)
                .set(IotInspectionPlanDO::getNextGenerateTime, nextGenerateTime)
                .setSql("generated_task_count = COALESCE(generated_task_count, 0) + 1");
        if (expectedNextGenerateTime == null) {
            wrapper.isNull(IotInspectionPlanDO::getNextGenerateTime);
        } else {
            wrapper.eq(IotInspectionPlanDO::getNextGenerateTime, expectedNextGenerateTime);
        }
        return update(null, wrapper);
    }

    /**
     * 统计引用指定巡检标准的计划数量。
     */
    default long countByStandardId(Long standardId) {
        if (standardId == null) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<IotInspectionPlanDO>()
                .eq(IotInspectionPlanDO::getStandardId, standardId));
    }

    /**
     * 统计引用指定巡检线路的计划数量。
     */
    default long countByLineId(Long lineId) {
        if (lineId == null) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<IotInspectionPlanDO>()
                .eq(IotInspectionPlanDO::getLineId, lineId));
    }

}
