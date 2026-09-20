package com.sydigit.yzwater.module.iot.service.shiftschedule;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.validation.ValidationUtils;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleCalendarReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleImportExcelVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleImportRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftSchedulePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleShiftOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shiftschedule.vo.IotShiftScheduleTeamOptionRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftconfig.IotShiftConfigDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shifthandover.IotShiftHandoverDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftschedule.IotShiftScheduleDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam.IotShiftTeamDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam.IotShiftTeamMemberDO;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftconfig.IotShiftConfigMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shifthandover.IotShiftHandoverMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftschedule.IotShiftScheduleMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftteam.IotShiftTeamMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftteam.IotShiftTeamMemberMapper;
import com.sydigit.yzwater.module.system.api.dept.PostApi;
import com.sydigit.yzwater.module.system.api.dept.dto.PostRespDTO;
import com.sydigit.yzwater.module.system.api.user.AdminUserApi;
import com.sydigit.yzwater.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_SCHEDULE_CONFLICT;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_SCHEDULE_DELETE_FORBIDDEN_REFERENCED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_SCHEDULE_DUTY_TIME_REQUIRED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_SCHEDULE_MOBILE_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_SCHEDULE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_SCHEDULE_SHIFT_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_SCHEDULE_TEAM_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_SCHEDULE_USER_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_SCHEDULE_USER_NOT_IN_TEAM;

/**
 * 员工排班 Service 实现
 */
@Service
@Validated
public class IotShiftScheduleServiceImpl implements IotShiftScheduleService {

    private static final String SCHEDULE_NO_PREFIX = "PB-";
    private static final DateTimeFormatter DATE_COMPACT_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Resource
    private IotShiftScheduleMapper shiftScheduleMapper;
    @Resource
    private IotShiftConfigMapper shiftConfigMapper;
    @Resource
    private IotShiftHandoverMapper shiftHandoverMapper;
    @Resource
    private IotShiftTeamMapper shiftTeamMapper;
    @Resource
    private IotShiftTeamMemberMapper shiftTeamMemberMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PostApi postApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createShiftSchedule(IotShiftScheduleSaveReqVO createReqVO) {
        return createShiftScheduleInternal(createReqVO, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShiftSchedule(IotShiftScheduleSaveReqVO updateReqVO) {
        IotShiftScheduleDO existed = validateShiftScheduleExists(updateReqVO.getId());
        ShiftScheduleContext context = buildScheduleContext(updateReqVO, existed.getId(), false);

        IotShiftScheduleDO updateObj = new IotShiftScheduleDO();
        updateObj.setId(existed.getId());
        updateObj.setScheduleDate(context.scheduleDate);
        updateObj.setStationId(context.stationId);
        updateObj.setShiftId(context.shiftId);
        updateObj.setShiftName(context.shiftName);
        updateObj.setTeamId(context.teamId);
        updateObj.setTeamName(context.teamName);
        updateObj.setDutyUserId(context.dutyUserId);
        updateObj.setDutyUserName(context.dutyUserName);
        updateObj.setDutyMobile(context.dutyMobile);
        updateObj.setDutyPostName(context.dutyPostName);
        updateObj.setDutyStartTime(context.dutyStartTime);
        updateObj.setDutyEndTime(context.dutyEndTime);
        updateObj.setDutyLog(context.dutyLog);
        updateObj.setStatus(computeStatus(context.dutyStartTime, context.dutyEndTime));
        updateObj.setRemark(context.remark);
        shiftScheduleMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShiftSchedule(Long id) {
        validateShiftScheduleExists(id);
        validateCanDelete(id);
        shiftScheduleMapper.deleteById(id);
    }

    @Override
    public IotShiftScheduleRespVO getShiftSchedule(Long id) {
        IotShiftScheduleDO schedule = validateShiftScheduleExists(id);
        return buildResp(schedule, buildCreatorMap(Collections.singletonList(schedule)));
    }

    @Override
    public PageResult<IotShiftScheduleRespVO> getShiftSchedulePage(IotShiftSchedulePageReqVO pageReqVO) {
        PageResult<IotShiftScheduleDO> pageResult = shiftScheduleMapper.selectPage(pageReqVO);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(pageResult.getList());
        List<IotShiftScheduleRespVO> respList = CollectionUtils.convertList(pageResult.getList(),
                item -> buildResp(item, creatorMap));
        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public List<IotShiftScheduleRespVO> getShiftScheduleList(IotShiftSchedulePageReqVO reqVO) {
        List<IotShiftScheduleDO> list = shiftScheduleMapper.selectListByReqVO(reqVO);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(list);
        return CollectionUtils.convertList(list, item -> buildResp(item, creatorMap));
    }

    @Override
    public List<IotShiftScheduleRespVO> getShiftScheduleCalendar(IotShiftScheduleCalendarReqVO reqVO) {
        YearMonth yearMonth = resolveYearMonth(reqVO.getMonth());
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<IotShiftScheduleDO> list = shiftScheduleMapper.selectCalendarList(reqVO, startDate, endDate);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(list);
        return CollectionUtils.convertList(list, item -> buildResp(item, creatorMap));
    }

    @Override
    public List<IotShiftScheduleShiftOptionRespVO> getShiftOptions(String stationId) {
        List<IotShiftConfigDO> shiftList = shiftConfigMapper.selectList(new LambdaQueryWrapperX<IotShiftConfigDO>()
                .eq(IotShiftConfigDO::getStatus, 0)
                .eqIfPresent(IotShiftConfigDO::getStationId, trimToNull(stationId))
                .orderByAsc(IotShiftConfigDO::getStartTime)
                .orderByAsc(IotShiftConfigDO::getId));
        return CollectionUtils.convertList(shiftList, item -> {
            IotShiftScheduleShiftOptionRespVO option = new IotShiftScheduleShiftOptionRespVO();
            option.setId(item.getId());
            option.setShiftNo(item.getShiftNo());
            option.setShiftName(item.getShiftName());
            option.setStartTime(item.getStartTime());
            option.setEndTime(item.getEndTime());
            option.setCrossDay(Objects.equals(item.getCrossDay(), 1));
            return option;
        });
    }

    @Override
    public List<IotShiftScheduleTeamOptionRespVO> getTeamOptions(String stationId) {
        List<IotShiftTeamDO> teamList = shiftTeamMapper.selectList(new LambdaQueryWrapperX<IotShiftTeamDO>()
                .eq(IotShiftTeamDO::getStatus, 0)
                .eqIfPresent(IotShiftTeamDO::getStationId, trimToNull(stationId))
                .orderByAsc(IotShiftTeamDO::getTeamName)
                .orderByAsc(IotShiftTeamDO::getId));
        return CollectionUtils.convertList(teamList, item -> {
            IotShiftScheduleTeamOptionRespVO option = new IotShiftScheduleTeamOptionRespVO();
            option.setId(item.getId());
            option.setTeamNo(item.getTeamNo());
            option.setTeamName(item.getTeamName());
            return option;
        });
    }

    @Override
    public IotShiftScheduleImportRespVO importShiftSchedule(List<IotShiftScheduleImportExcelVO> importList) {
        if (importList == null || importList.isEmpty()) {
            return IotShiftScheduleImportRespVO.builder()
                    .successCount(0)
                    .failureCount(0)
                    .failureMessages(Collections.emptyList())
                    .build();
        }

        int successCount = 0;
        List<String> failureMessages = new ArrayList<>();
        for (int i = 0; i < importList.size(); i++) {
            IotShiftScheduleImportExcelVO row = importList.get(i);
            int rowNumber = i + 2;
            try {
                IotShiftScheduleSaveReqVO reqVO = convertImportRow(row);
                createShiftScheduleInternal(reqVO, true);
                successCount++;
            } catch (Exception ex) {
                String message = StrUtil.blankToDefault(ex.getMessage(), "导入失败");
                failureMessages.add("第 " + rowNumber + " 行：" + message);
            }
        }

        return IotShiftScheduleImportRespVO.builder()
                .successCount(successCount)
                .failureCount(failureMessages.size())
                .failureMessages(failureMessages)
                .build();
    }

    /**
     * 创建排班（内部复用）
     */
    private Long createShiftScheduleInternal(IotShiftScheduleSaveReqVO createReqVO, boolean allowShiftTimeFallback) {
        ShiftScheduleContext context = buildScheduleContext(createReqVO, null, allowShiftTimeFallback);

        IotShiftScheduleDO schedule = new IotShiftScheduleDO();
        schedule.setScheduleNo(generateScheduleNo());
        schedule.setScheduleDate(context.scheduleDate);
        schedule.setStationId(context.stationId);
        schedule.setShiftId(context.shiftId);
        schedule.setShiftName(context.shiftName);
        schedule.setTeamId(context.teamId);
        schedule.setTeamName(context.teamName);
        schedule.setDutyUserId(context.dutyUserId);
        schedule.setDutyUserName(context.dutyUserName);
        schedule.setDutyMobile(context.dutyMobile);
        schedule.setDutyPostName(context.dutyPostName);
        schedule.setDutyStartTime(context.dutyStartTime);
        schedule.setDutyEndTime(context.dutyEndTime);
        schedule.setDutyLog(context.dutyLog);
        schedule.setStatus(computeStatus(context.dutyStartTime, context.dutyEndTime));
        schedule.setRemark(context.remark);
        shiftScheduleMapper.insert(schedule);
        return schedule.getId();
    }

    /**
     * 构建排班上下文并完成校验
     */
    private ShiftScheduleContext buildScheduleContext(IotShiftScheduleSaveReqVO reqVO, Long excludeId,
                                                      boolean allowShiftTimeFallback) {
        ShiftScheduleContext context = new ShiftScheduleContext();
        context.scheduleDate = reqVO.getScheduleDate();
        context.stationId = trimToEmpty(reqVO.getStationId());

        IotShiftConfigDO shift = shiftConfigMapper.selectById(reqVO.getShiftId());
        if (shift == null) {
            throw exception(SHIFT_SCHEDULE_SHIFT_NOT_EXISTS);
        }
        context.shiftId = shift.getId();
        context.shiftName = trimToEmpty(shift.getShiftName());

        IotShiftTeamDO team = shiftTeamMapper.selectById(reqVO.getTeamId());
        if (team == null) {
            throw exception(SHIFT_SCHEDULE_TEAM_NOT_EXISTS);
        }
        context.teamId = team.getId();
        context.teamName = trimToEmpty(team.getTeamName());

        List<IotShiftTeamMemberDO> teamMembers = shiftTeamMemberMapper.selectListByTeamId(team.getId());
        if (teamMembers == null || teamMembers.isEmpty()) {
            throw exception(SHIFT_SCHEDULE_USER_NOT_IN_TEAM);
        }
        IotShiftTeamMemberDO leaderMember = resolveLeaderMember(teamMembers);
        if (leaderMember == null || leaderMember.getUserId() == null) {
            throw exception(SHIFT_SCHEDULE_USER_NOT_IN_TEAM);
        }
        Set<Long> teamUserIds = CollectionUtils.convertSet(teamMembers, IotShiftTeamMemberDO::getUserId,
                item -> item != null && item.getUserId() != null);
        Map<Long, AdminUserRespDTO> teamUserMap = teamUserIds.isEmpty() ? Collections.emptyMap() : adminUserApi.getUserMap(teamUserIds);
        AdminUserRespDTO leaderUser = teamUserMap.get(leaderMember.getUserId());
        if (leaderUser == null) {
            throw exception(SHIFT_SCHEDULE_USER_NOT_EXISTS);
        }
        context.dutyUserId = leaderMember.getUserId();
        context.dutyUserName = resolveTeamMemberNames(teamMembers, teamUserMap);
        context.dutyMobile = resolveDutyUserMobile(leaderMember, leaderUser);
        context.dutyPostName = resolveDutyUserPostName(leaderUser);

        LocalDateTime customStart = reqVO.getDutyStartTime();
        LocalDateTime customEnd = reqVO.getDutyEndTime();
        if (customStart != null && customEnd != null) {
            LocalDateTime normalizedStart = customStart;
            LocalDateTime normalizedEnd = customEnd;
            if (!normalizedEnd.isAfter(normalizedStart)) {
                normalizedEnd = normalizedEnd.plusDays(1);
            }
            context.scheduleDate = normalizedStart.toLocalDate();
            context.dutyStartTime = normalizedStart;
            context.dutyEndTime = normalizedEnd;
        } else if (allowShiftTimeFallback && customStart == null && customEnd == null) {
            LocalDateTime startTime = LocalDateTime.of(context.scheduleDate, shift.getStartTime());
            LocalDateTime endTime = LocalDateTime.of(context.scheduleDate, shift.getEndTime());
            boolean crossDay = Objects.equals(shift.getCrossDay(), 1) || !shift.getEndTime().isAfter(shift.getStartTime());
            if (crossDay) {
                endTime = endTime.plusDays(1);
            }
            context.dutyStartTime = startTime;
            context.dutyEndTime = endTime;
        } else {
            throw exception(SHIFT_SCHEDULE_DUTY_TIME_REQUIRED);
        }

        validateConflict(context.dutyUserId, context.dutyStartTime, context.dutyEndTime, excludeId);
        context.dutyLog = trimToEmpty(reqVO.getDutyLog());
        context.remark = trimToEmpty(reqVO.getRemark());
        return context;
    }

    /**
     * 校验排班是否存在
     */
    private IotShiftScheduleDO validateShiftScheduleExists(Long id) {
        IotShiftScheduleDO schedule = shiftScheduleMapper.selectById(id);
        if (schedule == null) {
            throw exception(SHIFT_SCHEDULE_NOT_EXISTS);
        }
        return schedule;
    }

    private IotShiftTeamMemberDO resolveLeaderMember(List<IotShiftTeamMemberDO> teamMembers) {
        for (IotShiftTeamMemberDO member : teamMembers) {
            if (member != null && Objects.equals(member.getIsLeader(), 1)) {
                return member;
            }
        }
        return null;
    }

    private String resolveTeamMemberNames(List<IotShiftTeamMemberDO> teamMembers, Map<Long, AdminUserRespDTO> userMap) {
        LinkedHashSet<String> memberNames = new LinkedHashSet<>();
        for (IotShiftTeamMemberDO member : teamMembers) {
            if (member == null || member.getUserId() == null) {
                continue;
            }
            String memberName = trimToNull(member.getUserName());
            if (memberName == null) {
                AdminUserRespDTO user = userMap.get(member.getUserId());
                memberName = user == null ? null : trimToNull(user.getNickname());
            }
            if (memberName == null) {
                memberName = String.valueOf(member.getUserId());
            }
            memberNames.add(memberName);
        }
        return memberNames.isEmpty() ? "" : String.join("、", memberNames);
    }

    /**
     * 校验值班冲突
     */
    private void validateConflict(Long dutyUserId, LocalDateTime dutyStartTime, LocalDateTime dutyEndTime, Long excludeId) {
        Long conflictCount = shiftScheduleMapper.selectConflictCount(dutyUserId, dutyStartTime, dutyEndTime, excludeId);
        if (conflictCount != null && conflictCount > 0) {
            throw exception(SHIFT_SCHEDULE_CONFLICT);
        }
    }

    /**
     * 校验排班是否可删除：已关联交接班记录时禁止删除
     */
    private void validateCanDelete(Long scheduleId) {
        IotShiftHandoverDO handover = shiftHandoverMapper.selectByScheduleId(scheduleId);
        if (handover != null) {
            throw exception(SHIFT_SCHEDULE_DELETE_FORBIDDEN_REFERENCED);
        }
    }

    /**
     * 生成排班编号：PB-YYYYMMDD-XXX
     */
    private String generateScheduleNo() {
        String prefix = SCHEDULE_NO_PREFIX + LocalDate.now().format(DATE_COMPACT_FORMATTER) + "-";
        String latest = shiftScheduleMapper.selectLatestScheduleNoByPrefix(prefix);
        int nextSeq = parseNextSeq(latest);
        return prefix + String.format("%03d", nextSeq);
    }

    private int parseNextSeq(String latest) {
        if (StrUtil.isBlank(latest)) {
            return 1;
        }
        int index = latest.lastIndexOf('-');
        if (index < 0 || index >= latest.length() - 1) {
            return 1;
        }
        String suffix = latest.substring(index + 1);
        if (!StrUtil.isNumeric(suffix)) {
            return 1;
        }
        return Integer.parseInt(suffix) + 1;
    }

    /**
     * 状态自动计算
     */
    private Integer computeStatus(LocalDateTime dutyStartTime, LocalDateTime dutyEndTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(dutyStartTime)) {
            return 0;
        }
        if (now.isBefore(dutyEndTime)) {
            return 1;
        }
        return 2;
    }

    /**
     * 构建响应对象
     */
    private IotShiftScheduleRespVO buildResp(IotShiftScheduleDO schedule, Map<Long, AdminUserRespDTO> creatorMap) {
        IotShiftScheduleRespVO respVO = new IotShiftScheduleRespVO();
        respVO.setId(schedule.getId());
        respVO.setScheduleNo(schedule.getScheduleNo());
        respVO.setScheduleDate(schedule.getScheduleDate());
        respVO.setStationId(schedule.getStationId());
        respVO.setShiftId(schedule.getShiftId());
        respVO.setShiftName(schedule.getShiftName());
        respVO.setTeamId(schedule.getTeamId());
        respVO.setTeamName(schedule.getTeamName());
        respVO.setDutyUserId(schedule.getDutyUserId());
        respVO.setDutyUserName(schedule.getDutyUserName());
        respVO.setDutyMobile(schedule.getDutyMobile());
        respVO.setDutyPostName(schedule.getDutyPostName());
        respVO.setDutyStartTime(schedule.getDutyStartTime());
        respVO.setDutyEndTime(schedule.getDutyEndTime());
        respVO.setDutyLog(schedule.getDutyLog());
        respVO.setStatus(schedule.getStatus());
        respVO.setRemark(schedule.getRemark());
        respVO.setCreator(resolveCreatorName(schedule.getCreator(), creatorMap));
        respVO.setCreateTime(schedule.getCreateTime());
        return respVO;
    }

    /**
     * 批量构建创建人映射
     */
    private Map<Long, AdminUserRespDTO> buildCreatorMap(List<IotShiftScheduleDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> creatorIds = new LinkedHashSet<>();
        for (IotShiftScheduleDO schedule : list) {
            Long creatorId = parseUserId(schedule == null ? null : schedule.getCreator());
            if (creatorId != null) {
                creatorIds.add(creatorId);
            }
        }
        if (creatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return adminUserApi.getUserMap(creatorIds);
    }

    /**
     * 解析创建人中文名称
     */
    private String resolveCreatorName(String creator, Map<Long, AdminUserRespDTO> creatorMap) {
        Long creatorId = parseUserId(creator);
        if (creatorId == null) {
            return trimToEmpty(creator);
        }
        AdminUserRespDTO user = creatorMap.get(creatorId);
        if (user == null) {
            return trimToEmpty(creator);
        }
        String nickname = trimToNull(user.getNickname());
        return nickname == null ? String.valueOf(creatorId) : nickname;
    }

    private Long parseUserId(String creator) {
        String value = trimToNull(creator);
        if (value == null || !StrUtil.isNumeric(value)) {
            return null;
        }
        return Long.valueOf(value);
    }

    private YearMonth resolveYearMonth(String month) {
        String normalized = trimToNull(month);
        if (normalized == null) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(normalized, YEAR_MONTH_FORMATTER);
        } catch (DateTimeParseException ignore) {
            return YearMonth.now();
        }
    }

    private IotShiftScheduleSaveReqVO convertImportRow(IotShiftScheduleImportExcelVO row) {
        if (row == null || row.getScheduleDate() == null || row.getShiftId() == null
                || row.getTeamId() == null || row.getDutyUserId() == null || StrUtil.isBlank(row.getDutyLog())) {
            throw new IllegalArgumentException("值班日期、班次ID、班组ID、值班人员ID、值班日志不能为空");
        }
        IotShiftScheduleSaveReqVO reqVO = new IotShiftScheduleSaveReqVO();
        reqVO.setScheduleDate(row.getScheduleDate());
        reqVO.setStationId("");
        reqVO.setShiftId(row.getShiftId());
        reqVO.setTeamId(row.getTeamId());
        reqVO.setDutyUserId(row.getDutyUserId());
        reqVO.setDutyLog(row.getDutyLog());
        reqVO.setRemark(row.getRemark());
        return reqVO;
    }

    private String resolveDutyUserMobile(IotShiftTeamMemberDO leaderMember, AdminUserRespDTO user) {
        String mobile = trimToNull(leaderMember == null ? null : leaderMember.getMobile());
        if (mobile == null) {
            mobile = trimToNull(user == null ? null : user.getMobile());
        }
        if (!ValidationUtils.isMobile(mobile)) {
            throw exception(SHIFT_SCHEDULE_MOBILE_INVALID);
        }
        return mobile;
    }

    private String resolveDutyUserPostName(AdminUserRespDTO user) {
        Set<Long> postIds = user.getPostIds();
        if (postIds == null || postIds.isEmpty()) {
            return "";
        }
        Map<Long, PostRespDTO> postMap = postApi.getPostMap(postIds);
        LinkedHashSet<String> postNames = new LinkedHashSet<>();
        for (Long postId : postIds) {
            PostRespDTO post = postMap.get(postId);
            String postName = post == null ? null : trimToNull(post.getName());
            if (postName != null) {
                postNames.add(postName);
            }
        }
        if (postNames.isEmpty()) {
            return "";
        }
        return String.join("/", postNames);
    }

    private String trimToNull(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private String trimToEmpty(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? "" : normalized;
    }

    /**
     * 排班上下文
     */
    private static class ShiftScheduleContext {
        private LocalDate scheduleDate;
        private String stationId;
        private Long shiftId;
        private String shiftName;
        private Long teamId;
        private String teamName;
        private Long dutyUserId;
        private String dutyUserName;
        private String dutyMobile;
        private String dutyPostName;
        private LocalDateTime dutyStartTime;
        private LocalDateTime dutyEndTime;
        private String dutyLog;
        private String remark;
    }
}
