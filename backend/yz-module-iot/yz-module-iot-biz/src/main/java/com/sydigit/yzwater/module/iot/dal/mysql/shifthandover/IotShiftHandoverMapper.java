package com.sydigit.yzwater.module.iot.dal.mysql.shifthandover;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverPageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shifthandover.IotShiftHandoverDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 交接班记录 Mapper
 */
@Mapper
public interface IotShiftHandoverMapper extends BaseMapperX<IotShiftHandoverDO> {

    /**
     * 分页查询
     */
    default PageResult<IotShiftHandoverDO> selectPage(IotShiftHandoverPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO)
                .orderByDesc(IotShiftHandoverDO::getHandoverTime)
                .orderByDesc(IotShiftHandoverDO::getId));
    }

    /**
     * 导出查询
     */
    default List<IotShiftHandoverDO> selectListByReqVO(IotShiftHandoverPageReqVO reqVO) {
        return selectList(buildQueryWrapper(reqVO)
                .orderByDesc(IotShiftHandoverDO::getHandoverTime)
                .orderByDesc(IotShiftHandoverDO::getId));
    }

    /**
     * 查询最新编号
     */
    default String selectLatestHandoverNoByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return null;
        }
        IotShiftHandoverDO latest = selectOne(new LambdaQueryWrapperX<IotShiftHandoverDO>()
                .likeRight(IotShiftHandoverDO::getHandoverNo, prefix)
                .orderByDesc(IotShiftHandoverDO::getHandoverNo)
                .last("LIMIT 1"));
        return latest == null ? null : latest.getHandoverNo();
    }

    /**
     * 按排班查询
     */
    default IotShiftHandoverDO selectByScheduleId(Long scheduleId) {
        return selectOne(new LambdaQueryWrapperX<IotShiftHandoverDO>()
                .eq(IotShiftHandoverDO::getScheduleId, scheduleId)
                .last("LIMIT 1"));
    }

    private LambdaQueryWrapperX<IotShiftHandoverDO> buildQueryWrapper(IotShiftHandoverPageReqVO reqVO) {
        LambdaQueryWrapperX<IotShiftHandoverDO> queryWrapper = new LambdaQueryWrapperX<IotShiftHandoverDO>()
                .eqIfPresent(IotShiftHandoverDO::getTeamId, reqVO.getTeamId())
                .eqIfPresent(IotShiftHandoverDO::getShiftId, reqVO.getShiftId())
                .eqIfPresent(IotShiftHandoverDO::getHandoverUserId, reqVO.getHandoverUserId())
                .eqIfPresent(IotShiftHandoverDO::getTakeoverUserId, reqVO.getTakeoverUserId())
                .eqIfPresent(IotShiftHandoverDO::getStatus, reqVO.getStatus());
        LocalDate[] handoverDateRange = reqVO.getHandoverDateRange();
        if (handoverDateRange != null && handoverDateRange.length == 2
                && handoverDateRange[0] != null && handoverDateRange[1] != null) {
            LocalDateTime startTime = handoverDateRange[0].atStartOfDay();
            LocalDateTime endTime = handoverDateRange[1].atTime(LocalTime.MAX);
            queryWrapper.between(IotShiftHandoverDO::getHandoverTime, startTime, endTime);
        }
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            String keyword = reqVO.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(IotShiftHandoverDO::getHandoverNo, keyword)
                    .or()
                    .like(IotShiftHandoverDO::getShiftName, keyword)
                    .or()
                    .like(IotShiftHandoverDO::getTeamName, keyword)
                    .or()
                    .like(IotShiftHandoverDO::getHandoverUserName, keyword)
                    .or()
                    .like(IotShiftHandoverDO::getTakeoverUserName, keyword)
                    .or()
                    .like(IotShiftHandoverDO::getDispatchInstructionNo, keyword)
                    .or()
                    .like(IotShiftHandoverDO::getDispatchInstructionName, keyword));
        }
        return queryWrapper;
    }
}

