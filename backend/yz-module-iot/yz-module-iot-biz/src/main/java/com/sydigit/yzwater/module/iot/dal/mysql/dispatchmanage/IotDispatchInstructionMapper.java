package com.sydigit.yzwater.module.iot.dal.mysql.dispatchmanage;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManagePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceivePageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchmanage.IotDispatchInstructionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调度管理 Mapper
 */
@Mapper
public interface IotDispatchInstructionMapper extends BaseMapperX<IotDispatchInstructionDO> {

    /**
     * 分页查询调度指令
     */
    default PageResult<IotDispatchInstructionDO> selectPage(IotDispatchManagePageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO)
                .orderByDesc(IotDispatchInstructionDO::getCreateTime)
                .orderByDesc(IotDispatchInstructionDO::getId));
    }

    /**
     * 导出查询调度指令
     */
    default List<IotDispatchInstructionDO> selectListByReqVO(IotDispatchManagePageReqVO reqVO) {
        return selectList(buildQueryWrapper(reqVO)
                .orderByDesc(IotDispatchInstructionDO::getCreateTime)
                .orderByDesc(IotDispatchInstructionDO::getId));
    }

    /**
     * 调令接受分页查询
     */
    default PageResult<IotDispatchInstructionDO> selectReceivePage(IotDispatchReceivePageReqVO reqVO) {
        return selectPage(reqVO, buildReceiveQueryWrapper(reqVO)
                .orderByDesc(IotDispatchInstructionDO::getCreateTime)
                .orderByDesc(IotDispatchInstructionDO::getId));
    }

    /**
     * 调令接受导出查询
     */
    default List<IotDispatchInstructionDO> selectReceiveList(IotDispatchReceivePageReqVO reqVO) {
        return selectList(buildReceiveQueryWrapper(reqVO)
                .orderByDesc(IotDispatchInstructionDO::getCreateTime)
                .orderByDesc(IotDispatchInstructionDO::getId));
    }

    /**
     * 查询日期前缀下最新调度编号
     */
    default String selectLatestInstructionNoByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return null;
        }
        IotDispatchInstructionDO instruction = selectOne(new LambdaQueryWrapperX<IotDispatchInstructionDO>()
                .likeRight(IotDispatchInstructionDO::getInstructionNo, prefix)
                .orderByDesc(IotDispatchInstructionDO::getInstructionNo)
                .last("LIMIT 1"));
        return instruction == null ? null : instruction.getInstructionNo();
    }

    /**
     * 刷新逾期状态
     */
    @Update("UPDATE yz_dispatch_instruction " +
            "SET status = 3, update_time = NOW() " +
            "WHERE deleted = 0 AND status = 1 AND planned_finish_time < #{now}")
    int updateOverdueByNow(@Param("now") LocalDateTime now);

    /**
     * 统计方案被调度指令引用次数
     */
    default Long selectCountByPlanId(Long planId) {
        if (planId == null) {
            return 0L;
        }
        return selectCount(new LambdaQueryWrapperX<IotDispatchInstructionDO>()
                .apply("{0} = ANY (string_to_array(plan_ids, ',')::int8[])", planId));
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapperX<IotDispatchInstructionDO> buildQueryWrapper(IotDispatchManagePageReqVO reqVO) {
        return new LambdaQueryWrapperX<IotDispatchInstructionDO>()
                .eqIfPresent(IotDispatchInstructionDO::getStationId, reqVO.getStationId())
                .likeIfPresent(IotDispatchInstructionDO::getInstructionNo, reqVO.getInstructionNo())
                .likeIfPresent(IotDispatchInstructionDO::getIssueOrgName, reqVO.getIssueOrgName())
                .likeIfPresent(IotDispatchInstructionDO::getPlanSnapshotJson, reqVO.getPlanName())
                .eqIfPresent(IotDispatchInstructionDO::getStatus, reqVO.getStatus())
                .eqIfPresent(IotDispatchInstructionDO::getReceiverUserId, reqVO.getReceiverUserId())
                .betweenIfPresent(IotDispatchInstructionDO::getPlannedFinishTime, reqVO.getPlannedFinishTime());
    }

    /**
     * 构建调令接受查询条件
     */
    private LambdaQueryWrapperX<IotDispatchInstructionDO> buildReceiveQueryWrapper(IotDispatchReceivePageReqVO reqVO) {
        LambdaQueryWrapperX<IotDispatchInstructionDO> queryWrapper = new LambdaQueryWrapperX<IotDispatchInstructionDO>()
                .likeIfPresent(IotDispatchInstructionDO::getInstructionNo, reqVO.getInstructionNo())
                .likeIfPresent(IotDispatchInstructionDO::getInstructionName, reqVO.getInstructionName())
                .likeIfPresent(IotDispatchInstructionDO::getIssueOrgName, reqVO.getIssueOrgName())
                .eqIfPresent(IotDispatchInstructionDO::getStationId, reqVO.getStationId())
                .likeIfPresent(IotDispatchInstructionDO::getPlanSnapshotJson, reqVO.getPlanName())
                .eqIfPresent(IotDispatchInstructionDO::getStatus, reqVO.getStatus())
                .eqIfPresent(IotDispatchInstructionDO::getReceiveStatus, reqVO.getReceiveStatus())
                .betweenIfPresent(IotDispatchInstructionDO::getCreateTime, reqVO.getIssueTime());
        if (reqVO.getAccessUserId() != null) {
            queryWrapper.eq(IotDispatchInstructionDO::getExecutorUserId, reqVO.getAccessUserId());
        }
        return queryWrapper;
    }
}
