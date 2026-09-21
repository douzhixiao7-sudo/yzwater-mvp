package com.sydigit.yzwater.module.iot.service.inspectiontask;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.framework.security.core.service.SecurityFrameworkService;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.framework.tenant.core.context.TenantContextHolder;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskSubmitResultItemVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskSubmitResultRecordVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskSubmitResultReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo.IotInspectionTaskTargetSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLineDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanTargetDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask.IotInspectionTaskDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectiontask.IotInspectionTaskTargetDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionline.IotInspectionLineMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionplan.IotInspectionPlanMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionplan.IotInspectionPlanTargetMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard.IotInspectionStandardMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectiontask.IotInspectionTaskMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectiontask.IotInspectionTaskTargetMapper;
import com.sydigit.yzwater.module.iot.service.faultrepair.IotFaultRepairService;
import com.sydigit.yzwater.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_DELETE_FORBIDDEN_TASK_PROCESSED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_DATE_RANGE_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_LINE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_OBJECT_TYPE_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_PLAN_READ_ONLY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_SUBMIT_ABNORMAL_REMARK_REQUIRED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_STANDARD_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_SUBMIT_FORBIDDEN;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_SUBMIT_FAULT_DEVICE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_SUBMIT_STATUS_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_TARGET_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_TASK_TARGET_NOT_EXISTS;

/**
 * 巡检任务 Service 实现
 */
@Service
@Validated
public class IotInspectionTaskServiceImpl implements IotInspectionTaskService {

    private static final Integer OBJECT_TYPE_DEVICE = 1;
    private static final Integer OBJECT_TYPE_LOCATION = 2;
    private static final Integer SOURCE_TYPE_PLAN = 1;
    private static final Integer SOURCE_TYPE_MANUAL = 2;
    private static final Integer TASK_STATUS_NOT_STARTED = 0;
    private static final Integer TASK_STATUS_UNFINISHED = 1;
    private static final Integer TASK_STATUS_FINISHED = 2;
    private static final Integer TASK_STATUS_OVERDUE = 3;
    private static final Integer WORKFLOW_STATUS_NOT_STARTED = 0;
    private static final Integer WORKFLOW_STATUS_RUNNING = 1;
    private static final Integer WORKFLOW_STATUS_FINISHED = 2;
    private static final DateTimeFormatter TASK_NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private IotInspectionTaskMapper taskMapper;
    @Resource
    private IotInspectionTaskTargetMapper taskTargetMapper;
    @Resource
    private IotInspectionPlanMapper planMapper;
    @Resource
    private IotInspectionPlanTargetMapper planTargetMapper;
    @Resource
    private IotInspectionStandardMapper standardMapper;
    @Resource
    private IotInspectionLineMapper lineMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private SecurityFrameworkService securityFrameworkService;
    @Resource
    private IotFaultRepairService faultRepairService;
    @Resource
    private IotDeviceMapper deviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(IotInspectionTaskSaveReqVO createReqVO) {
        Integer objectType = normalizeObjectType(createReqVO.getObjectType());
        List<IotInspectionTaskTargetSaveReqVO> targets = normalizeTargets(createReqVO.getTargets());
        Map<Long, TargetMeta> targetMetaMap = resolveTargetMetaMap(objectType, targets);
        StandardMeta standardMeta = resolveStandardMeta(createReqVO.getStandardId(), true);
        LineMeta lineMeta = resolveLineMeta(createReqVO.getLineId());
        LocalDateTime planStartTime = createReqVO.getPlanStartTime();
        LocalDateTime planEndTime = createReqVO.getPlanEndTime();
        validatePlanTimeRange(planStartTime, planEndTime);

        TaskCreateCommand command = new TaskCreateCommand();
        command.taskName = trimToEmpty(createReqVO.getTaskName());
        command.stationId = resolveTaskStationId(trimToEmpty(createReqVO.getStationId()), objectType, targets, targetMetaMap);
        command.inspectionType = trimToEmpty(createReqVO.getInspectionType());
        command.objectType = objectType;
        command.standardId = standardMeta.standardId;
        command.standardName = standardMeta.standardName;
        command.lineId = lineMeta == null ? null : lineMeta.lineId;
        command.lineName = lineMeta == null ? "" : lineMeta.lineName;
        command.sourceType = SOURCE_TYPE_MANUAL;
        command.planId = null;
        command.planStartTime = planStartTime;
        command.planEndTime = planEndTime;
        command.executorUserId = createReqVO.getExecutorUserId();
        command.executorName = resolveExecutorName(createReqVO.getExecutorUserId(), createReqVO.getExecutorName());
        command.taskDesc = trimToEmpty(createReqVO.getTaskDesc());
        command.remark = trimToEmpty(createReqVO.getRemark());
        command.targetSnapshots = buildTargetSnapshots(objectType, targets, targetMetaMap);
        return createTaskInternal(command);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(IotInspectionTaskSaveReqVO updateReqVO) {
        if (updateReqVO.getId() == null) {
            throw exception(INSPECTION_TASK_NOT_EXISTS);
        }
        IotInspectionTaskDO existedTask = validateTaskExists(updateReqVO.getId());
        if (Objects.equals(existedTask.getSourceType(), SOURCE_TYPE_PLAN)) {
            throw exception(INSPECTION_TASK_PLAN_READ_ONLY);
        }

        Integer objectType = normalizeObjectType(updateReqVO.getObjectType());
        List<IotInspectionTaskTargetSaveReqVO> targets = normalizeTargets(updateReqVO.getTargets());
        Map<Long, TargetMeta> targetMetaMap = resolveTargetMetaMap(objectType, targets);
        StandardMeta standardMeta = resolveStandardMeta(updateReqVO.getStandardId(), true);
        LineMeta lineMeta = resolveLineMeta(updateReqVO.getLineId());
        LocalDateTime planStartTime = updateReqVO.getPlanStartTime();
        LocalDateTime planEndTime = updateReqVO.getPlanEndTime();
        validatePlanTimeRange(planStartTime, planEndTime);

        IotInspectionTaskDO updateObj = new IotInspectionTaskDO();
        updateObj.setId(existedTask.getId());
        updateObj.setTaskName(trimToEmpty(updateReqVO.getTaskName()));
        updateObj.setStationId(resolveTaskStationId(trimToEmpty(updateReqVO.getStationId()), objectType, targets, targetMetaMap));
        updateObj.setInspectionType(trimToEmpty(updateReqVO.getInspectionType()));
        updateObj.setObjectType(objectType);
        updateObj.setStandardId(standardMeta.standardId);
        updateObj.setStandardName(standardMeta.standardName);
        updateObj.setLineId(lineMeta == null ? null : lineMeta.lineId);
        updateObj.setLineName(lineMeta == null ? "" : lineMeta.lineName);
        updateObj.setPlanStartTime(planStartTime);
        updateObj.setPlanEndTime(planEndTime);
        updateObj.setExecutorUserId(updateReqVO.getExecutorUserId());
        updateObj.setExecutorName(resolveExecutorName(updateReqVO.getExecutorUserId(), updateReqVO.getExecutorName()));
        updateObj.setTaskDesc(trimToEmpty(updateReqVO.getTaskDesc()));
        updateObj.setRemark(trimToEmpty(updateReqVO.getRemark()));
        updateObj.setTaskStatus(calculateTaskStatus(planStartTime, planEndTime, existedTask.getSubmitTime()));
        updateObj.setWorkflowStatus(WORKFLOW_STATUS_NOT_STARTED);
        updateObj.setProcessDefinitionKey("");
        updateObj.setProcessInstanceId("");
        updateObj.setProcessStartTime(null);
        updateObj.setProcessEndTime(null);
        taskMapper.updateById(updateObj);

        List<TargetSnapshot> targetSnapshots = buildTargetSnapshots(objectType, targets, targetMetaMap);
        taskTargetMapper.deleteByTaskId(existedTask.getId());
        saveTaskTargets(existedTask.getId(), targetSnapshots);
        startTaskWorkflow(existedTask.getId(), updateReqVO.getExecutorUserId(),
                resolveExecutorName(updateReqVO.getExecutorUserId(), updateReqVO.getExecutorName()),
                trimToEmpty(updateReqVO.getTaskName()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTaskFromPlan(Long planId) {
        return createTaskFromPlanInternal(planId, null, null, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTaskFromPlanByWindow(Long planId, LocalDateTime planStartTime, LocalDateTime planEndTime) {
        return createTaskFromPlanInternal(planId, planStartTime, planEndTime, false);
    }

    @Override
    public boolean isPlanEditable(Long planId) {
        if (planId == null) {
            return true;
        }
        IotInspectionTaskDO latestPlanTask = taskMapper.selectLatestByPlanId(planId);
        if (latestPlanTask == null) {
            return true;
        }
        return isPlanTaskEditableForSync(latestPlanTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cascadeDeletePlanAutoTasks(Long planId) {
        if (planId == null) {
            return;
        }
        List<IotInspectionTaskDO> autoTasks = taskMapper.selectAutoTaskListByPlanId(planId);
        if (autoTasks.isEmpty()) {
            return;
        }
        for (IotInspectionTaskDO autoTask : autoTasks) {
            if (!isPlanTaskRunningAndUnfinished(autoTask)) {
                throw exception(INSPECTION_PLAN_DELETE_FORBIDDEN_TASK_PROCESSED);
            }
        }
        for (IotInspectionTaskDO autoTask : autoTasks) {
            if (autoTask.getId() != null) {
                deleteTask(autoTask.getId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTaskResult(IotInspectionTaskSubmitResultReqVO reqVO) {
        IotInspectionTaskDO task = validateTaskExists(reqVO.getId());
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!Objects.equals(task.getExecutorUserId(), loginUserId)) {
            throw exception(INSPECTION_TASK_SUBMIT_FORBIDDEN);
        }
        Integer currentTaskStatus = calculateTaskStatus(task.getPlanStartTime(), task.getPlanEndTime(), task.getSubmitTime());
        if (!Objects.equals(currentTaskStatus, TASK_STATUS_UNFINISHED)
                && !Objects.equals(currentTaskStatus, TASK_STATUS_OVERDUE)) {
            throw exception(INSPECTION_TASK_SUBMIT_STATUS_INVALID);
        }

        List<IotInspectionTaskSubmitResultItemVO> normalizedItems = normalizeSubmitResultItems(reqVO.getItems());
        validateAbnormalCheckRemark(normalizedItems);
        LocalDateTime submitTime = LocalDateTime.now();
        if (Boolean.TRUE.equals(reqVO.getAutoCreateFaultRecords())) {
            createFaultRepairsBySubmitResult(task, normalizedItems, loginUserId, submitTime);
        }

        IotInspectionTaskDO updateObj = new IotInspectionTaskDO();
        updateObj.setId(task.getId());
        updateObj.setTaskStatus(TASK_STATUS_FINISHED);
        updateObj.setAbnormalCount(Math.max(reqVO.getAbnormalCount() == null ? 0 : reqVO.getAbnormalCount(), 0));
        updateObj.setItemTotalCount(normalizedItems.size());
        updateObj.setItemFinishedCount((int) normalizedItems.stream()
                .filter(item -> StrUtil.isNotBlank(item.getCheckResult()))
                .count());
        updateObj.setResultItemsJson(normalizedItems.isEmpty() ? null : JsonUtils.toJsonString(normalizedItems));
        updateObj.setSubmitTime(submitTime);
        updateObj.setWorkflowStatus(WORKFLOW_STATUS_FINISHED);
        updateObj.setProcessEndTime(submitTime);
        if (StrUtil.isNotBlank(reqVO.getRemark())) {
            updateObj.setRemark(reqVO.getRemark().trim());
        }
        taskMapper.updateById(updateObj);
        syncPlanStatusByTask(task.getPlanId(), TASK_STATUS_FINISHED, WORKFLOW_STATUS_FINISHED);
    }

    /**
     * 基于巡检计划创建任务，可指定时间窗；syncLatestPending=true 时支持计划编辑覆盖未提交自动任务。
     */
    private Long createTaskFromPlanInternal(Long planId,
                                            LocalDateTime planStartTimeOverride,
                                            LocalDateTime planEndTimeOverride,
                                            boolean syncLatestPending) {
        IotInspectionPlanDO plan = planMapper.selectById(planId);
        if (plan == null) {
            throw exception(INSPECTION_PLAN_NOT_EXISTS);
        }
        List<IotInspectionPlanTargetDO> planTargets = planTargetMapper.selectListByPlanId(planId);
        if (planTargets.isEmpty()) {
            throw exception(INSPECTION_TASK_TARGET_EMPTY);
        }
        Integer objectType = normalizeObjectType(plan.getObjectType());
        StandardMeta standardMeta = resolveStandardMeta(plan.getStandardId(), false);
        LineMeta lineMeta = resolveLineMeta(plan.getLineId());
        LocalDateTime planStartTimeValue = planStartTimeOverride == null ? plan.getPlanStartTime() : planStartTimeOverride;
        LocalDateTime planEndTimeValue = planEndTimeOverride == null ? plan.getPlanEndTime() : planEndTimeOverride;
        validatePlanTimeRange(planStartTimeValue, planEndTimeValue);

        TaskCreateCommand command = new TaskCreateCommand();
        command.taskName = buildPlanTaskName(plan.getPlanName());
        command.stationId = resolveTaskStationIdFromPlan(trimToEmpty(plan.getStationId()), objectType, planTargets);
        command.inspectionType = trimToEmpty(plan.getInspectionType());
        command.objectType = objectType;
        command.standardId = standardMeta.standardId;
        command.standardName = standardMeta.standardName;
        command.lineId = lineMeta == null ? null : lineMeta.lineId;
        command.lineName = lineMeta == null ? "" : lineMeta.lineName;
        command.sourceType = SOURCE_TYPE_PLAN;
        command.planId = plan.getId();
        command.planStartTime = planStartTimeValue;
        command.planEndTime = planEndTimeValue;
        command.executorUserId = plan.getExecutorUserId();
        command.executorName = resolveExecutorName(plan.getExecutorUserId(), plan.getExecutorName());
        command.taskDesc = StrUtil.isBlank(plan.getRemark()) ? "由巡检计划自动生成" : plan.getRemark().trim();
        command.remark = "";
        command.targetSnapshots = buildTargetSnapshotsFromPlan(objectType, planTargets);
        IotInspectionTaskDO existedTask = taskMapper.selectByPlanWindow(planId, planStartTimeValue, planEndTimeValue);
        if (existedTask != null) {
            if (syncLatestPending && shouldSyncPlanTaskOnPlanEdit(existedTask)) {
                updatePlanTaskInternal(existedTask, command);
            }
            syncPlanStatusByTask(existedTask.getPlanId(),
                    calculateTaskStatus(existedTask.getPlanStartTime(), existedTask.getPlanEndTime(), existedTask.getSubmitTime()),
                    existedTask.getWorkflowStatus());
            return existedTask.getId();
        }
        if (syncLatestPending) {
            // 计划更新后，仅覆盖“未完成且流程进行中”的自动任务，避免误改已完成或已终止任务。
            IotInspectionTaskDO latestPlanTask = taskMapper.selectLatestByPlanId(planId);
            if (shouldSyncPlanTaskOnPlanEdit(latestPlanTask)) {
                updatePlanTaskInternal(latestPlanTask, command);
                return latestPlanTask.getId();
            }
        }
        return createTaskInternal(command);
    }

    /**
     * 判定计划编辑时是否允许同步覆盖任务。
     * 仅自动任务且满足“未完成 + 流程进行中”时允许覆盖。
     */
    private boolean shouldSyncPlanTaskOnPlanEdit(IotInspectionTaskDO task) {
        if (task == null || !Objects.equals(task.getSourceType(), SOURCE_TYPE_PLAN)) {
            return false;
        }
        return isPlanTaskEditableForSync(task);
    }

    /**
     * 自动任务是否为“未完成 + 流程进行中”。
     */
    private boolean isPlanTaskRunningAndUnfinished(IotInspectionTaskDO task) {
        if (task == null) {
            return false;
        }
        Integer currentTaskStatus = calculateTaskStatus(task.getPlanStartTime(), task.getPlanEndTime(), task.getSubmitTime());
        return Objects.equals(currentTaskStatus, TASK_STATUS_UNFINISHED)
                && Objects.equals(task.getWorkflowStatus(), WORKFLOW_STATUS_RUNNING);
    }

    /**
     * 自动任务是否允许随计划编辑同步（未开始/未完成）。
     */
    private boolean isPlanTaskEditableForSync(IotInspectionTaskDO task) {
        if (task == null) {
            return false;
        }
        Integer currentTaskStatus = calculateTaskStatus(task.getPlanStartTime(), task.getPlanEndTime(), task.getSubmitTime());
        return Objects.equals(currentTaskStatus, TASK_STATUS_NOT_STARTED)
                || Objects.equals(currentTaskStatus, TASK_STATUS_UNFINISHED);
    }

    /**
     * 任务状态变化后同步巡检计划状态。
     */
    private void syncPlanStatusByTask(Long planId, Integer taskStatus, Integer workflowStatus) {
        if (planId == null) {
            return;
        }
        IotInspectionPlanDO updateObj = new IotInspectionPlanDO();
        updateObj.setId(planId);
        updateObj.setPlanStatus(resolvePlanStatusByTask(taskStatus, workflowStatus));
        planMapper.updateById(updateObj);
    }

    /**
     * 由巡检任务状态推导巡检计划状态。
     */
    private Integer resolvePlanStatusByTask(Integer taskStatus, Integer workflowStatus) {
        if (Objects.equals(taskStatus, TASK_STATUS_FINISHED) && Objects.equals(workflowStatus, WORKFLOW_STATUS_FINISHED)) {
            return TASK_STATUS_FINISHED;
        }
        if (Objects.equals(taskStatus, TASK_STATUS_OVERDUE)) {
            return TASK_STATUS_OVERDUE;
        }
        if (Objects.equals(taskStatus, TASK_STATUS_NOT_STARTED)) {
            return TASK_STATUS_NOT_STARTED;
        }
        return TASK_STATUS_UNFINISHED;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        IotInspectionTaskDO task = validateTaskExists(id);
        renameTaskNoBeforeDelete(task);
        taskTargetMapper.deleteByTaskId(id);
        taskMapper.deleteById(id);
    }

    @Override
    public IotInspectionTaskDO getTask(Long id) {
        IotInspectionTaskDO task = taskMapper.selectById(id);
        if (task != null) {
            task.setTaskStatus(calculateTaskStatus(task.getPlanStartTime(), task.getPlanEndTime(), task.getSubmitTime()));
        }
        return task;
    }

    @Override
    public IotInspectionTaskDO validateTaskExists(Long id) {
        IotInspectionTaskDO task = taskMapper.selectById(id);
        if (task == null) {
            throw exception(INSPECTION_TASK_NOT_EXISTS);
        }
        return task;
    }

    @Override
    public PageResult<IotInspectionTaskDO> getTaskPage(IotInspectionTaskPageReqVO pageReqVO) {
        // 非管理员仅允许查看“执行人 = 当前登录人”的任务列表
        if (!securityFrameworkService.hasAnyRoles(
                RoleCodeEnum.TENANT_ADMIN.getCode(),
                RoleCodeEnum.SUPER_ADMIN.getCode(),
                RoleCodeEnum.CRM_ADMIN.getCode())) {
            pageReqVO.setExecutorUserId(SecurityFrameworkUtils.getLoginUserId());
        }
        PageResult<IotInspectionTaskDO> pageResult = taskMapper.selectPage(pageReqVO);
        pageResult.getList().forEach(task ->
                task.setTaskStatus(calculateTaskStatus(task.getPlanStartTime(), task.getPlanEndTime(), task.getSubmitTime())));
        return pageResult;
    }

    @Override
    public List<IotInspectionTaskTargetDO> getTaskTargetList(Long taskId) {
        return taskTargetMapper.selectListByTaskId(taskId);
    }

    @Override
    public List<IotInspectionTaskTargetDO> getTaskTargetList(Collection<Long> taskIds) {
        return taskTargetMapper.selectListByTaskIds(taskIds);
    }

    /**
     * 执行任务创建落库。
     */
    private Long createTaskInternal(TaskCreateCommand command) {
        IotInspectionTaskDO task = new IotInspectionTaskDO();
        task.setTaskNo(generateTaskNo(command.stationId));
        task.setTaskName(command.taskName);
        task.setStationId(command.stationId);
        task.setInspectionType(command.inspectionType);
        task.setObjectType(command.objectType);
        task.setStandardId(command.standardId);
        task.setStandardName(command.standardName);
        task.setLineId(command.lineId);
        task.setLineName(command.lineName);
        task.setPlanId(command.planId);
        task.setSourceType(command.sourceType);
        task.setPlanStartTime(command.planStartTime);
        task.setPlanEndTime(command.planEndTime);
        task.setExecutorUserId(command.executorUserId);
        task.setExecutorName(command.executorName);
        task.setTaskStatus(calculateTaskStatus(command.planStartTime, command.planEndTime, null));
        task.setAbnormalCount(0);
        task.setItemTotalCount(0);
        task.setItemFinishedCount(0);
        task.setSubmitTime(null);
        task.setWorkflowStatus(WORKFLOW_STATUS_NOT_STARTED);
        task.setProcessDefinitionKey("");
        task.setProcessInstanceId("");
        task.setProcessStartTime(null);
        task.setProcessEndTime(null);
        task.setTaskDesc(command.taskDesc);
        task.setRemark(command.remark);
        taskMapper.insert(task);

        startTaskWorkflow(task.getId(), command.executorUserId, command.executorName, command.taskName);
        saveTaskTargets(task.getId(), command.targetSnapshots);
        syncPlanStatusByTask(task.getPlanId(), task.getTaskStatus(), WORKFLOW_STATUS_RUNNING);
        return task.getId();
    }

    /**
     * 更新计划自动任务快照（保留任务编号，刷新计划信息与对象列表）。
     */
    private void updatePlanTaskInternal(IotInspectionTaskDO existedTask, TaskCreateCommand command) {
        IotInspectionTaskDO updateObj = new IotInspectionTaskDO();
        updateObj.setId(existedTask.getId());
        updateObj.setTaskName(command.taskName);
        updateObj.setStationId(command.stationId);
        updateObj.setInspectionType(command.inspectionType);
        updateObj.setObjectType(command.objectType);
        updateObj.setStandardId(command.standardId);
        updateObj.setStandardName(command.standardName);
        updateObj.setLineId(command.lineId);
        updateObj.setLineName(command.lineName);
        updateObj.setPlanId(command.planId);
        updateObj.setSourceType(command.sourceType);
        updateObj.setPlanStartTime(command.planStartTime);
        updateObj.setPlanEndTime(command.planEndTime);
        updateObj.setExecutorUserId(command.executorUserId);
        updateObj.setExecutorName(command.executorName);
        updateObj.setTaskStatus(calculateTaskStatus(command.planStartTime, command.planEndTime, null));
        updateObj.setAbnormalCount(0);
        updateObj.setItemTotalCount(0);
        updateObj.setItemFinishedCount(0);
        updateObj.setSubmitTime(null);
        updateObj.setWorkflowStatus(WORKFLOW_STATUS_NOT_STARTED);
        updateObj.setProcessDefinitionKey("");
        updateObj.setProcessInstanceId("");
        updateObj.setProcessStartTime(null);
        updateObj.setProcessEndTime(null);
        updateObj.setTaskDesc(command.taskDesc);
        updateObj.setRemark(command.remark);
        taskMapper.updateById(updateObj);

        taskTargetMapper.deleteByTaskId(existedTask.getId());
        saveTaskTargets(existedTask.getId(), command.targetSnapshots);
        startTaskWorkflow(existedTask.getId(), command.executorUserId, command.executorName, command.taskName);
        syncPlanStatusByTask(existedTask.getPlanId(), updateObj.getTaskStatus(), WORKFLOW_STATUS_RUNNING);
    }

    /**
     * 保存任务对象明细。
     */
    private void saveTaskTargets(Long taskId, List<TargetSnapshot> targetSnapshots) {
        if (targetSnapshots == null || targetSnapshots.isEmpty()) {
            return;
        }
        for (TargetSnapshot targetSnapshot : targetSnapshots) {
            IotInspectionTaskTargetDO targetDO = new IotInspectionTaskTargetDO();
            targetDO.setTaskId(taskId);
            targetDO.setTargetSort(targetSnapshot.targetSort);
            targetDO.setTargetType(targetSnapshot.targetType);
            targetDO.setDeviceId(targetSnapshot.deviceId);
            targetDO.setLocationId(targetSnapshot.locationId);
            targetDO.setTargetName(targetSnapshot.targetName);
            targetDO.setStationId(StrUtil.blankToDefault(targetSnapshot.stationId, ""));
            taskTargetMapper.insert(targetDO);
        }
    }

    /**
     * 初始化巡检任务执行状态，不再依赖 BPM 工作流。
     */
    private void startTaskWorkflow(Long taskId, Long executorUserId, String executorName, String taskName) {
        IotInspectionTaskDO updateObj = new IotInspectionTaskDO();
        updateObj.setId(taskId);
        updateObj.setProcessInstanceId("");
        updateObj.setProcessDefinitionKey("");
        updateObj.setWorkflowStatus(WORKFLOW_STATUS_RUNNING);
        updateObj.setProcessStartTime(LocalDateTime.now());
        updateObj.setProcessEndTime(null);
        taskMapper.updateById(updateObj);
    }

    /**
     * 生成任务编号：XJ-YYYYMMDD-01..02..03（按租户+日期全局递增）。
     */
    private String generateTaskNo(String stationId) {
        // 保留入参以兼容现有调用链，编号序号改为按租户+日期全局递增，避免多站点同日冲突。
        trimToEmpty(stationId);
        LocalDate counterDate = LocalDate.now();
        Long tenantId = TenantContextHolder.getTenantId() == null ? 0L : TenantContextHolder.getTenantId();
        String dateSegment = counterDate.format(TASK_NO_DATE_FORMATTER);

        // 与历史数据对齐：若计数器缺失或落后，至少从当日已有最大序号 + 1 开始。
        Integer maxExistingSeq = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(CAST(SPLIT_PART(task_no, '-', 3) AS INTEGER)), 0) " +
                        "FROM yz_equipment_inspection_task " +
                        "WHERE tenant_id = ? AND deleted = 0 " +
                        "AND task_no LIKE ? " +
                        "AND task_no ~ ?",
                Integer.class,
                tenantId,
                "XJ-" + dateSegment + "-%",
                "^XJ-" + dateSegment + "-[0-9]+$");
        int baseSequence = (maxExistingSeq == null || maxExistingSeq < 0 ? 0 : maxExistingSeq) + 1;

        Integer seq = jdbcTemplate.queryForObject(
                "INSERT INTO yz_equipment_inspection_task_no_counter " +
                        "(id, counter_date, current_seq, station_id, creator, create_time, updater, update_time, deleted, tenant_id) " +
                        "VALUES (?, ?, ?, '', '', CURRENT_TIMESTAMP, '', CURRENT_TIMESTAMP, 0, ?) " +
                        "ON CONFLICT (tenant_id, station_id, counter_date, deleted) " +
                        "DO UPDATE SET current_seq = GREATEST(yz_equipment_inspection_task_no_counter.current_seq + 1, EXCLUDED.current_seq), " +
                        "update_time = CURRENT_TIMESTAMP " +
                        "RETURNING current_seq",
                Integer.class,
                IdWorker.getId(),
                Date.valueOf(counterDate),
                baseSequence,
                tenantId);
        int sequence = seq == null || seq <= 0 ? baseSequence : seq;
        return String.format(Locale.ROOT, "XJ-%s-%02d", dateSegment, sequence);
    }

    /**
     * 构建计划自动生成任务名称。
     */
    private String buildPlanTaskName(String planName) {
        String normalizedPlanName = trimToEmpty(planName);
        if (StrUtil.isBlank(normalizedPlanName)) {
            normalizedPlanName = "巡检计划任务";
        }
        return normalizedPlanName;
    }

    /**
     * 校验时间范围。
     */
    private void validatePlanTimeRange(LocalDateTime planStartTime, LocalDateTime planEndTime) {
        if (planStartTime == null || planEndTime == null || planEndTime.isBefore(planStartTime)) {
            throw exception(INSPECTION_TASK_DATE_RANGE_INVALID);
        }
    }

    /**
     * 校验巡检对象类型。
     */
    private Integer normalizeObjectType(Integer objectType) {
        if (Objects.equals(objectType, OBJECT_TYPE_DEVICE) || Objects.equals(objectType, OBJECT_TYPE_LOCATION)) {
            return objectType;
        }
        throw exception(INSPECTION_TASK_OBJECT_TYPE_INVALID);
    }

    /**
     * 规范化对象列表并去重。
     */
    private List<IotInspectionTaskTargetSaveReqVO> normalizeTargets(List<IotInspectionTaskTargetSaveReqVO> targets) {
        if (targets == null || targets.isEmpty()) {
            throw exception(INSPECTION_TASK_TARGET_EMPTY);
        }
        Set<Long> exists = new LinkedHashSet<>();
        List<IotInspectionTaskTargetSaveReqVO> result = new ArrayList<>();
        for (IotInspectionTaskTargetSaveReqVO target : targets) {
            if (target == null || target.getTargetId() == null) {
                continue;
            }
            if (!exists.add(target.getTargetId())) {
                continue;
            }
            IotInspectionTaskTargetSaveReqVO normalized = new IotInspectionTaskTargetSaveReqVO();
            normalized.setTargetId(target.getTargetId());
            normalized.setTargetName(trimToNull(target.getTargetName()));
            normalized.setTargetSort(target.getTargetSort());
            result.add(normalized);
        }
        if (result.isEmpty()) {
            throw exception(INSPECTION_TASK_TARGET_EMPTY);
        }
        for (int i = 0; i < result.size(); i++) {
            IotInspectionTaskTargetSaveReqVO item = result.get(i);
            if (item.getTargetSort() == null || item.getTargetSort() <= 0) {
                item.setTargetSort(i + 1);
            }
        }
        return result;
    }

    /**
     * 查询对象元数据。
     */
    private Map<Long, TargetMeta> resolveTargetMetaMap(Integer objectType, List<IotInspectionTaskTargetSaveReqVO> targets) {
        List<Long> targetIds = targets.stream().map(IotInspectionTaskTargetSaveReqVO::getTargetId).toList();
        Map<Long, TargetMeta> targetMetaMap;
        if (Objects.equals(objectType, OBJECT_TYPE_DEVICE)) {
            targetMetaMap = queryDeviceMetaMap(targetIds);
        } else if (Objects.equals(objectType, OBJECT_TYPE_LOCATION)) {
            targetMetaMap = queryLocationMetaMap(targetIds);
        } else {
            throw exception(INSPECTION_TASK_OBJECT_TYPE_INVALID);
        }
        for (Long targetId : targetIds) {
            if (!targetMetaMap.containsKey(targetId)) {
                throw exception(INSPECTION_TASK_TARGET_NOT_EXISTS);
            }
        }
        return targetMetaMap;
    }

    /**
     * 构建人工创建对象快照。
     */
    private List<TargetSnapshot> buildTargetSnapshots(Integer objectType,
                                                      List<IotInspectionTaskTargetSaveReqVO> targets,
                                                      Map<Long, TargetMeta> targetMetaMap) {
        List<TargetSnapshot> snapshots = new ArrayList<>(targets.size());
        for (IotInspectionTaskTargetSaveReqVO target : targets) {
            TargetMeta targetMeta = targetMetaMap.get(target.getTargetId());
            if (targetMeta == null) {
                throw exception(INSPECTION_TASK_TARGET_NOT_EXISTS);
            }
            TargetSnapshot snapshot = new TargetSnapshot();
            snapshot.targetType = objectType;
            snapshot.targetSort = target.getTargetSort();
            snapshot.targetName = targetMeta.targetName;
            snapshot.stationId = targetMeta.stationId;
            if (Objects.equals(objectType, OBJECT_TYPE_DEVICE)) {
                snapshot.deviceId = target.getTargetId();
                snapshot.locationId = null;
            } else {
                snapshot.locationId = target.getTargetId();
                snapshot.deviceId = null;
            }
            snapshots.add(snapshot);
        }
        return snapshots;
    }

    /**
     * 构建计划创建对象快照。
     */
    private List<TargetSnapshot> buildTargetSnapshotsFromPlan(Integer objectType, List<IotInspectionPlanTargetDO> planTargets) {
        List<TargetSnapshot> snapshots = new ArrayList<>(planTargets.size());
        for (IotInspectionPlanTargetDO planTarget : planTargets) {
            TargetSnapshot snapshot = new TargetSnapshot();
            snapshot.targetType = objectType;
            snapshot.targetSort = planTarget.getTargetSort();
            snapshot.targetName = StrUtil.blankToDefault(planTarget.getTargetName(),
                    (Objects.equals(objectType, OBJECT_TYPE_DEVICE) ? "设备-" : "区域-")
                            + (Objects.equals(objectType, OBJECT_TYPE_DEVICE) ? planTarget.getDeviceId() : planTarget.getLocationId()));
            snapshot.stationId = trimToEmpty(planTarget.getStationId());
            snapshot.deviceId = planTarget.getDeviceId();
            snapshot.locationId = planTarget.getLocationId();
            snapshots.add(snapshot);
        }
        return snapshots;
    }

    /**
     * 查询设备元数据。
     */
    private Map<Long, TargetMeta> queryDeviceMetaMap(List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = String.join(",", Collections.nCopies(targetIds.size(), "?"));
        String sql = "SELECT id, station_id, COALESCE(NULLIF(nickname, ''), NULLIF(device_name, ''), CONCAT('设备-', id)) AS name " +
                "FROM iot_device WHERE deleted = 0 AND id IN (" + placeholders + ")";
        List<TargetMeta> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new TargetMeta(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("station_id")), targetIds.toArray());
        Map<Long, TargetMeta> result = new HashMap<>();
        for (TargetMeta row : rows) {
            result.put(row.targetId, row);
        }
        return result;
    }

    /**
     * 查询区域元数据。
     */
    private Map<Long, TargetMeta> queryLocationMetaMap(List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = String.join(",", Collections.nCopies(targetIds.size(), "?"));
        String sql = "SELECT id, COALESCE(NULLIF(name, ''), CONCAT('区域-', id)) AS name " +
                "FROM iot_device_location WHERE deleted = 0 AND id IN (" + placeholders + ")";
        List<TargetMeta> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new TargetMeta(
                rs.getLong("id"),
                rs.getString("name"),
                ""), targetIds.toArray());
        Map<Long, TargetMeta> result = new HashMap<>();
        for (TargetMeta row : rows) {
            result.put(row.targetId, row);
        }
        return result;
    }

    /**
     * 解析标准快照。
     */
    private StandardMeta resolveStandardMeta(Long standardId, boolean required) {
        if (standardId == null) {
            if (required) {
                throw exception(INSPECTION_TASK_STANDARD_NOT_EXISTS);
            }
            return new StandardMeta(null, "");
        }
        IotInspectionStandardDO standard = standardMapper.selectById(standardId);
        if (standard == null) {
            throw exception(INSPECTION_TASK_STANDARD_NOT_EXISTS);
        }
        return new StandardMeta(standardId, StrUtil.blankToDefault(standard.getStandardName(), "标准-" + standardId));
    }

    /**
     * 解析线路快照。
     */
    private LineMeta resolveLineMeta(Long lineId) {
        if (lineId == null) {
            return null;
        }
        IotInspectionLineDO line = lineMapper.selectById(lineId);
        if (line == null) {
            throw exception(INSPECTION_TASK_LINE_NOT_EXISTS);
        }
        return new LineMeta(lineId, StrUtil.blankToDefault(line.getLineName(), "线路-" + lineId));
    }

    /**
     * 回填任务所属闸站。
     */
    private String resolveTaskStationId(String reqStationId,
                                        Integer objectType,
                                        List<IotInspectionTaskTargetSaveReqVO> targets,
                                        Map<Long, TargetMeta> targetMetaMap) {
        String normalizedStationId = trimToNull(reqStationId);
        if (normalizedStationId != null) {
            return normalizedStationId;
        }
        if (!Objects.equals(objectType, OBJECT_TYPE_DEVICE)) {
            return "";
        }
        for (IotInspectionTaskTargetSaveReqVO target : targets) {
            TargetMeta targetMeta = targetMetaMap.get(target.getTargetId());
            if (targetMeta != null && StrUtil.isNotBlank(targetMeta.stationId)) {
                return targetMeta.stationId;
            }
        }
        return "";
    }

    /**
     * 回填计划自动任务所属闸站。
     */
    private String resolveTaskStationIdFromPlan(String planStationId, Integer objectType, List<IotInspectionPlanTargetDO> planTargets) {
        String normalizedStationId = trimToNull(planStationId);
        if (normalizedStationId != null) {
            return normalizedStationId;
        }
        if (!Objects.equals(objectType, OBJECT_TYPE_DEVICE)) {
            return "";
        }
        for (IotInspectionPlanTargetDO target : planTargets) {
            if (StrUtil.isNotBlank(target.getStationId())) {
                return target.getStationId().trim();
            }
        }
        return "";
    }

    /**
     * 计算任务状态。
     */
    private Integer calculateTaskStatus(LocalDateTime planStartTime, LocalDateTime planEndTime, LocalDateTime submitTime) {
        if (submitTime != null) {
            return TASK_STATUS_FINISHED;
        }
        LocalDateTime now = LocalDateTime.now();
        if (planStartTime != null && now.isBefore(planStartTime)) {
            return TASK_STATUS_NOT_STARTED;
        }
        if (planEndTime != null && now.isAfter(planEndTime)) {
            return TASK_STATUS_OVERDUE;
        }
        return TASK_STATUS_UNFINISHED;
    }

    private String resolveExecutorName(Long executorUserId, String executorName) {
        if (StrUtil.isNotBlank(executorName)) {
            return executorName.trim();
        }
        return executorUserId == null ? "" : String.valueOf(executorUserId);
    }

    /**
     * 校验异常项备注：当检查结果为 qualified/unqualified 时，检查备注必须填写。
     */
    private void validateAbnormalCheckRemark(List<IotInspectionTaskSubmitResultItemVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (IotInspectionTaskSubmitResultItemVO item : items) {
            if (item == null) {
                continue;
            }
            if (isAbnormalCheckResult(item.getCheckResult()) && StrUtil.isBlank(item.getCheckRemark())) {
                throw exception(INSPECTION_TASK_SUBMIT_ABNORMAL_REMARK_REQUIRED);
            }
        }
    }

    /**
     * 按提交异常项自动生成故障记录（可选能力）。
     */
    private void createFaultRepairsBySubmitResult(IotInspectionTaskDO task,
                                                  List<IotInspectionTaskSubmitResultItemVO> items,
                                                  Long loginUserId,
                                                  LocalDateTime submitTime) {
        if (items == null || items.isEmpty()) {
            return;
        }
        String stationId = trimToEmpty(task.getStationId());
        String reporterName = StrUtil.blankToDefault(SecurityFrameworkUtils.getLoginUserNickname(),
                StrUtil.blankToDefault(task.getExecutorName(), String.valueOf(loginUserId)));
        for (IotInspectionTaskSubmitResultItemVO item : items) {
            if (item == null || !isAbnormalCheckResult(item.getCheckResult())) {
                continue;
            }
            IotDeviceDO device = resolveDeviceForFault(stationId, item);
            if (device == null) {
                // 提交提醒仅对设备对象生成故障；非设备对象或无法映射设备时跳过，不阻塞任务提交
                continue;
            }
            String faultSymptom = trimToEmpty(item.getCheckRemark());
            if (StrUtil.isBlank(faultSymptom)) {
                throw exception(INSPECTION_TASK_SUBMIT_ABNORMAL_REMARK_REQUIRED);
            }
            String deviceType = device.getDeviceType() == null ? "" : String.valueOf(device.getDeviceType());
            if (StrUtil.isBlank(deviceType)) {
                throw exception(INSPECTION_TASK_SUBMIT_FAULT_DEVICE_NOT_EXISTS);
            }

            IotFaultRepairSaveReqVO saveReqVO = new IotFaultRepairSaveReqVO();
            saveReqVO.setDeviceId(device.getId());
            saveReqVO.setDeviceName(StrUtil.blankToDefault(device.getNickname(), device.getDeviceName()));
            saveReqVO.setDeviceType(deviceType);
            // 故障类型默认“其他”，对应字典 iot_deivce_fault_type 的 value=2
            saveReqVO.setFaultType("2");
            saveReqVO.setFaultTime(submitTime);
            saveReqVO.setFaultSymptom(faultSymptom);
            saveReqVO.setReporterUserId(loginUserId);
            saveReqVO.setReporterName(reporterName);
            saveReqVO.setFaultImages((item.getAttachments() == null || item.getAttachments().isEmpty())
                    ? Collections.emptyList()
                    : item.getAttachments().stream()
                    .filter(StrUtil::isNotBlank)
                    .map(String::trim)
                    .collect(Collectors.toList()));
            saveReqVO.setRemark(StrUtil.format("巡检任务自动生成：{} / {}", trimToEmpty(task.getTaskNo()), trimToEmpty(item.getItemName())));
            faultRepairService.createFaultRepair(saveReqVO);
        }
    }

    /**
     * 解析异常项对应设备：优先 targetId，其次按站点+设备名称匹配。
     */
    private IotDeviceDO resolveDeviceForFault(String stationId, IotInspectionTaskSubmitResultItemVO item) {
        if (item.getTargetId() != null) {
            IotDeviceDO byId = deviceMapper.selectById(item.getTargetId());
            if (byId != null) {
                String stationIdNormalized = trimToNull(stationId);
                if (stationIdNormalized == null
                        || Objects.equals(stationIdNormalized, trimToNull(byId.getStationId()))) {
                    return byId;
                }
            }
        }
        String targetName = trimToNull(item.getTargetName());
        if (targetName == null) {
            return null;
        }
        String stationIdNormalized = trimToNull(stationId);
        List<IotDeviceDO> matched = deviceMapper.selectList(new LambdaQueryWrapperX<IotDeviceDO>()
                .eqIfPresent(IotDeviceDO::getStationId, stationIdNormalized)
                .and(wrapper -> wrapper.eq(IotDeviceDO::getDeviceName, targetName)
                        .or()
                        .eq(IotDeviceDO::getNickname, targetName))
                .orderByDesc(IotDeviceDO::getId)
                .last("LIMIT 1"));
        return matched.isEmpty() ? null : matched.get(0);
    }

    private boolean isAbnormalCheckResult(String checkResult) {
        String normalized = trimToEmpty(checkResult).toLowerCase(Locale.ROOT);
        return Objects.equals(normalized, "qualified") || Objects.equals(normalized, "unqualified");
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
     * 规范化提交结果明细，清理空白并过滤空对象，保证 JSON 可稳定落库。
     */
    private List<IotInspectionTaskSubmitResultItemVO> normalizeSubmitResultItems(List<IotInspectionTaskSubmitResultItemVO> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<IotInspectionTaskSubmitResultItemVO> normalizedItems = new ArrayList<>();
        for (IotInspectionTaskSubmitResultItemVO item : items) {
            if (item == null) {
                continue;
            }
            IotInspectionTaskSubmitResultItemVO normalizedItem = new IotInspectionTaskSubmitResultItemVO();
            normalizedItem.setItemId(item.getItemId());
            normalizedItem.setItemName(trimToEmpty(item.getItemName()));
            normalizedItem.setTargetId(item.getTargetId());
            normalizedItem.setTargetName(trimToEmpty(item.getTargetName()));
            normalizedItem.setCheckResult(trimToEmpty(item.getCheckResult()));
            normalizedItem.setCheckRemark(trimToEmpty(item.getCheckRemark()));
            normalizedItem.setAttachments((item.getAttachments() == null || item.getAttachments().isEmpty())
                    ? Collections.emptyList()
                    : item.getAttachments().stream()
                    .filter(StrUtil::isNotBlank)
                    .map(String::trim)
                    .collect(Collectors.toList()));
            normalizedItem.setRecords(normalizeSubmitResultRecords(item.getRecords()));
            normalizedItems.add(normalizedItem);
        }
        return normalizedItems;
    }

    /**
     * 规范化提交记录项明细，保留关键字段并清理空白。
     */
    private List<IotInspectionTaskSubmitResultRecordVO> normalizeSubmitResultRecords(List<IotInspectionTaskSubmitResultRecordVO> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<IotInspectionTaskSubmitResultRecordVO> normalizedRecords = new ArrayList<>();
        for (IotInspectionTaskSubmitResultRecordVO record : records) {
            if (record == null) {
                continue;
            }
            IotInspectionTaskSubmitResultRecordVO normalizedRecord = new IotInspectionTaskSubmitResultRecordVO();
            normalizedRecord.setAttrName(trimToEmpty(record.getAttrName()));
            normalizedRecord.setAttrUnit(trimToEmpty(record.getAttrUnit()));
            normalizedRecord.setStandardValue(trimToEmpty(record.getStandardValue()));
            normalizedRecord.setActualValue(trimToEmpty(record.getActualValue()));
            normalizedRecords.add(normalizedRecord);
        }
        return normalizedRecords;
    }

    /**
     * 逻辑删除前改编号，规避唯一索引 (tenant_id, task_no, deleted) 在 deleted=1 场景下冲突。
     */
    private void renameTaskNoBeforeDelete(IotInspectionTaskDO task) {
        if (task == null || task.getId() == null) {
            return;
        }
        IotInspectionTaskDO updateObj = new IotInspectionTaskDO();
        updateObj.setId(task.getId());
        updateObj.setTaskNo("DEL-" + task.getId());
        taskMapper.updateById(updateObj);
    }

    /**
     * 任务创建命令。
     */
    private static final class TaskCreateCommand {
        private String taskName;
        private String stationId;
        private String inspectionType;
        private Integer objectType;
        private Long standardId;
        private String standardName;
        private Long lineId;
        private String lineName;
        private Integer sourceType;
        private Long planId;
        private LocalDateTime planStartTime;
        private LocalDateTime planEndTime;
        private Long executorUserId;
        private String executorName;
        private String taskDesc;
        private String remark;
        private List<TargetSnapshot> targetSnapshots;
    }

    /**
     * 巡检对象元数据。
     */
    private static final class TargetMeta {
        private final Long targetId;
        private final String targetName;
        private final String stationId;

        private TargetMeta(Long targetId, String targetName, String stationId) {
            this.targetId = targetId;
            this.targetName = targetName;
            this.stationId = stationId;
        }
    }

    /**
     * 任务对象快照。
     */
    private static final class TargetSnapshot {
        private Integer targetSort;
        private Integer targetType;
        private Long deviceId;
        private Long locationId;
        private String targetName;
        private String stationId;
    }

    /**
     * 标准快照。
     */
    private static final class StandardMeta {
        private final Long standardId;
        private final String standardName;

        private StandardMeta(Long standardId, String standardName) {
            this.standardId = standardId;
            this.standardName = standardName;
        }
    }

    /**
     * 线路快照。
     */
    private static final class LineMeta {
        private final Long lineId;
        private final String lineName;

        private LineMeta(Long lineId, String lineName) {
            this.lineId = lineId;
            this.lineName = lineName;
        }
    }

}
