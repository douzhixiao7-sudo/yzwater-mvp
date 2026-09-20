package com.sydigit.yzwater.module.iot.dal.mysql.inspectiontask;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask.IotInspectionTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface IotInspectionTaskMapper extends BaseMapperX<IotInspectionTaskDO> {

    /**
     * 分页查询巡检任务。
     */
    default PageResult<IotInspectionTaskDO> selectPage(IotInspectionTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotInspectionTaskDO>()
                .likeIfPresent(IotInspectionTaskDO::getTaskNo, reqVO.getTaskNo())
                .likeIfPresent(IotInspectionTaskDO::getTaskName, reqVO.getTaskName())
                .eqIfPresent(IotInspectionTaskDO::getInspectionType, reqVO.getInspectionType())
                .eqIfPresent(IotInspectionTaskDO::getTaskStatus, reqVO.getTaskStatus())
                .eqIfPresent(IotInspectionTaskDO::getSourceType, reqVO.getSourceType())
                .eqIfPresent(IotInspectionTaskDO::getWorkflowStatus, reqVO.getWorkflowStatus())
                .eqIfPresent(IotInspectionTaskDO::getStationId, reqVO.getStationId())
                .eqIfPresent(IotInspectionTaskDO::getExecutorUserId, reqVO.getExecutorUserId())
                .betweenIfPresent(IotInspectionTaskDO::getPlanStartTime, reqVO.getPlanStartTime())
                .betweenIfPresent(IotInspectionTaskDO::getPlanEndTime, reqVO.getPlanEndTime())
                .orderByDesc(IotInspectionTaskDO::getUpdateTime)
                .orderByDesc(IotInspectionTaskDO::getId));
    }

    /**
     * 查询计划窗口是否已生成任务，用于幂等控制。
     */
    default IotInspectionTaskDO selectByPlanWindow(Long planId, LocalDateTime planStartTime, LocalDateTime planEndTime) {
        if (planId == null || planStartTime == null || planEndTime == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IotInspectionTaskDO>()
                .eq(IotInspectionTaskDO::getPlanId, planId)
                .eq(IotInspectionTaskDO::getSourceType, 1)
                .eq(IotInspectionTaskDO::getPlanStartTime, planStartTime)
                .eq(IotInspectionTaskDO::getPlanEndTime, planEndTime)
                .last("LIMIT 1"));
    }

    /**
     * 按计划查询最近一条自动生成任务。
     */
    default IotInspectionTaskDO selectLatestByPlanId(Long planId) {
        if (planId == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<IotInspectionTaskDO>()
                .eq(IotInspectionTaskDO::getPlanId, planId)
                .eq(IotInspectionTaskDO::getSourceType, 1)
                .orderByDesc(IotInspectionTaskDO::getId)
                .last("LIMIT 1"));
    }

    /**
     * 查询计划下全部自动生成任务。
     */
    default java.util.List<IotInspectionTaskDO> selectAutoTaskListByPlanId(Long planId) {
        if (planId == null) {
            return java.util.Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<IotInspectionTaskDO>()
                .eq(IotInspectionTaskDO::getPlanId, planId)
                .eq(IotInspectionTaskDO::getSourceType, 1)
                .orderByDesc(IotInspectionTaskDO::getId));
    }

    /**
     * 统计引用指定巡检标准的任务数量。
     */
    default long countByStandardId(Long standardId) {
        if (standardId == null) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<IotInspectionTaskDO>()
                .eq(IotInspectionTaskDO::getStandardId, standardId));
    }

    /**
     * 统计引用指定巡检线路的任务数量。
     */
    default long countByLineId(Long lineId) {
        if (lineId == null) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<IotInspectionTaskDO>()
                .eq(IotInspectionTaskDO::getLineId, lineId));
    }

}
