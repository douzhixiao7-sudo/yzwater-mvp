package com.sydigit.yzwater.module.iot.service.shifthandover;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.framework.security.core.service.SecurityFrameworkService;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverDefaultRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverReminderRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.shifthandover.vo.IotShiftHandoverSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchmanage.IotDispatchInstructionDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shifthandover.IotShiftHandoverDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftschedule.IotShiftScheduleDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam.IotShiftTeamDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam.IotShiftTeamMemberDO;
import com.sydigit.yzwater.module.iot.dal.mysql.dispatchmanage.IotDispatchInstructionMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shifthandover.IotShiftHandoverMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftschedule.IotShiftScheduleMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftteam.IotShiftTeamMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftteam.IotShiftTeamMemberMapper;
import com.sydigit.yzwater.module.system.api.user.AdminUserApi;
import com.sydigit.yzwater.module.system.api.user.dto.AdminUserRespDTO;
import com.sydigit.yzwater.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.RUN_LOG_DISPATCH_INSTRUCTION_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_CURRENT_DUTY_USER_NOT_SET;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_CURRENT_SCHEDULE_NOT_FOUND;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_CURRENT_SCHEDULE_TIME_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_NEXT_DUTY_USER_NOT_SET;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_NEXT_SCHEDULE_NOT_FOUND;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_NEXT_SCHEDULE_TIME_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_SCHEDULE_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_SCHEDULE_MISMATCH;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_SCHEDULE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_SUBMIT_TOO_EARLY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_SUBMIT_WINDOW_EXPIRED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_TAKEOVER_USER_NOT_FOUND;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SHIFT_HANDOVER_USER_NOT_EXISTS;

/**
 * 交接班 Service 实现
 */
@Service
@Validated
public class IotShiftHandoverServiceImpl implements IotShiftHandoverService {

    private static final String HANDOVER_NO_PREFIX = "JB-";
    private static final DateTimeFormatter DATE_COMPACT_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 交接提醒提前分钟数
     */
    private static final long HANDOVER_REMIND_ADVANCE_MINUTES = 15;

    @Resource
    private IotShiftHandoverMapper shiftHandoverMapper;
    @Resource
    private IotShiftScheduleMapper shiftScheduleMapper;
    @Resource
    private IotShiftTeamMapper shiftTeamMapper;
    @Resource
    private IotShiftTeamMemberMapper shiftTeamMemberMapper;
    @Resource
    private IotDispatchInstructionMapper dispatchInstructionMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private SecurityFrameworkService securityFrameworkService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createShiftHandover(IotShiftHandoverSaveReqVO createReqVO) {
        ShiftHandoverContext context = buildContext(createReqVO, null);

        IotShiftHandoverDO handover = new IotShiftHandoverDO();
        handover.setHandoverNo(generateHandoverNo());
        handover.setScheduleId(context.scheduleId);
        handover.setHandoverTime(context.handoverTime);
        handover.setShiftId(context.shiftId);
        handover.setShiftName(context.shiftName);
        handover.setTeamId(context.teamId);
        handover.setTeamName(context.teamName);
        handover.setHandoverUserId(context.handoverUserId);
        handover.setHandoverUserName(context.handoverUserName);
        handover.setTakeoverUserId(context.takeoverUserId);
        handover.setTakeoverUserName(context.takeoverUserName);
        handover.setDutyLog(context.dutyLog);
        handover.setPendingItems(context.pendingItems);
        handover.setDispatchInstructionId(context.dispatchInstructionId);
        handover.setDispatchInstructionNo(context.dispatchInstructionNo);
        handover.setDispatchInstructionName(context.dispatchInstructionName);
        // 缺陷/两票字段已停用，创建时统一置空
        handover.setDefectTicketFlag("");
        handover.setStatus(1);
        handover.setRemark(context.remark);
        shiftHandoverMapper.insert(handover);
        return handover.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShiftHandover(IotShiftHandoverSaveReqVO updateReqVO) {
        IotShiftHandoverDO existed = validateShiftHandoverExists(updateReqVO.getId());
        ShiftHandoverContext context = buildContext(updateReqVO, existed.getId());

        IotShiftHandoverDO updateObj = new IotShiftHandoverDO();
        updateObj.setId(existed.getId());
        updateObj.setScheduleId(context.scheduleId);
        updateObj.setHandoverTime(context.handoverTime);
        updateObj.setShiftId(context.shiftId);
        updateObj.setShiftName(context.shiftName);
        updateObj.setTeamId(context.teamId);
        updateObj.setTeamName(context.teamName);
        updateObj.setHandoverUserId(context.handoverUserId);
        updateObj.setHandoverUserName(context.handoverUserName);
        updateObj.setTakeoverUserId(context.takeoverUserId);
        updateObj.setTakeoverUserName(context.takeoverUserName);
        updateObj.setDutyLog(context.dutyLog);
        updateObj.setPendingItems(context.pendingItems);
        updateObj.setDispatchInstructionId(context.dispatchInstructionId);
        updateObj.setDispatchInstructionNo(context.dispatchInstructionNo);
        updateObj.setDispatchInstructionName(context.dispatchInstructionName);
        // 缺陷/两票字段已停用，更新时保留历史值
        updateObj.setDefectTicketFlag(existed.getDefectTicketFlag());
        updateObj.setStatus(1);
        updateObj.setRemark(context.remark);
        shiftHandoverMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShiftHandover(Long id) {
        validateShiftHandoverExists(id);
        shiftHandoverMapper.deleteById(id);
    }

    @Override
    public IotShiftHandoverRespVO getShiftHandover(Long id) {
        IotShiftHandoverDO handover = validateShiftHandoverExists(id);
        return buildResp(handover, buildCreatorMap(Collections.singletonList(handover)));
    }

    @Override
    public PageResult<IotShiftHandoverRespVO> getShiftHandoverPage(IotShiftHandoverPageReqVO pageReqVO) {
        PageResult<IotShiftHandoverDO> pageResult = shiftHandoverMapper.selectPage(pageReqVO);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(pageResult.getList());
        List<IotShiftHandoverRespVO> respList = CollectionUtils.convertList(pageResult.getList(),
                item -> buildResp(item, creatorMap));
        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public List<IotShiftHandoverRespVO> getShiftHandoverList(IotShiftHandoverPageReqVO reqVO) {
        List<IotShiftHandoverDO> list = shiftHandoverMapper.selectListByReqVO(reqVO);
        Map<Long, AdminUserRespDTO> creatorMap = buildCreatorMap(list);
        return CollectionUtils.convertList(list, item -> buildResp(item, creatorMap));
    }

    @Override
    public IotShiftHandoverDefaultRespVO getShiftHandoverDefault() {
        HandoverCandidate candidate = resolveHandoverCandidateForCreateOrThrow(LocalDateTime.now());
        return buildDefaultResp(candidate);
    }

    @Override
    public IotShiftHandoverReminderRespVO getShiftHandoverReminder() {
        IotShiftHandoverReminderRespVO respVO = new IotShiftHandoverReminderRespVO();
        respVO.setNeedRemind(false);
        respVO.setMessage("");

        LocalDateTime now = LocalDateTime.now();
        HandoverCandidate candidate = resolveHandoverCandidate(now);
        if (candidate == null) {
            return respVO;
        }
        LocalDateTime nowMinute = normalizeToMinute(now);
        if (isOutOfSubmitWindow(nowMinute, candidate.remindStartTime, candidate.remindEndTime)) {
            return respVO;
        }
        IotShiftHandoverDO existed = shiftHandoverMapper.selectByScheduleId(candidate.currentSchedule.getId());
        if (existed != null) {
            return respVO;
        }

        IotShiftHandoverDefaultRespVO defaultRespVO = buildDefaultResp(candidate);
        respVO.setNeedRemind(true);
        respVO.setHandoverDefault(defaultRespVO);
        respVO.setMessage("当前已进入交接窗口，请及时完成交接班并提交值班日志。");
        return respVO;
    }

    private ShiftHandoverContext buildContext(IotShiftHandoverSaveReqVO reqVO, Long excludeId) {
        boolean createMode = excludeId == null;
        IotShiftScheduleDO schedule = shiftScheduleMapper.selectById(reqVO.getScheduleId());
        if (schedule == null) {
            throw exception(SHIFT_HANDOVER_SCHEDULE_NOT_EXISTS);
        }
        validateScheduleUnique(reqVO.getScheduleId(), excludeId);
        if (createMode) {
            validateCreateRequestMatchesCandidate(schedule.getId());
        }

        ShiftHandoverContext context = new ShiftHandoverContext();
        context.scheduleId = schedule.getId();
        context.shiftId = schedule.getShiftId();
        context.shiftName = trimToEmpty(schedule.getShiftName());
        context.teamId = schedule.getTeamId();
        context.teamName = trimToEmpty(schedule.getTeamName());
        context.handoverTime = createMode
                ? resolveAutoHandoverTime(schedule)
                : (reqVO.getHandoverTime() == null ? LocalDateTime.now() : reqVO.getHandoverTime());
        context.dutyLog = trimToEmpty(reqVO.getDutyLog());
        context.pendingItems = trimToEmpty(reqVO.getPendingItems());
        context.remark = trimToEmpty(reqVO.getRemark());

        DispatchInstructionSnapshot instructionSnapshot = resolveDispatchInstructionSnapshot(reqVO.getDispatchInstructionId());
        context.dispatchInstructionId = instructionSnapshot.id;
        context.dispatchInstructionNo = instructionSnapshot.instructionNo;
        context.dispatchInstructionName = instructionSnapshot.instructionName;

        Long handoverUserId = createMode
                ? schedule.getDutyUserId()
                : (reqVO.getHandoverUserId() == null ? schedule.getDutyUserId() : reqVO.getHandoverUserId());
        AdminUserRespDTO handoverUser = resolveUser(handoverUserId);
        context.handoverUserId = handoverUser.getId();
        context.handoverUserName = resolveUserName(handoverUser);

        Long takeoverUserId = createMode
                ? resolveTakeoverUserId(schedule)
                : (reqVO.getTakeoverUserId() == null ? resolveTakeoverUserId(schedule) : reqVO.getTakeoverUserId());
        AdminUserRespDTO takeoverUser = resolveUser(takeoverUserId);
        context.takeoverUserId = takeoverUser.getId();
        context.takeoverUserName = resolveUserName(takeoverUser);
        return context;
    }

    /**
     * 创建交接班时，必须命中当前用户自动匹配到的可交接班次
     */
    private void validateCreateRequestMatchesCandidate(Long scheduleId) {
        HandoverCandidate candidate = resolveHandoverCandidateForCreateOrThrow(LocalDateTime.now());
        if (!candidate.currentSchedule.getId().equals(scheduleId)) {
            throw exception(SHIFT_HANDOVER_SCHEDULE_MISMATCH);
        }
    }

    /**
     * 创建交接班时的自动匹配前置校验，逐条报出明确原因
     */
    private HandoverCandidate resolveHandoverCandidateForCreateOrThrow(LocalDateTime now) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        boolean adminRole = hasAdminRole();
        if (!adminRole && loginUserId == null) {
            throw exception(SHIFT_HANDOVER_CURRENT_SCHEDULE_NOT_FOUND);
        }

        Set<Long> memberTeamIds = resolveLoginUserTeamIds(loginUserId);
        LambdaQueryWrapperX<IotShiftScheduleDO> currentQueryWrapper = new LambdaQueryWrapperX<>();
        currentQueryWrapper.le(IotShiftScheduleDO::getDutyStartTime, now);
        currentQueryWrapper.ge(IotShiftScheduleDO::getDutyEndTime, now);
        if (!adminRole) {
            if (memberTeamIds.isEmpty()) {
                throw exception(SHIFT_HANDOVER_CURRENT_SCHEDULE_NOT_FOUND);
            }
            currentQueryWrapper.in(IotShiftScheduleDO::getTeamId, memberTeamIds);
        }
        IotShiftScheduleDO currentSchedule = shiftScheduleMapper.selectOne(currentQueryWrapper
                .orderByDesc(IotShiftScheduleDO::getDutyStartTime)
                .orderByDesc(IotShiftScheduleDO::getId)
                .last("LIMIT 1"));
        if (currentSchedule == null) {
            throw exception(SHIFT_HANDOVER_CURRENT_SCHEDULE_NOT_FOUND);
        }
        if (currentSchedule.getDutyStartTime() == null || currentSchedule.getDutyEndTime() == null) {
            throw exception(SHIFT_HANDOVER_CURRENT_SCHEDULE_TIME_INVALID);
        }

        IotShiftScheduleDO nextSchedule = findNextSchedule(currentSchedule);
        if (nextSchedule == null) {
            throw exception(SHIFT_HANDOVER_NEXT_SCHEDULE_NOT_FOUND);
        }
        if (nextSchedule.getDutyStartTime() == null) {
            throw exception(SHIFT_HANDOVER_NEXT_SCHEDULE_TIME_INVALID);
        }

        if (currentSchedule.getDutyUserId() == null) {
            throw exception(SHIFT_HANDOVER_CURRENT_DUTY_USER_NOT_SET);
        }
        if (nextSchedule.getDutyUserId() == null) {
            throw exception(SHIFT_HANDOVER_NEXT_DUTY_USER_NOT_SET);
        }

        LocalDateTime nowMinute = normalizeToMinute(now);
        LocalDateTime currentEndTime = normalizeToMinute(currentSchedule.getDutyEndTime());
        LocalDateTime nextStartTime = nextSchedule.getDutyStartTime();
        LocalDateTime remindStartTime = normalizeToMinute(nextStartTime.minusMinutes(HANDOVER_REMIND_ADVANCE_MINUTES));
        if (nowMinute.isBefore(remindStartTime)) {
            throw exception(SHIFT_HANDOVER_SUBMIT_TOO_EARLY);
        }
        if (nowMinute.isAfter(currentEndTime)) {
            throw exception(SHIFT_HANDOVER_SUBMIT_WINDOW_EXPIRED);
        }

        HandoverCandidate candidate = new HandoverCandidate();
        candidate.currentSchedule = currentSchedule;
        candidate.nextSchedule = nextSchedule;
        candidate.nextScheduleStartTime = nextStartTime;
        candidate.remindStartTime = remindStartTime;
        candidate.remindEndTime = currentEndTime;
        return candidate;
    }

    private void validateScheduleUnique(Long scheduleId, Long excludeId) {
        IotShiftHandoverDO existed = shiftHandoverMapper.selectByScheduleId(scheduleId);
        if (existed == null) {
            return;
        }
        if (excludeId != null && existed.getId().equals(excludeId)) {
            return;
        }
        throw exception(SHIFT_HANDOVER_SCHEDULE_EXISTS);
    }

    private Long resolveTakeoverUserId(IotShiftScheduleDO schedule) {
        IotShiftScheduleDO nextSchedule = findNextSchedule(schedule);
        if (nextSchedule == null) {
            throw exception(SHIFT_HANDOVER_TAKEOVER_USER_NOT_FOUND);
        }
        IotShiftTeamDO nextTeam = nextSchedule.getTeamId() == null ? null : shiftTeamMapper.selectById(nextSchedule.getTeamId());
        Long leaderUserId = nextTeam == null ? null : nextTeam.getLeaderUserId();
        if (leaderUserId != null) {
            return leaderUserId;
        }
        if (nextSchedule.getDutyUserId() != null) {
            return nextSchedule.getDutyUserId();
        }
        throw exception(SHIFT_HANDOVER_TAKEOVER_USER_NOT_FOUND);
    }

    /**
     * 解析调度指令快照（可空）
     */
    private DispatchInstructionSnapshot resolveDispatchInstructionSnapshot(Long dispatchInstructionId) {
        DispatchInstructionSnapshot snapshot = new DispatchInstructionSnapshot();
        if (dispatchInstructionId == null) {
            return snapshot;
        }
        IotDispatchInstructionDO instruction = dispatchInstructionMapper.selectById(dispatchInstructionId);
        if (instruction == null) {
            throw exception(RUN_LOG_DISPATCH_INSTRUCTION_NOT_EXISTS);
        }
        snapshot.id = instruction.getId();
        snapshot.instructionNo = trimToEmpty(instruction.getInstructionNo());
        snapshot.instructionName = trimToEmpty(instruction.getInstructionName());
        return snapshot;
    }

    private IotShiftHandoverDO validateShiftHandoverExists(Long id) {
        IotShiftHandoverDO handover = shiftHandoverMapper.selectById(id);
        if (handover == null) {
            throw exception(SHIFT_HANDOVER_NOT_EXISTS);
        }
        return handover;
    }

    private AdminUserRespDTO resolveUser(Long userId) {
        if (userId == null) {
            throw exception(SHIFT_HANDOVER_USER_NOT_EXISTS);
        }
        adminUserApi.validateUser(userId);
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        if (user == null) {
            throw exception(SHIFT_HANDOVER_USER_NOT_EXISTS);
        }
        return user;
    }

    private String resolveUserName(AdminUserRespDTO user) {
        String nickname = trimToNull(user.getNickname());
        return nickname == null ? String.valueOf(user.getId()) : nickname;
    }

    private HandoverCandidate resolveHandoverCandidate(LocalDateTime now) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        boolean adminRole = hasAdminRole();
        // 先锁定“当前正在值班”的排班，避免按全局下一班次误匹配到其它班组
        LambdaQueryWrapperX<IotShiftScheduleDO> currentQueryWrapper = new LambdaQueryWrapperX<>();
        currentQueryWrapper.le(IotShiftScheduleDO::getDutyStartTime, now);
        currentQueryWrapper.ge(IotShiftScheduleDO::getDutyEndTime, now);
        if (!adminRole) {
            Set<Long> memberTeamIds = resolveLoginUserTeamIds(loginUserId);
            if (memberTeamIds.isEmpty()) {
                return null;
            }
            currentQueryWrapper.in(IotShiftScheduleDO::getTeamId, memberTeamIds);
        }
        IotShiftScheduleDO currentSchedule = shiftScheduleMapper.selectOne(currentQueryWrapper
                .orderByDesc(IotShiftScheduleDO::getDutyStartTime)
                .orderByDesc(IotShiftScheduleDO::getId)
                .last("LIMIT 1"));
        if (currentSchedule == null || currentSchedule.getDutyStartTime() == null
                || currentSchedule.getDutyEndTime() == null) {
            return null;
        }

        // 再找下一条排班（优先同站点，按开始时间紧邻），允许不同班组/班次交接
        IotShiftScheduleDO nextSchedule = findNextSchedule(currentSchedule);
        if (nextSchedule == null || nextSchedule.getDutyStartTime() == null) {
            return null;
        }

        if (currentSchedule.getDutyUserId() == null || nextSchedule.getDutyUserId() == null) {
            return null;
        }

        LocalDateTime nowMinute = normalizeToMinute(now);
        LocalDateTime currentEndTime = normalizeToMinute(currentSchedule.getDutyEndTime());
        LocalDateTime nextStartTime = nextSchedule.getDutyStartTime();
        LocalDateTime remindStartTime = normalizeToMinute(nextStartTime.minusMinutes(HANDOVER_REMIND_ADVANCE_MINUTES));
        // 仅在交接窗口内可提交（不支持补交）
        if (isOutOfSubmitWindow(nowMinute, remindStartTime, currentEndTime)) {
            return null;
        }

        HandoverCandidate candidate = new HandoverCandidate();
        candidate.currentSchedule = currentSchedule;
        candidate.nextSchedule = nextSchedule;
        candidate.nextScheduleStartTime = nextStartTime;
        candidate.remindStartTime = remindStartTime;
        candidate.remindEndTime = currentEndTime;
        return candidate;
    }

    private IotShiftHandoverDefaultRespVO buildDefaultResp(HandoverCandidate candidate) {
        IotShiftHandoverDefaultRespVO respVO = new IotShiftHandoverDefaultRespVO();
        IotShiftScheduleDO currentSchedule = candidate.currentSchedule;
        IotShiftScheduleDO nextSchedule = candidate.nextSchedule;
        respVO.setScheduleId(currentSchedule.getId());
        respVO.setShiftId(currentSchedule.getShiftId());
        respVO.setShiftName(trimToEmpty(currentSchedule.getShiftName()));
        respVO.setTeamId(currentSchedule.getTeamId());
        respVO.setTeamName(trimToEmpty(currentSchedule.getTeamName()));
        respVO.setHandoverUserId(currentSchedule.getDutyUserId());
        AdminUserRespDTO handoverUser = resolveUser(currentSchedule.getDutyUserId());
        respVO.setHandoverUserName(resolveUserName(handoverUser));
        Long takeoverUserId = resolveTakeoverUserId(currentSchedule);
        respVO.setTakeoverUserId(takeoverUserId);
        respVO.setTakeoverUserName(resolveUserName(resolveUser(takeoverUserId)));
        respVO.setHandoverTime(formatDateTime(resolveAutoHandoverTime(currentSchedule)));
        respVO.setNextShiftStartTime(formatDateTime(candidate.nextScheduleStartTime));
        return respVO;
    }

    private Set<Long> resolveLoginUserTeamIds(Long loginUserId) {
        if (loginUserId == null) {
            return Collections.emptySet();
        }
        List<IotShiftTeamMemberDO> members = shiftTeamMemberMapper.selectList(new LambdaQueryWrapperX<IotShiftTeamMemberDO>()
                .eq(IotShiftTeamMemberDO::getUserId, loginUserId)
                .eq(IotShiftTeamMemberDO::getStatus, 0));
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return CollectionUtils.convertSet(members, IotShiftTeamMemberDO::getTeamId,
                item -> item != null && item.getTeamId() != null);
    }

    private IotShiftScheduleDO findNextSchedule(IotShiftScheduleDO currentSchedule) {
        if (currentSchedule == null || currentSchedule.getDutyStartTime() == null) {
            return null;
        }
        LambdaQueryWrapperX<IotShiftScheduleDO> nextQueryWrapper = new LambdaQueryWrapperX<>();
        nextQueryWrapper.gt(IotShiftScheduleDO::getDutyStartTime, currentSchedule.getDutyStartTime());
        String currentStationId = trimToNull(currentSchedule.getStationId());
        if (currentStationId != null) {
            nextQueryWrapper.eq(IotShiftScheduleDO::getStationId, currentStationId);
        }
        return shiftScheduleMapper.selectOne(nextQueryWrapper
                .orderByAsc(IotShiftScheduleDO::getDutyStartTime)
                .orderByAsc(IotShiftScheduleDO::getId)
                .last("LIMIT 1"));
    }

    private boolean hasAdminRole() {
        return securityFrameworkService.hasAnyRoles(
                RoleCodeEnum.SUPER_ADMIN.getCode(), RoleCodeEnum.TENANT_ADMIN.getCode());
    }

    private LocalDateTime normalizeToMinute(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.withSecond(0).withNano(0);
    }

    private boolean isOutOfSubmitWindow(LocalDateTime now, LocalDateTime remindStartTime, LocalDateTime remindEndTime) {
        return now == null || remindStartTime == null || remindEndTime == null
                || now.isBefore(remindStartTime) || now.isAfter(remindEndTime);
    }

    /**
     * 交接班时间优先取当前待交接排班的结束时间，确保与排班值班日期一致
     */
    private LocalDateTime resolveAutoHandoverTime(IotShiftScheduleDO schedule) {
        if (schedule == null) {
            return LocalDateTime.now();
        }
        if (schedule.getDutyEndTime() != null) {
            return schedule.getDutyEndTime();
        }
        if (schedule.getDutyStartTime() != null) {
            return schedule.getDutyStartTime();
        }
        return LocalDateTime.now();
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(DATE_TIME_FORMATTER);
    }

    private IotShiftHandoverRespVO buildResp(IotShiftHandoverDO handover, Map<Long, AdminUserRespDTO> creatorMap) {
        IotShiftHandoverRespVO respVO = new IotShiftHandoverRespVO();
        respVO.setId(handover.getId());
        respVO.setHandoverNo(handover.getHandoverNo());
        respVO.setScheduleId(handover.getScheduleId());
        respVO.setHandoverTime(handover.getHandoverTime());
        respVO.setShiftId(handover.getShiftId());
        respVO.setShiftName(handover.getShiftName());
        respVO.setTeamId(handover.getTeamId());
        respVO.setTeamName(handover.getTeamName());
        respVO.setHandoverUserId(handover.getHandoverUserId());
        respVO.setHandoverUserName(handover.getHandoverUserName());
        respVO.setTakeoverUserId(handover.getTakeoverUserId());
        respVO.setTakeoverUserName(handover.getTakeoverUserName());
        respVO.setDutyLog(handover.getDutyLog());
        respVO.setPendingItems(handover.getPendingItems());
        respVO.setDispatchInstructionId(handover.getDispatchInstructionId());
        respVO.setDispatchInstructionNo(handover.getDispatchInstructionNo());
        respVO.setDispatchInstructionName(handover.getDispatchInstructionName());
        respVO.setDefectTicketFlag(handover.getDefectTicketFlag());
        respVO.setStatus(handover.getStatus());
        respVO.setRemark(handover.getRemark());
        respVO.setCreator(resolveCreatorName(handover.getCreator(), creatorMap));
        respVO.setCreateTime(handover.getCreateTime());
        return respVO;
    }

    private Map<Long, AdminUserRespDTO> buildCreatorMap(List<IotShiftHandoverDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> creatorIds = new LinkedHashSet<>();
        for (IotShiftHandoverDO handover : list) {
            Long creatorId = parseUserId(handover == null ? null : handover.getCreator());
            if (creatorId != null) {
                creatorIds.add(creatorId);
            }
        }
        if (creatorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return adminUserApi.getUserMap(creatorIds);
    }

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

    private String generateHandoverNo() {
        String prefix = HANDOVER_NO_PREFIX + LocalDate.now().format(DATE_COMPACT_FORMATTER) + "-";
        String latest = shiftHandoverMapper.selectLatestHandoverNoByPrefix(prefix);
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

    private Long parseUserId(String creator) {
        String value = trimToNull(creator);
        if (value == null || !StrUtil.isNumeric(value)) {
            return null;
        }
        return Long.valueOf(value);
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
     * 交接班构建上下文
     */
    private static class ShiftHandoverContext {
        private Long scheduleId;
        private LocalDateTime handoverTime;
        private Long shiftId;
        private String shiftName;
        private Long teamId;
        private String teamName;
        private Long handoverUserId;
        private String handoverUserName;
        private Long takeoverUserId;
        private String takeoverUserName;
        private String dutyLog;
        private String pendingItems;
        private Long dispatchInstructionId;
        private String dispatchInstructionNo;
        private String dispatchInstructionName;
        private String remark;
    }

    /**
     * 调度指令快照
     */
    private static class DispatchInstructionSnapshot {
        private Long id;
        private String instructionNo = "";
        private String instructionName = "";
    }

    /**
     * 自动匹配交接班候选
     */
    private static class HandoverCandidate {
        private IotShiftScheduleDO currentSchedule;
        private IotShiftScheduleDO nextSchedule;
        private LocalDateTime nextScheduleStartTime;
        private LocalDateTime remindStartTime;
        private LocalDateTime remindEndTime;
    }
}

