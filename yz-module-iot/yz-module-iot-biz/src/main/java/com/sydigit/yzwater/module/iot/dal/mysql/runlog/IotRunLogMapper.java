package com.sydigit.yzwater.module.iot.dal.mysql.runlog;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.runlog.IotRunLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运行日志 Mapper
 */
@Mapper
public interface IotRunLogMapper extends BaseMapperX<IotRunLogDO> {

    /**
     * 分页查询运行日志
     */
    default PageResult<IotRunLogDO> selectPage(IotRunLogPageReqVO reqVO,
                                               boolean hasAdminRole,
                                               Long loginUserId,
                                               Long loginDeptId) {
        return selectPage(reqVO, buildQueryWrapper(reqVO, hasAdminRole, loginUserId, loginDeptId)
                .orderByDesc(IotRunLogDO::getUpdateTime)
                .orderByDesc(IotRunLogDO::getId));
    }

    /**
     * 导出查询运行日志
     */
    default List<IotRunLogDO> selectListByReqVO(IotRunLogPageReqVO reqVO,
                                                boolean hasAdminRole,
                                                Long loginUserId,
                                                Long loginDeptId) {
        return selectList(buildQueryWrapper(reqVO, hasAdminRole, loginUserId, loginDeptId)
                .orderByDesc(IotRunLogDO::getUpdateTime)
                .orderByDesc(IotRunLogDO::getId));
    }

    /**
     * 查询日期前缀下最新记录编号
     */
    default String selectLatestLogNoByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return null;
        }
        IotRunLogDO log = selectOne(new LambdaQueryWrapperX<IotRunLogDO>()
                .likeRight(IotRunLogDO::getLogNo, prefix)
                .orderByDesc(IotRunLogDO::getLogNo)
                .last("LIMIT 1"));
        return log == null ? null : log.getLogNo();
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapperX<IotRunLogDO> buildQueryWrapper(IotRunLogPageReqVO reqVO,
                                                               boolean hasAdminRole,
                                                               Long loginUserId,
                                                               Long loginDeptId) {
        LambdaQueryWrapperX<IotRunLogDO> queryWrapper = new LambdaQueryWrapperX<IotRunLogDO>()
                .likeIfPresent(IotRunLogDO::getLogNo, reqVO.getLogNo())
                .likeIfPresent(IotRunLogDO::getTeamName, reqVO.getDutyTeamName())
                .likeIfPresent(IotRunLogDO::getCheckPeriod, reqVO.getCheckPeriod())
                .eqIfPresent(IotRunLogDO::getStationId, reqVO.getStationId())
                .likeIfPresent(IotRunLogDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(IotRunLogDO::getDispatchInstructionId, reqVO.getDispatchInstructionId());

        LocalDateTime[] runTime = reqVO.getRunTime();
        if (runTime != null && runTime.length == 2 && runTime[0] != null && runTime[1] != null) {
            queryWrapper.le(IotRunLogDO::getRunStartTime, runTime[1])
                    .ge(IotRunLogDO::getRunEndTime, runTime[0]);
        }
        if (StrUtil.isNotBlank(reqVO.getDispatchKeyword())) {
            String keyword = reqVO.getDispatchKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(IotRunLogDO::getDispatchInstructionNo, keyword)
                    .or()
                    .like(IotRunLogDO::getDispatchInstructionName, keyword)
                    .or()
                    .like(IotRunLogDO::getLogTitle, keyword));
        }

        if (!hasAdminRole) {
            if (loginDeptId == null) {
                queryWrapper.eqIfPresent(IotRunLogDO::getRecorderUserId, loginUserId);
            } else {
                queryWrapper.and(wrapper -> wrapper
                        .eq(IotRunLogDO::getRecorderUserId, loginUserId)
                        .or()
                        .eq(IotRunLogDO::getTeamId, loginDeptId));
            }
        }
        return queryWrapper;
    }
}
