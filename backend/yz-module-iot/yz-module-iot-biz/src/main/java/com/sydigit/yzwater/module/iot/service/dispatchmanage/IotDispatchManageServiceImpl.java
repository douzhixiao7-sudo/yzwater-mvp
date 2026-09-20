package com.sydigit.yzwater.module.iot.service.dispatchmanage;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.enums.CommonStatusEnum;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.framework.security.core.service.SecurityFrameworkService;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.framework.tenant.core.context.TenantContextHolder;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManagePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManagePlanOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageReceiverUserRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchmanage.vo.IotDispatchManageSubmitResultReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceivePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveRunLogOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.dispatchreceive.vo.IotDispatchReceiveSubmitReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchmanage.IotDispatchInstructionDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchmanage.IotDispatchInstructionLogDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchplan.IotDispatchPlanDO;
import com.sydigit.yzwater.module.iot.dal.mysql.dispatchmanage.IotDispatchInstructionLogMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.dispatchmanage.IotDispatchInstructionMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.dispatchplan.IotDispatchPlanMapper;
import com.sydigit.yzwater.module.iot.enums.dispatchmanage.IotDispatchInstructionStatusEnum;
import com.sydigit.yzwater.module.system.api.dept.DeptApi;
import com.sydigit.yzwater.module.system.api.dept.dto.DeptRespDTO;
import com.sydigit.yzwater.module.system.api.permission.PermissionApi;
import com.sydigit.yzwater.module.system.api.user.AdminUserApi;
import com.sydigit.yzwater.module.system.api.user.dto.AdminUserRespDTO;
import com.sydigit.yzwater.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_EXECUTED_LOCKED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_PLAN_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_PLAN_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_QUERY_FORBIDDEN;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_RECEIVER_DEPT_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_RECEIVER_USER_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_RECEIVER_USER_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_EXECUTOR_USER_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_EXECUTOR_USER_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_STATUS_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_SUBMIT_FORBIDDEN;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_SUBMIT_STATUS_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_MANAGE_UPDATE_DELETE_FORBIDDEN;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_RECEIVE_ACCEPT_FORBIDDEN;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_RECEIVE_ATTACHMENT_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_RECEIVE_EXECUTE_FLAG_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_RECEIVE_FORBIDDEN;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_RECEIVE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_RECEIVE_STATUS_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DISPATCH_RECEIVE_SUBMIT_FORBIDDEN;

/**
 * 调度管理 Service 实现
 */
@Service
@Validated
public class IotDispatchManageServiceImpl implements IotDispatchManageService {

    private static final DateTimeFormatter INSTRUCTION_NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String INSTRUCTION_NO_PREFIX = "DD";

    private static final int ACTION_ISSUE = 1;
    private static final int ACTION_RECEIVE = 2;
    private static final int ACTION_FEEDBACK = 3;
    private static final int ACTION_UPDATE = 5;
    private static final String GUEST_ROLE_CODE = "GUEST";

    @Resource
    private IotDispatchInstructionMapper instructionMapper;
    @Resource
    private IotDispatchInstructionLogMapper instructionLogMapper;
    @Resource
    private IotDispatchPlanMapper dispatchPlanMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private PermissionApi permissionApi;
    @Resource
    private SecurityFrameworkService securityFrameworkService;
    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInstruction(IotDispatchManageSaveReqVO createReqVO) {
        List<IotDispatchPlanDO> plans = validateAndGetPlans(createReqVO.getPlanIds());
        ReceiverSnapshot receiverSnapshot = validateAndGetReceiver(
                createReqVO.getReceiverDeptId(), createReqVO.getReceiverUserId(), createReqVO.getExecutorUserId());

        IotDispatchInstructionDO instruction = new IotDispatchInstructionDO();
        instruction.setInstructionNo(generateInstructionNo());
        instruction.setInstructionName(trimToEmpty(createReqVO.getInstructionName()));
        instruction.setIssueOrgName(trimToEmpty(createReqVO.getIssueOrgName()));
        instruction.setIssueUserId(SecurityFrameworkUtils.getLoginUserId());
        instruction.setIssueUserName(resolveIssueUserName(createReqVO.getIssueUserName()));
        instruction.setStationId(trimToEmpty(createReqVO.getStationId()));
        instruction.setInstructionContent(trimToEmpty(createReqVO.getInstructionContent()));
        instruction.setPlanIds(joinPlanIds(plans));
        instruction.setPlanSnapshotJson(JsonUtils.toJsonString(buildPlanSnapshots(plans)));
        instruction.setPlannedFinishTime(createReqVO.getPlannedFinishTime());
        instruction.setReceiverDeptId(receiverSnapshot.receiverDeptId);
        instruction.setReceiverDeptName(receiverSnapshot.receiverDeptName);
        instruction.setReceiverUserId(receiverSnapshot.receiverUserId);
        instruction.setReceiverUserName(receiverSnapshot.receiverUserName);
        instruction.setExecutorUserId(receiverSnapshot.executorUserId);
        instruction.setExecutorUserName(receiverSnapshot.executorUserName);
        instruction.setReceiveStatus(0);
        instruction.setReceiveTime(null);
        instruction.setStatus(resolveInitialStatus(createReqVO.getPlannedFinishTime()));
        instruction.setOperationTicketUrl(trimToEmpty(createReqVO.getOperationTicketUrl()));
        instruction.setRemark(trimToEmpty(createReqVO.getRemark()));
        instructionMapper.insert(instruction);

        insertInstructionLog(instruction.getId(), ACTION_ISSUE, "下达调度指令");
        return instruction.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInstruction(IotDispatchManageSaveReqVO updateReqVO) {
        IotDispatchInstructionDO existed = validateInstructionExists(updateReqVO.getId());
        validateUpdateDeletePermission(existed);
        List<IotDispatchPlanDO> plans = validateAndGetPlans(updateReqVO.getPlanIds());
        ReceiverSnapshot receiverSnapshot = validateAndGetReceiver(
                updateReqVO.getReceiverDeptId(), updateReqVO.getReceiverUserId(), updateReqVO.getExecutorUserId());

        IotDispatchInstructionDO updateObj = new IotDispatchInstructionDO();
        updateObj.setId(existed.getId());
        updateObj.setInstructionName(trimToEmpty(updateReqVO.getInstructionName()));
        updateObj.setIssueOrgName(trimToEmpty(updateReqVO.getIssueOrgName()));
        updateObj.setIssueUserName(resolveIssueUserName(updateReqVO.getIssueUserName()));
        updateObj.setStationId(trimToEmpty(updateReqVO.getStationId()));
        updateObj.setInstructionContent(trimToEmpty(updateReqVO.getInstructionContent()));
        updateObj.setPlanIds(joinPlanIds(plans));
        updateObj.setPlanSnapshotJson(JsonUtils.toJsonString(buildPlanSnapshots(plans)));
        updateObj.setPlannedFinishTime(updateReqVO.getPlannedFinishTime());
        updateObj.setReceiverDeptId(receiverSnapshot.receiverDeptId);
        updateObj.setReceiverDeptName(receiverSnapshot.receiverDeptName);
        updateObj.setReceiverUserId(receiverSnapshot.receiverUserId);
        updateObj.setReceiverUserName(receiverSnapshot.receiverUserName);
        updateObj.setExecutorUserId(receiverSnapshot.executorUserId);
        updateObj.setExecutorUserName(receiverSnapshot.executorUserName);
        updateObj.setReceiveStatus(0);
        updateObj.setReceiveTime(null);
        if (!Objects.equals(existed.getStatus(), IotDispatchInstructionStatusEnum.EXECUTED.getStatus())) {
            updateObj.setStatus(resolveInitialStatus(updateReqVO.getPlannedFinishTime()));
        }
        updateObj.setOperationTicketUrl(trimToEmpty(updateReqVO.getOperationTicketUrl()));
        updateObj.setRemark(trimToEmpty(updateReqVO.getRemark()));
        instructionMapper.updateById(updateObj);

        insertInstructionLog(existed.getId(), ACTION_UPDATE, "手工修改调度指令");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInstruction(Long id) {
        IotDispatchInstructionDO existed = validateInstructionExists(id);
        validateUpdateDeletePermission(existed);
        instructionMapper.deleteById(id);
    }

    @Override
    public IotDispatchManageRespVO getInstruction(Long id) {
        refreshOverdueStatus();
        IotDispatchInstructionDO instruction = validateInstructionExists(id);
        validateQueryPermission(instruction);
        return buildResp(instruction, queryRunLogCountMap(Collections.singleton(instruction.getId())));
    }

    @Override
    public PageResult<IotDispatchManageRespVO> getInstructionPage(IotDispatchManagePageReqVO pageReqVO) {
        refreshOverdueStatus();
        pageReqVO.setReceiverUserId(resolveQueryReceiverUserId());
        PageResult<IotDispatchInstructionDO> pageResult = instructionMapper.selectPage(pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }
        Map<Long, Integer> runLogCountMap = queryRunLogCountMap(
                CollectionUtils.convertList(pageResult.getList(), IotDispatchInstructionDO::getId));
        List<IotDispatchManageRespVO> list = pageResult.getList().stream()
                .map(item -> buildResp(item, runLogCountMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public List<IotDispatchManageRespVO> getInstructionList(IotDispatchManagePageReqVO reqVO) {
        refreshOverdueStatus();
        reqVO.setReceiverUserId(resolveQueryReceiverUserId());
        List<IotDispatchInstructionDO> list = instructionMapper.selectListByReqVO(reqVO);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, Integer> runLogCountMap = queryRunLogCountMap(
                CollectionUtils.convertList(list, IotDispatchInstructionDO::getId));
        return list.stream().map(item -> buildResp(item, runLogCountMap)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitResult(IotDispatchManageSubmitResultReqVO reqVO) {
        refreshOverdueStatus();
        IotDispatchInstructionDO instruction = validateInstructionExists(reqVO.getId());
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!Objects.equals(loginUserId, resolveExecutorUserId(instruction))) {
            throw exception(DISPATCH_MANAGE_SUBMIT_FORBIDDEN);
        }
        if (!Objects.equals(instruction.getStatus(), IotDispatchInstructionStatusEnum.WAIT_EXECUTE.getStatus())
                && !Objects.equals(instruction.getStatus(), IotDispatchInstructionStatusEnum.OVERDUE.getStatus())) {
            throw exception(DISPATCH_MANAGE_SUBMIT_STATUS_INVALID);
        }

        IotDispatchInstructionDO updateObj = new IotDispatchInstructionDO();
        updateObj.setId(instruction.getId());
        updateObj.setReceiveStatus(1);
        if (instruction.getReceiveTime() == null) {
            updateObj.setReceiveTime(LocalDateTime.now());
        }
        updateObj.setStatus(IotDispatchInstructionStatusEnum.EXECUTED.getStatus());
        updateObj.setFeedbackContent(trimToEmpty(reqVO.getFeedbackContent()));
        updateObj.setFeedbackRemark(trimToEmpty(reqVO.getFeedbackRemark()));
        updateObj.setFeedbackSubmitUserId(loginUserId);
        updateObj.setFeedbackSubmitUserName(resolveLoginUserName());
        updateObj.setFeedbackSubmitTime(LocalDateTime.now());
        instructionMapper.updateById(updateObj);

        insertInstructionLog(instruction.getId(), ACTION_FEEDBACK, "提交执行反馈");
    }

    @Override
    public List<IotDispatchManagePlanOptionRespVO> getPlanOptions(Integer planStatus, String stationId) {
        List<IotDispatchPlanDO> plans = dispatchPlanMapper.selectList(new LambdaQueryWrapperX<IotDispatchPlanDO>()
                .eqIfPresent(IotDispatchPlanDO::getPlanStatus, planStatus)
                .eqIfPresent(IotDispatchPlanDO::getStationId, trimToNull(stationId)));
        if (plans == null || plans.isEmpty()) {
            return Collections.emptyList();
        }
        return plans.stream()
                .sorted((a, b) -> {
                    LocalDateTime at = a.getPrepareTime() == null ? LocalDateTime.MIN : a.getPrepareTime();
                    LocalDateTime bt = b.getPrepareTime() == null ? LocalDateTime.MIN : b.getPrepareTime();
                    return bt.compareTo(at);
                })
                .map(item -> {
                    IotDispatchManagePlanOptionRespVO vo = new IotDispatchManagePlanOptionRespVO();
                    vo.setId(item.getId());
                    vo.setPlanNo(item.getPlanNo());
                    vo.setPlanName(item.getPlanName());
                    vo.setPlanType(item.getPlanType());
                    return vo;
                }).collect(Collectors.toList());
    }

    @Override
    public List<IotDispatchManageReceiverUserRespVO> getReceiverUserList(Long deptId) {
        return getDeptUsersByDeptId(deptId, false);
    }

    @Override
    public List<IotDispatchManageReceiverUserRespVO> getExecutorUserList(Long deptId) {
        // 执行人不受接收单位约束：返回当前租户下全部启用用户，并排除游客角色。
        return getAllTenantUsers(true);
    }

    private List<IotDispatchManageReceiverUserRespVO> getDeptUsersByDeptId(Long deptId, boolean excludeGuestRole) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        List<AdminUserRespDTO> users = adminUserApi.getUserListByDeptIds(Collections.singleton(deptId));
        return convertToReceiverUserOptions(users, excludeGuestRole);
    }

    private List<IotDispatchManageReceiverUserRespVO> getAllTenantUsers(boolean excludeGuestRole) {
        Long tenantId = TenantContextHolder.getTenantId() == null ? 0L : TenantContextHolder.getTenantId();
        String sql = "SELECT id FROM system_users WHERE deleted = 0 AND tenant_id = :tenantId";
        List<Long> userIds = namedParameterJdbcTemplate.query(
                sql,
                new MapSqlParameterSource("tenantId", tenantId),
                (rs, rowNum) -> rs.getLong("id"));
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<AdminUserRespDTO> users = adminUserApi.getUserList(userIds);
        return convertToReceiverUserOptions(users, excludeGuestRole);
    }

    private List<IotDispatchManageReceiverUserRespVO> convertToReceiverUserOptions(
            List<AdminUserRespDTO> users, boolean excludeGuestRole) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .filter(item -> Objects.equals(item.getStatus(), CommonStatusEnum.ENABLE.getStatus()))
                .filter(item -> !excludeGuestRole || !isGuestRoleUser(item.getId()))
                .sorted(Comparator.comparing(item -> trimToEmpty(item.getNickname())))
                .map(item -> {
                    IotDispatchManageReceiverUserRespVO vo = new IotDispatchManageReceiverUserRespVO();
                    vo.setId(item.getId());
                    vo.setNickname(item.getNickname());
                    vo.setDeptId(item.getDeptId());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<IotDispatchReceiveRespVO> getReceivePage(IotDispatchReceivePageReqVO reqVO) {
        refreshOverdueStatus();
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (loginUserId == null) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        normalizeReceiveQuery(reqVO);
        reqVO.setAccessUserId(hasAdminRole() ? null : loginUserId);
        PageResult<IotDispatchInstructionDO> pageResult = instructionMapper.selectReceivePage(reqVO);
        if (pageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }
        List<IotDispatchReceiveRespVO> list = pageResult.getList().stream()
                .map(this::buildReceiveResp)
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public IotDispatchReceiveRespVO getReceive(Long id) {
        refreshOverdueStatus();
        IotDispatchInstructionDO instruction = validateReceiveInstructionExists(id);
        validateReceiveViewPermission(instruction);
        return buildReceiveResp(instruction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptReceive(Long id) {
        refreshOverdueStatus();
        IotDispatchInstructionDO instruction = validateReceiveInstructionExists(id);
        validateReceiveAcceptPermission(instruction);
        if (Objects.equals(instruction.getStatus(), IotDispatchInstructionStatusEnum.EXECUTED.getStatus())) {
            throw exception(DISPATCH_RECEIVE_STATUS_INVALID);
        }
        if (Objects.equals(instruction.getReceiveStatus(), 1)) {
            return;
        }
        IotDispatchInstructionDO updateObj = new IotDispatchInstructionDO();
        updateObj.setId(instruction.getId());
        updateObj.setReceiveStatus(1);
        updateObj.setReceiveTime(LocalDateTime.now());
        instructionMapper.updateById(updateObj);
        insertInstructionLog(instruction.getId(), ACTION_RECEIVE, "接收调度指令");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReceiveResult(IotDispatchReceiveSubmitReqVO reqVO) {
        refreshOverdueStatus();
        Integer executeFlag = reqVO.getExecuteFlag();
        if (!Objects.equals(executeFlag, 0) && !Objects.equals(executeFlag, 1)) {
            throw exception(DISPATCH_RECEIVE_EXECUTE_FLAG_INVALID);
        }
        List<String> attachments = normalizeAttachments(reqVO.getAttachments());
        if (attachments.isEmpty()) {
            throw exception(DISPATCH_RECEIVE_ATTACHMENT_EMPTY);
        }
        IotDispatchInstructionDO instruction = validateReceiveInstructionExists(reqVO.getId());
        validateReceiveSubmitPermission(instruction);
        if (Objects.equals(instruction.getStatus(), IotDispatchInstructionStatusEnum.EXECUTED.getStatus())) {
            throw exception(DISPATCH_RECEIVE_STATUS_INVALID);
        }
        Integer nextStatus = Objects.equals(executeFlag, 1)
                ? IotDispatchInstructionStatusEnum.EXECUTED.getStatus()
                : resolveInitialStatus(reqVO.getFinishTime());
        List<Long> runLogIds = normalizeRunLogIds(reqVO.getRunLogIds());
        FeedbackPayload payload = new FeedbackPayload();
        payload.setExecuteFlag(executeFlag);
        payload.setFinishTime(reqVO.getFinishTime());
        payload.setAttachments(attachments);
        payload.setRunLogIds(runLogIds);

        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        IotDispatchInstructionDO updateObj = new IotDispatchInstructionDO();
        updateObj.setId(instruction.getId());
        updateObj.setReceiveStatus(1);
        if (instruction.getReceiveTime() == null) {
            updateObj.setReceiveTime(LocalDateTime.now());
        }
        updateObj.setStatus(nextStatus);
        updateObj.setFeedbackContent(JsonUtils.toJsonString(payload));
        updateObj.setFeedbackRemark(trimToEmpty(reqVO.getRemark()));
        updateObj.setFeedbackSubmitUserId(loginUserId);
        updateObj.setFeedbackSubmitUserName(resolveLoginUserName());
        updateObj.setFeedbackSubmitTime(LocalDateTime.now());
        instructionMapper.updateById(updateObj);

        // 运行日志表可能未初始化，先停用关联写入，避免提交执行结果时报错。
        insertInstructionLog(instruction.getId(), ACTION_FEEDBACK, "提交调令执行结果");
    }

    @Override
    public List<IotDispatchReceiveRespVO> getReceiveList(IotDispatchReceivePageReqVO reqVO) {
        refreshOverdueStatus();
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (loginUserId == null) {
            return Collections.emptyList();
        }
        normalizeReceiveQuery(reqVO);
        reqVO.setAccessUserId(hasAdminRole() ? null : loginUserId);
        List<IotDispatchInstructionDO> list = instructionMapper.selectReceiveList(reqVO);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::buildReceiveResp).collect(Collectors.toList());
    }

    @Override
    public List<IotDispatchReceiveRunLogOptionRespVO> getReceiveRunLogOptions(String stationId) {
        try {
            String normalizedStationId = trimToNull(stationId);
            StringBuilder sql = new StringBuilder("SELECT id, log_title, log_no, create_time FROM yz_run_log " +
                    "WHERE deleted = 0");
            MapSqlParameterSource params = new MapSqlParameterSource();
            if (normalizedStationId != null) {
                sql.append(" AND station_id = :stationId");
                params.addValue("stationId", normalizedStationId);
            }
            sql.append(" ORDER BY create_time DESC NULLS LAST, id DESC LIMIT 200");
            List<IotDispatchReceiveRunLogOptionRespVO> result = new ArrayList<>();
            namedParameterJdbcTemplate.query(sql.toString(), params, rs -> {
                IotDispatchReceiveRunLogOptionRespVO vo = new IotDispatchReceiveRunLogOptionRespVO();
                long id = rs.getLong("id");
                String logTitle = trimToNull(rs.getString("log_title"));
                String logNo = trimToNull(rs.getString("log_no"));
                vo.setId(id);
                // 调令接收页运行日志下拉仅展示运行日志名称，不拼接编号/时间。
                vo.setTitle(logTitle != null ? logTitle : (logNo != null ? logNo : ("运行日志#" + id)));
                result.add(vo);
            });
            return result;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * 规范化调令接受查询条件
     */
    private void normalizeReceiveQuery(IotDispatchReceivePageReqVO reqVO) {
        reqVO.setStatus(null);
        reqVO.setReceiveStatus(null);
        Integer executionStatus = reqVO.getExecutionStatus();
        if (executionStatus == null) {
            return;
        }
        switch (executionStatus) {
            case 0:
                reqVO.setReceiveStatus(0);
                break;
            case 1:
                reqVO.setReceiveStatus(1);
                reqVO.setStatus(IotDispatchInstructionStatusEnum.WAIT_EXECUTE.getStatus());
                break;
            case 2:
                reqVO.setStatus(IotDispatchInstructionStatusEnum.EXECUTED.getStatus());
                break;
            case 3:
                reqVO.setReceiveStatus(1);
                reqVO.setStatus(IotDispatchInstructionStatusEnum.OVERDUE.getStatus());
                break;
            default:
                break;
        }
    }

    /**
     * 组装调令接受响应
     */
    private IotDispatchReceiveRespVO buildReceiveResp(IotDispatchInstructionDO instruction) {
        IotDispatchReceiveRespVO respVO = new IotDispatchReceiveRespVO();
        respVO.setId(instruction.getId());
        respVO.setInstructionNo(instruction.getInstructionNo());
        respVO.setInstructionName(instruction.getInstructionName());
        respVO.setIssueOrgName(instruction.getIssueOrgName());
        respVO.setIssueUserName(instruction.getIssueUserName());
        respVO.setStationId(instruction.getStationId());
        respVO.setInstructionContent(instruction.getInstructionContent());
        respVO.setPlanNames(parsePlanNames(instruction.getPlanSnapshotJson()));
        respVO.setIssueTime(instruction.getCreateTime());
        respVO.setPlannedFinishTime(instruction.getPlannedFinishTime());
        respVO.setReceiverDeptName(instruction.getReceiverDeptName());
        respVO.setReceiverUserId(instruction.getReceiverUserId());
        respVO.setReceiverUserName(instruction.getReceiverUserName());
        respVO.setExecutorUserId(resolveExecutorUserId(instruction));
        respVO.setExecutorUserName(resolveExecutorUserName(instruction));
        respVO.setReceiveStatus(instruction.getReceiveStatus());
        respVO.setReceiveTime(instruction.getReceiveTime());
        Integer executionStatus = resolveReceiveExecutionStatus(instruction);
        respVO.setExecutionStatus(executionStatus);
        respVO.setExecutionStatusName(resolveReceiveExecutionStatusName(executionStatus));
        FeedbackPayload payload = parseFeedbackPayload(instruction.getFeedbackContent());
        respVO.setExecuteFlag(payload.getExecuteFlag());
        respVO.setFinishTime(payload.getFinishTime());
        respVO.setAttachments(payload.getAttachments());
        respVO.setRunLogIds(payload.getRunLogIds());
        respVO.setRemark(instruction.getFeedbackRemark());
        respVO.setSubmitUserName(instruction.getFeedbackSubmitUserName());
        respVO.setCreateTime(instruction.getCreateTime());
        return respVO;
    }

    /**
     * 计算调令接受执行状态
     */
    private Integer resolveReceiveExecutionStatus(IotDispatchInstructionDO instruction) {
        if (Objects.equals(instruction.getStatus(), IotDispatchInstructionStatusEnum.EXECUTED.getStatus())) {
            return 2;
        }
        if (!Objects.equals(instruction.getReceiveStatus(), 1)) {
            return 0;
        }
        if (Objects.equals(instruction.getStatus(), IotDispatchInstructionStatusEnum.OVERDUE.getStatus())) {
            return 3;
        }
        return 1;
    }

    /**
     * 执行状态名称
     */
    private String resolveReceiveExecutionStatusName(Integer executionStatus) {
        if (executionStatus == null) {
            return "待执行";
        }
        switch (executionStatus) {
            case 0:
                return "待接收";
            case 1:
                return "待执行";
            case 2:
                return "已执行";
            case 3:
                return "已逾期";
            default:
                return "待执行";
        }
    }

    /**
     * 解析反馈内容
     */
    private FeedbackPayload parseFeedbackPayload(String feedbackContent) {
        FeedbackPayload payload = new FeedbackPayload();
        payload.setAttachments(Collections.emptyList());
        payload.setRunLogIds(Collections.emptyList());
        if (StrUtil.isBlank(feedbackContent)) {
            return payload;
        }
        try {
            FeedbackPayload parsed = JsonUtils.parseObject(feedbackContent, FeedbackPayload.class);
            if (parsed == null) {
                return payload;
            }
            parsed.setAttachments(normalizeAttachments(parsed.getAttachments()));
            parsed.setRunLogIds(normalizeRunLogIds(parsed.getRunLogIds()));
            return parsed;
        } catch (Exception ignored) {
            return payload;
        }
    }

    private List<String> normalizeAttachments(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }
        return attachments.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> normalizeRunLogIds(List<Long> runLogIds) {
        if (runLogIds == null || runLogIds.isEmpty()) {
            return Collections.emptyList();
        }
        return runLogIds.stream()
                .filter(Objects::nonNull)
                .filter(item -> item > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    private IotDispatchInstructionDO validateReceiveInstructionExists(Long id) {
        IotDispatchInstructionDO instruction = instructionMapper.selectById(id);
        if (instruction == null) {
            throw exception(DISPATCH_RECEIVE_NOT_EXISTS);
        }
        return instruction;
    }

    private void validateReceiveViewPermission(IotDispatchInstructionDO instruction) {
        if (hasAdminRole()) {
            return;
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!Objects.equals(loginUserId, instruction.getReceiverUserId())
                && !Objects.equals(loginUserId, resolveExecutorUserId(instruction))) {
            throw exception(DISPATCH_RECEIVE_FORBIDDEN);
        }
    }

    private void validateReceiveAcceptPermission(IotDispatchInstructionDO instruction) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!Objects.equals(loginUserId, resolveExecutorUserId(instruction))) {
            throw exception(DISPATCH_RECEIVE_ACCEPT_FORBIDDEN);
        }
    }

    private void validateReceiveSubmitPermission(IotDispatchInstructionDO instruction) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!Objects.equals(loginUserId, resolveExecutorUserId(instruction))) {
            throw exception(DISPATCH_RECEIVE_SUBMIT_FORBIDDEN);
        }
    }

    /**
     * 绑定运行日志
     */
    private void bindRunLogs(Long instructionId, List<Long> runLogIds) {
        if (instructionId == null) {
            return;
        }
        try {
            String clearSql = "UPDATE yz_run_log SET dispatch_instruction_id = NULL, update_time = NOW() " +
                    "WHERE deleted = 0 AND dispatch_instruction_id = :instructionId";
            namedParameterJdbcTemplate.update(clearSql, new MapSqlParameterSource("instructionId", instructionId));
            if (runLogIds == null || runLogIds.isEmpty()) {
                return;
            }
            String bindSql = "UPDATE yz_run_log SET dispatch_instruction_id = :instructionId, update_time = NOW() " +
                    "WHERE deleted = 0 AND id IN (:ids)";
            MapSqlParameterSource bindParams = new MapSqlParameterSource();
            bindParams.addValue("instructionId", instructionId);
            bindParams.addValue("ids", runLogIds);
            namedParameterJdbcTemplate.update(bindSql, bindParams);
        } catch (Exception ignored) {
            // 运行日志模块未初始化或字段不一致时，不影响主流程
        }
    }

    /**
     * 校验并查询调度方案
     */
    private List<IotDispatchPlanDO> validateAndGetPlans(List<Long> planIds) {
        if (planIds == null || planIds.isEmpty()) {
            throw exception(DISPATCH_MANAGE_PLAN_EMPTY);
        }
        Set<Long> distinctIds = new LinkedHashSet<>(planIds);
        List<IotDispatchPlanDO> plans = dispatchPlanMapper.selectBatchIds(distinctIds);
        if (plans == null || plans.size() != distinctIds.size()) {
            throw exception(DISPATCH_MANAGE_PLAN_NOT_EXISTS);
        }
        Map<Long, IotDispatchPlanDO> planMap = plans.stream()
                .collect(Collectors.toMap(IotDispatchPlanDO::getId, item -> item, (a, b) -> a));
        List<IotDispatchPlanDO> orderedPlans = new ArrayList<>(distinctIds.size());
        for (Long planId : distinctIds) {
            IotDispatchPlanDO plan = planMap.get(planId);
            if (plan == null) {
                throw exception(DISPATCH_MANAGE_PLAN_NOT_EXISTS);
            }
            orderedPlans.add(plan);
        }
        return orderedPlans;
    }

    /**
     * 校验接收单位、接收人、执行人
     */
    private ReceiverSnapshot validateAndGetReceiver(Long receiverDeptId, Long receiverUserId, Long executorUserId) {
        if (receiverDeptId == null) {
            throw exception(DISPATCH_MANAGE_RECEIVER_DEPT_EMPTY);
        }
        if (receiverUserId == null) {
            throw exception(DISPATCH_MANAGE_RECEIVER_USER_EMPTY);
        }
        if (executorUserId == null) {
            throw exception(DISPATCH_MANAGE_EXECUTOR_USER_EMPTY);
        }
        deptApi.validateDeptList(Collections.singleton(receiverDeptId));
        DeptRespDTO dept = deptApi.getDept(receiverDeptId);
        if (dept == null) {
            throw exception(DISPATCH_MANAGE_RECEIVER_DEPT_EMPTY);
        }
        AdminUserRespDTO receiver = adminUserApi.getUser(receiverUserId);
        if (receiver == null
                || !Objects.equals(receiver.getStatus(), CommonStatusEnum.ENABLE.getStatus())
                || !Objects.equals(receiver.getDeptId(), receiverDeptId)) {
            throw exception(DISPATCH_MANAGE_RECEIVER_USER_INVALID);
        }
        AdminUserRespDTO executor = adminUserApi.getUser(executorUserId);
        if (executor == null
                || !Objects.equals(executor.getStatus(), CommonStatusEnum.ENABLE.getStatus())
                || isGuestRoleUser(executorUserId)) {
            throw exception(DISPATCH_MANAGE_EXECUTOR_USER_INVALID);
        }
        ReceiverSnapshot snapshot = new ReceiverSnapshot();
        snapshot.receiverDeptId = receiverDeptId;
        snapshot.receiverDeptName = trimToEmpty(dept.getName());
        snapshot.receiverUserId = receiverUserId;
        snapshot.receiverUserName = trimToEmpty(receiver.getNickname());
        snapshot.executorUserId = executorUserId;
        snapshot.executorUserName = trimToEmpty(executor.getNickname());
        return snapshot;
    }

    /**
     * 查询指令是否存在
     */
    private IotDispatchInstructionDO validateInstructionExists(Long id) {
        IotDispatchInstructionDO instruction = instructionMapper.selectById(id);
        if (instruction == null) {
            throw exception(DISPATCH_MANAGE_NOT_EXISTS);
        }
        return instruction;
    }

    /**
     * 组装响应对象
     */
    private IotDispatchManageRespVO buildResp(IotDispatchInstructionDO instruction, Map<Long, Integer> runLogCountMap) {
        IotDispatchManageRespVO respVO = new IotDispatchManageRespVO();
        respVO.setId(instruction.getId());
        respVO.setInstructionNo(instruction.getInstructionNo());
        respVO.setInstructionName(instruction.getInstructionName());
        respVO.setIssueOrgName(instruction.getIssueOrgName());
        respVO.setIssueUserId(instruction.getIssueUserId());
        respVO.setIssueUserName(instruction.getIssueUserName());
        respVO.setStationId(instruction.getStationId());
        respVO.setInstructionContent(instruction.getInstructionContent());
        respVO.setPlanIds(parsePlanIds(instruction.getPlanIds()));
        respVO.setPlanNames(parsePlanNames(instruction.getPlanSnapshotJson()));
        respVO.setPlannedFinishTime(instruction.getPlannedFinishTime());
        respVO.setReceiverDeptId(instruction.getReceiverDeptId());
        respVO.setReceiverDeptName(instruction.getReceiverDeptName());
        respVO.setReceiverUserId(instruction.getReceiverUserId());
        respVO.setReceiverUserName(instruction.getReceiverUserName());
        respVO.setExecutorUserId(resolveExecutorUserId(instruction));
        respVO.setExecutorUserName(resolveExecutorUserName(instruction));
        respVO.setStatus(instruction.getStatus());
        respVO.setStatusName(IotDispatchInstructionStatusEnum.getNameByStatus(instruction.getStatus()));
        respVO.setRunLogCount(runLogCountMap.getOrDefault(instruction.getId(), 0));
        respVO.setOperationTicketUrl(instruction.getOperationTicketUrl());
        respVO.setFeedbackContent(instruction.getFeedbackContent());
        respVO.setFeedbackRemark(instruction.getFeedbackRemark());
        respVO.setFeedbackSubmitUserName(instruction.getFeedbackSubmitUserName());
        respVO.setFeedbackSubmitTime(instruction.getFeedbackSubmitTime());
        respVO.setRemark(instruction.getRemark());
        respVO.setCreator(instruction.getCreator());
        respVO.setCreateTime(instruction.getCreateTime());
        respVO.setUpdateTime(instruction.getUpdateTime());
        return respVO;
    }

    /**
     * 解析调度方案名称
     */
    private List<String> parsePlanNames(String planSnapshotJson) {
        if (StrUtil.isBlank(planSnapshotJson)) {
            return Collections.emptyList();
        }
        try {
            List<PlanSnapshotItem> snapshots = JsonUtils.parseArray(planSnapshotJson, PlanSnapshotItem.class);
            return snapshots.stream()
                    .map(PlanSnapshotItem::getPlanName)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * 解析调度方案 ID 列表
     */
    private List<Long> parsePlanIds(String planIdsText) {
        if (StrUtil.isBlank(planIdsText)) {
            return Collections.emptyList();
        }
        List<String> values = StrUtil.split(planIdsText, ',');
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>(values.size());
        for (String value : values) {
            if (StrUtil.isBlank(value)) {
                continue;
            }
            try {
                result.add(Long.parseLong(value.trim()));
            } catch (NumberFormatException ignored) {
                // 忽略异常值
            }
        }
        return result;
    }

    /**
     * 查询运行日志数量映射，若运行日志模块未落库则返回空映射
     */
    private Map<Long, Integer> queryRunLogCountMap(Collection<Long> instructionIds) {
        if (instructionIds == null || instructionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            String sql = "SELECT dispatch_instruction_id AS instruction_id, COUNT(1) AS cnt " +
                    "FROM yz_run_log " +
                    "WHERE deleted = 0 AND dispatch_instruction_id IN (:ids) " +
                    "GROUP BY dispatch_instruction_id";
            MapSqlParameterSource params = new MapSqlParameterSource("ids", instructionIds);
            Map<Long, Integer> result = new LinkedHashMap<>();
            namedParameterJdbcTemplate.query(sql, params, rs -> {
                result.put(rs.getLong("instruction_id"), rs.getInt("cnt"));
            });
            return result;
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    /**
     * 刷新逾期状态
     */
    private void refreshOverdueStatus() {
        instructionMapper.updateOverdueByNow(LocalDateTime.now());
    }

    /**
     * 写入操作日志
     */
    private void insertInstructionLog(Long instructionId, Integer actionType, String actionDesc) {
        IotDispatchInstructionLogDO log = new IotDispatchInstructionLogDO();
        log.setInstructionId(instructionId);
        log.setActionType(actionType);
        log.setActionDesc(trimToEmpty(actionDesc));
        log.setOperatorUserId(SecurityFrameworkUtils.getLoginUserId());
        log.setOperatorUserName(resolveLoginUserName());
        log.setActionTime(LocalDateTime.now());
        instructionLogMapper.insert(log);
    }

    /**
     * 生成调度编号：DD-YYYYMMDD-XX
     */
    private String generateInstructionNo() {
        String datePart = LocalDate.now().format(INSTRUCTION_NO_DATE_FORMATTER);
        String prefix = INSTRUCTION_NO_PREFIX + "-" + datePart + "-";
        String latestNo = instructionMapper.selectLatestInstructionNoByPrefix(prefix);
        int nextSeq = 1;
        if (StrUtil.isNotBlank(latestNo) && latestNo.length() > prefix.length()) {
            String seqPart = latestNo.substring(prefix.length());
            try {
                nextSeq = Integer.parseInt(seqPart) + 1;
            } catch (NumberFormatException ignored) {
                nextSeq = 1;
            }
        }
        return prefix + String.format("%02d", nextSeq);
    }

    /**
     * 计算初始执行状态
     */
    private Integer resolveInitialStatus(LocalDateTime plannedFinishTime) {
        Integer status = plannedFinishTime != null && plannedFinishTime.isBefore(LocalDateTime.now())
                ? IotDispatchInstructionStatusEnum.OVERDUE.getStatus()
                : IotDispatchInstructionStatusEnum.WAIT_EXECUTE.getStatus();
        if (!IotDispatchInstructionStatusEnum.isValid(status)) {
            throw exception(DISPATCH_MANAGE_STATUS_INVALID);
        }
        return status;
    }

    /**
     * 组装调度方案快照
     */
    private List<PlanSnapshotItem> buildPlanSnapshots(List<IotDispatchPlanDO> plans) {
        return plans.stream().map(item -> {
            PlanSnapshotItem snapshot = new PlanSnapshotItem();
            snapshot.setPlanId(item.getId());
            snapshot.setPlanNo(trimToEmpty(item.getPlanNo()));
            snapshot.setPlanName(trimToEmpty(item.getPlanName()));
            return snapshot;
        }).collect(Collectors.toList());
    }

    /**
     * 拼接调度方案 ID 列表
     */
    private String joinPlanIds(List<IotDispatchPlanDO> plans) {
        return plans.stream()
                .map(IotDispatchPlanDO::getId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * 获取登录人名称
     */
    private String resolveIssueUserName(String issueUserName) {
        String manualIssueUserName = trimToNull(issueUserName);
        return manualIssueUserName != null ? manualIssueUserName : resolveLoginUserName();
    }

    /**
     * 校验当前用户是否有查询该指令的权限
     */
    private void validateQueryPermission(IotDispatchInstructionDO instruction) {
        if (hasAdminRole()) {
            return;
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!Objects.equals(loginUserId, instruction.getReceiverUserId())) {
            throw exception(DISPATCH_MANAGE_QUERY_FORBIDDEN);
        }
    }

    /**
     * 校验当前用户是否有编辑/删除权限
     */
    private void validateUpdateDeletePermission(IotDispatchInstructionDO instruction) {
        if (!hasAdminRole()) {
            throw exception(DISPATCH_MANAGE_UPDATE_DELETE_FORBIDDEN);
        }
        if (Objects.equals(instruction.getStatus(), IotDispatchInstructionStatusEnum.EXECUTED.getStatus())) {
            throw exception(DISPATCH_MANAGE_EXECUTED_LOCKED);
        }
    }

    /**
     * 解析查询使用的接收人过滤条件
     */
    private Long resolveQueryReceiverUserId() {
        if (hasAdminRole()) {
            return null;
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        return loginUserId == null ? -1L : loginUserId;
    }

    /**
     * 是否管理员角色（超管、租户管理员）
     */
    private boolean hasAdminRole() {
        return securityFrameworkService.hasAnyRoles(
                RoleCodeEnum.SUPER_ADMIN.getCode(), RoleCodeEnum.TENANT_ADMIN.getCode());
    }

    private Long resolveExecutorUserId(IotDispatchInstructionDO instruction) {
        if (instruction == null) {
            return null;
        }
        return instruction.getExecutorUserId() != null ? instruction.getExecutorUserId() : instruction.getReceiverUserId();
    }

    private String resolveExecutorUserName(IotDispatchInstructionDO instruction) {
        if (instruction == null) {
            return "";
        }
        String executorUserName = trimToNull(instruction.getExecutorUserName());
        if (executorUserName != null) {
            return executorUserName;
        }
        return trimToEmpty(instruction.getReceiverUserName());
    }

    private boolean isGuestRoleUser(Long userId) {
        if (userId == null) {
            return false;
        }
        return permissionApi.hasAnyRoles(userId, GUEST_ROLE_CODE, GUEST_ROLE_CODE.toLowerCase());
    }

    private String resolveLoginUserName() {
        String nickname = trimToNull(SecurityFrameworkUtils.getLoginUserNickname());
        if (nickname != null) {
            return nickname;
        }
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return userId == null ? "" : String.valueOf(userId);
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
     * 调令执行反馈载荷
     */
    private static class FeedbackPayload {
        private Integer executeFlag;
        private LocalDateTime finishTime;
        private List<String> attachments;
        private List<Long> runLogIds;

        public Integer getExecuteFlag() {
            return executeFlag;
        }

        public void setExecuteFlag(Integer executeFlag) {
            this.executeFlag = executeFlag;
        }

        public LocalDateTime getFinishTime() {
            return finishTime;
        }

        public void setFinishTime(LocalDateTime finishTime) {
            this.finishTime = finishTime;
        }

        public List<String> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<String> attachments) {
            this.attachments = attachments;
        }

        public List<Long> getRunLogIds() {
            return runLogIds;
        }

        public void setRunLogIds(List<Long> runLogIds) {
            this.runLogIds = runLogIds;
        }
    }

    /**
     * 接收人快照
     */
    private static class ReceiverSnapshot {
        private Long receiverDeptId;
        private String receiverDeptName;
        private Long receiverUserId;
        private String receiverUserName;
        private Long executorUserId;
        private String executorUserName;
    }

    /**
     * 调度方案快照
     */
    private static class PlanSnapshotItem {
        private Long planId;
        private String planNo;
        private String planName;

        public Long getPlanId() {
            return planId;
        }

        public void setPlanId(Long planId) {
            this.planId = planId;
        }

        public String getPlanNo() {
            return planNo;
        }

        public void setPlanNo(String planNo) {
            this.planNo = planNo;
        }

        public String getPlanName() {
            return planName;
        }

        public void setPlanName(String planName) {
            this.planName = planName;
        }
    }
}

