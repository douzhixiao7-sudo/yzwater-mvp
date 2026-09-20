package com.sydigit.yzwater.module.iot.dal.mysql.shiftschedule;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.mybatis.core.mapper.BaseMapperX;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleCalendarReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftSchedulePageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftschedule.IotShiftScheduleDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工排班 Mapper
 */
@Mapper
public interface IotShiftScheduleMapper extends BaseMapperX<IotShiftScheduleDO> {

    /**
     * 分页查询员工排班
     */
    default PageResult<IotShiftScheduleDO> selectPage(IotShiftSchedulePageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO)
                .orderByDesc(IotShiftScheduleDO::getScheduleDate)
                .orderByAsc(IotShiftScheduleDO::getDutyStartTime)
                .orderByDesc(IotShiftScheduleDO::getId));
    }

    /**
     * 导出查询员工排班
     */
    default List<IotShiftScheduleDO> selectListByReqVO(IotShiftSchedulePageReqVO reqVO) {
        return selectList(buildQueryWrapper(reqVO)
                .orderByDesc(IotShiftScheduleDO::getScheduleDate)
                .orderByAsc(IotShiftScheduleDO::getDutyStartTime)
                .orderByDesc(IotShiftScheduleDO::getId));
    }

    /**
     * 月历查询员工排班
     */
    default List<IotShiftScheduleDO> selectCalendarList(IotShiftScheduleCalendarReqVO reqVO,
                                                         LocalDate startDate,
                                                         LocalDate endDate) {
        LambdaQueryWrapper<IotShiftScheduleDO> queryWrapper =
                new LambdaQueryWrapperX<IotShiftScheduleDO>()
                .eqIfPresent(IotShiftScheduleDO::getTeamId, reqVO.getTeamId())
                .eqIfPresent(IotShiftScheduleDO::getShiftId, reqVO.getShiftId())
                .eqIfPresent(IotShiftScheduleDO::getDutyUserId, reqVO.getDutyUserId())
                .orderByAsc(IotShiftScheduleDO::getScheduleDate)
                .orderByAsc(IotShiftScheduleDO::getDutyStartTime)
                .orderByAsc(IotShiftScheduleDO::getId);
        queryWrapper.between(IotShiftScheduleDO::getScheduleDate, startDate, endDate);
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            String keyword = reqVO.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(IotShiftScheduleDO::getScheduleNo, keyword)
                    .or()
                    .like(IotShiftScheduleDO::getDutyUserName, keyword)
                    .or()
                    .like(IotShiftScheduleDO::getDutyMobile, keyword)
                    .or()
                    .like(IotShiftScheduleDO::getTeamName, keyword)
                    .or()
                    .like(IotShiftScheduleDO::getShiftName, keyword));
        }
        return selectList(queryWrapper);
    }

    /**
     * 查询日期前缀下最新排班编号
     */
    default String selectLatestScheduleNoByPrefix(String prefix) {
        if (StrUtil.isBlank(prefix)) {
            return null;
        }
        IotShiftScheduleDO schedule = selectOne(new LambdaQueryWrapperX<IotShiftScheduleDO>()
                .likeRight(IotShiftScheduleDO::getScheduleNo, prefix)
                .orderByDesc(IotShiftScheduleDO::getScheduleNo)
                .last("LIMIT 1"));
        return schedule == null ? null : schedule.getScheduleNo();
    }

    /**
     * 统计值班冲突数量
     */
    default Long selectConflictCount(Long dutyUserId,
                                     LocalDateTime dutyStartTime,
                                     LocalDateTime dutyEndTime,
                                     Long excludeId) {
        LambdaQueryWrapper<IotShiftScheduleDO> queryWrapper =
                new LambdaQueryWrapperX<IotShiftScheduleDO>()
                .eq(IotShiftScheduleDO::getDutyUserId, dutyUserId)
                .lt(IotShiftScheduleDO::getDutyStartTime, dutyEndTime)
                .gt(IotShiftScheduleDO::getDutyEndTime, dutyStartTime);
        if (excludeId != null) {
            queryWrapper.ne(IotShiftScheduleDO::getId, excludeId);
        }
        return selectCount(queryWrapper);
    }

    /**
     * 构建分页/导出查询条件
     */
    private LambdaQueryWrapperX<IotShiftScheduleDO> buildQueryWrapper(IotShiftSchedulePageReqVO reqVO) {
        LambdaQueryWrapperX<IotShiftScheduleDO> queryWrapper =
                new LambdaQueryWrapperX<IotShiftScheduleDO>()
                .eqIfPresent(IotShiftScheduleDO::getTeamId, reqVO.getTeamId())
                .eqIfPresent(IotShiftScheduleDO::getShiftId, reqVO.getShiftId())
                .eqIfPresent(IotShiftScheduleDO::getDutyUserId, reqVO.getDutyUserId())
                .eqIfPresent(IotShiftScheduleDO::getStatus, reqVO.getStatus())
                .likeIfPresent(IotShiftScheduleDO::getDutyUserName, reqVO.getDutyUserName());

        LocalDate[] scheduleDateRange = reqVO.getScheduleDateRange();
        if (scheduleDateRange != null && scheduleDateRange.length == 2
                && scheduleDateRange[0] != null && scheduleDateRange[1] != null) {
            queryWrapper.between(IotShiftScheduleDO::getScheduleDate, scheduleDateRange[0], scheduleDateRange[1]);
        }
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            String keyword = reqVO.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(IotShiftScheduleDO::getScheduleNo, keyword)
                    .or()
                    .like(IotShiftScheduleDO::getDutyUserName, keyword)
                    .or()
                    .like(IotShiftScheduleDO::getDutyMobile, keyword)
                    .or()
                    .like(IotShiftScheduleDO::getTeamName, keyword)
                    .or()
                    .like(IotShiftScheduleDO::getShiftName, keyword));
        }
        return queryWrapper;
    }
}
