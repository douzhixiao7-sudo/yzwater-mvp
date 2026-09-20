package com.sydigit.yzwater.module.iot.service.runlog;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sydigit.yzwater.framework.security.core.service.SecurityFrameworkService;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogDefaultRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogDispatchOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogInspectionResultItemVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogInspectionResultRecordVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.runlog.vo.IotRunLogSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.dispatchmanage.IotDispatchInstructionDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardTargetDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.runlog.IotRunLogDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.shiftteam.IotShiftTeamDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.dispatchmanage.IotDispatchInstructionMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard.IotInspectionStandardMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard.IotInspectionStandardTargetMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.runlog.IotRunLogMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.shiftteam.IotShiftTeamMapper;
import com.sydigit.yzwater.module.iot.service.faultrepair.IotFaultRepairService;
import com.sydigit.yzwater.module.system.api.dept.DeptApi;
import com.sydigit.yzwater.module.system.api.dept.dto.DeptRespDTO;
import com.sydigit.yzwater.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_STANDARD_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.RUN_LOG_DISPATCH_INSTRUCTION_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.RUN_LOG_FORBIDDEN;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.RUN_LOG_ABNORMAL_REMARK_REQUIRED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.RUN_LOG_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.RUN_LOG_PERIOD_INVALID;

/**
 * 运行日志 Service 实现
 */
@Service
@Validated
public class IotRunLogServiceImpl implements IotRunLogService {

    private static final String STANDARD_DEVICE_TARGET_TYPE = "device";
    private static final String DEFAULT_FAULT_TYPE_OTHER = "2";

    @Resource
    private IotRunLogMapper runLogMapper;
    @Resource
    private IotDispatchInstructionMapper dispatchInstructionMapper;
    @Resource
    private IotShiftTeamMapper shiftTeamMapper;
    @Resource
    private IotInspectionStandardMapper inspectionStandardMapper;
    @Resource
    private IotInspectionStandardTargetMapper inspectionStandardTargetMapper;
    @Resource
    private IotDeviceMapper deviceMapper;
    @Resource
    private IotFaultRepairService faultRepairService;
    @Resource
    private DeptApi deptApi;
    @Resource
    private SecurityFrameworkService securityFrameworkService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRunLog(IotRunLogSaveReqVO createReqVO) {
        validateRunPeriod(createReqVO.getRunStartTime(), createReqVO.getRunEndTime());

        DutyTeamSnapshot dutyTeamSnapshot = resolveDutyTeamSnapshot(createReqVO.getDutyTeamId(), createReqVO.getDutyTeamName());
        DispatchInstructionSnapshot instructionSnapshot = resolveDispatchInstructionSnapshot(createReqVO.getDispatchInstructionId());
        InspectionStandardSnapshot standardSnapshot = resolveInspectionStandardSnapshot(
                createReqVO.getInspectionStandardId(), createReqVO.getStationId(), createReqVO.getInspectionType());
        LocalDateTime recordTime = createReqVO.getRecordTime() == null ? LocalDateTime.now() : createReqVO.getRecordTime();
        String recorderUserName = trimToNull(createReqVO.getRecorderUserName()) != null
                ? createReqVO.getRecorderUserName().trim()
                : resolveLoginUserName();
        List<IotRunLogInspectionResultItemVO> normalizedItems = normalizeInspectionResultItems(createReqVO.getInspectionResultItems());
        validateAbnormalCheckRemark(normalizedItems);

        IotRunLogDO log = new IotRunLogDO();
        log.setLogNo(generateLogNo());
        log.setLogTitle(trimToEmpty(createReqVO.getTaskName()));
        log.setTeamId(dutyTeamSnapshot.teamId);
        log.setTeamName(dutyTeamSnapshot.teamName);
        log.setRecorderUserId(SecurityFrameworkUtils.getLoginUserId());
        log.setRecorderUserName(recorderUserName);
        log.setRecordTime(recordTime);
        log.setRunStartTime(createReqVO.getRunStartTime());
        log.setRunEndTime(createReqVO.getRunEndTime());
        log.setCheckPeriod(trimToEmpty(createReqVO.getCheckPeriod()));
        log.setStationId(trimToEmpty(createReqVO.getStationId()));
        log.setInspectionType(standardSnapshot.inspectionType);
        log.setInspectionStandardId(standardSnapshot.standardId);
        log.setInspectionStandardName(standardSnapshot.standardName);
        log.setInspectionResultItemsJson(toInspectionResultItemsJson(normalizedItems));
        // 设备、运行参数、事件描述当前不再维护，仅保留兼容字段
        log.setDeviceName("");
        log.setRunParamsText("");
        log.setEventDesc("");
        log.setAttachmentFileIds(normalizeAttachments(createReqVO.getAttachments()));
        log.setDispatchInstructionId(instructionSnapshot.id);
        log.setDispatchInstructionNo(instructionSnapshot.instructionNo);
        log.setDispatchInstructionName(instructionSnapshot.instructionName);
        log.setRemark(trimToEmpty(createReqVO.getRemark()));
        runLogMapper.insert(log);
        if (Boolean.TRUE.equals(createReqVO.getAutoCreateFaultRecords())) {
            createFaultRepairsByRunLog(log, normalizedItems, recordTime);
        }
        return log.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRunLog(IotRunLogSaveReqVO updateReqVO) {
        IotRunLogDO existed = validateRunLogExists(updateReqVO.getId());
        validateOperatePermission(existed);
        validateRunPeriod(updateReqVO.getRunStartTime(), updateReqVO.getRunEndTime());

        DutyTeamSnapshot dutyTeamSnapshot = resolveDutyTeamSnapshot(updateReqVO.getDutyTeamId(), updateReqVO.getDutyTeamName());
        DispatchInstructionSnapshot instructionSnapshot = resolveDispatchInstructionSnapshot(updateReqVO.getDispatchInstructionId());
        InspectionStandardSnapshot standardSnapshot = resolveInspectionStandardSnapshot(
                updateReqVO.getInspectionStandardId(), updateReqVO.getStationId(), updateReqVO.getInspectionType());
        List<IotRunLogInspectionResultItemVO> normalizedItems = normalizeInspectionResultItems(updateReqVO.getInspectionResultItems());
        validateAbnormalCheckRemark(normalizedItems);

        IotRunLogDO updateObj = new IotRunLogDO();
        updateObj.setId(existed.getId());
        updateObj.setLogTitle(trimToEmpty(updateReqVO.getTaskName()));
        updateObj.setTeamId(dutyTeamSnapshot.teamId);
        updateObj.setTeamName(dutyTeamSnapshot.teamName);
        if (updateReqVO.getRecordTime() != null) {
            updateObj.setRecordTime(updateReqVO.getRecordTime());
        }
        if (trimToNull(updateReqVO.getRecorderUserName()) != null) {
            updateObj.setRecorderUserName(updateReqVO.getRecorderUserName().trim());
        }
        updateObj.setRunStartTime(updateReqVO.getRunStartTime());
        updateObj.setRunEndTime(updateReqVO.getRunEndTime());
        updateObj.setCheckPeriod(trimToEmpty(updateReqVO.getCheckPeriod()));
        updateObj.setStationId(trimToEmpty(updateReqVO.getStationId()));
        updateObj.setInspectionType(standardSnapshot.inspectionType);
        updateObj.setInspectionStandardId(standardSnapshot.standardId);
        updateObj.setInspectionStandardName(standardSnapshot.standardName);
        updateObj.setInspectionResultItemsJson(toInspectionResultItemsJson(normalizedItems));
        // 兼容历史字段：编辑时不再维护 deviceName/runParamsText/eventDesc，避免误清旧数据
        updateObj.setAttachmentFileIds(normalizeAttachments(updateReqVO.getAttachments()));
        updateObj.setDispatchInstructionId(instructionSnapshot.id);
        updateObj.setDispatchInstructionNo(instructionSnapshot.instructionNo);
        updateObj.setDispatchInstructionName(instructionSnapshot.instructionName);
        updateObj.setRemark(trimToEmpty(updateReqVO.getRemark()));
        runLogMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRunLog(Long id) {
        IotRunLogDO existed = validateRunLogExists(id);
        validateOperatePermission(existed);
        runLogMapper.deleteById(id);
    }

    @Override
    public IotRunLogRespVO getRunLog(Long id) {
        IotRunLogDO log = validateRunLogExists(id);
        return buildResp(log);
    }

    @Override
    public PageResult<IotRunLogRespVO> getRunLogPage(IotRunLogPageReqVO pageReqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        Long loginDeptId = SecurityFrameworkUtils.getLoginUserDeptId();
        boolean hasAdminRole = hasAdminRole();

        PageResult<IotRunLogDO> pageResult = runLogMapper.selectPage(pageReqVO, hasAdminRole, loginUserId, loginDeptId);
        List<IotRunLogRespVO> respList = CollectionUtils.convertList(pageResult.getList(), this::buildResp);
        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public List<IotRunLogRespVO> getRunLogList(IotRunLogPageReqVO reqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        Long loginDeptId = SecurityFrameworkUtils.getLoginUserDeptId();
        boolean hasAdminRole = hasAdminRole();

        List<IotRunLogDO> list = runLogMapper.selectListByReqVO(reqVO, hasAdminRole, loginUserId, loginDeptId);
        return CollectionUtils.convertList(list, this::buildResp);
    }

    @Override
    public IotRunLogDefaultRespVO getRunLogDefaults() {
        IotRunLogDefaultRespVO respVO = new IotRunLogDefaultRespVO();
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        Long loginDeptId = SecurityFrameworkUtils.getLoginUserDeptId();
        DeptRespDTO dept = loginDeptId == null ? null : deptApi.getDept(loginDeptId);

        respVO.setRecorderUserId(loginUserId);
        respVO.setRecorderUserName(resolveLoginUserName());
        respVO.setDutyTeamId(dept == null ? null : dept.getId());
        respVO.setDutyTeamName(dept == null ? "" : trimToEmpty(dept.getName()));
        respVO.setRecordTime(LocalDateTime.now());
        return respVO;
    }

    @Override
    public List<IotRunLogDispatchOptionRespVO> getDispatchOptions(String keyword, String stationId) {
        LambdaQueryWrapperX<IotDispatchInstructionDO> queryWrapper = new LambdaQueryWrapperX<IotDispatchInstructionDO>()
                .eqIfPresent(IotDispatchInstructionDO::getStationId, trimToNull(stationId))
                .orderByDesc(IotDispatchInstructionDO::getCreateTime)
                .orderByDesc(IotDispatchInstructionDO::getId)
                .last("LIMIT 30");
        if (StrUtil.isNotBlank(keyword)) {
            String normalized = keyword.trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(IotDispatchInstructionDO::getInstructionNo, normalized)
                    .or()
                    .like(IotDispatchInstructionDO::getInstructionName, normalized));
        }

        List<IotDispatchInstructionDO> instructions = dispatchInstructionMapper.selectList(queryWrapper);
        return CollectionUtils.convertList(instructions, item -> {
            IotRunLogDispatchOptionRespVO option = new IotRunLogDispatchOptionRespVO();
            option.setId(item.getId());
            option.setInstructionNo(trimToEmpty(item.getInstructionNo()));
            option.setInstructionName(trimToEmpty(item.getInstructionName()));
            String instructionNo = trimToNull(item.getInstructionNo());
            String instructionName = trimToNull(item.getInstructionName());
            if (instructionNo == null && instructionName == null) {
                option.setTitle("调令#" + item.getId());
            } else if (instructionNo == null) {
                option.setTitle(instructionName);
            } else if (instructionName == null) {
                option.setTitle(instructionNo);
            } else {
                option.setTitle(instructionNo + " / " + instructionName);
            }
            return option;
        });
    }

    /**
     * 校验运行日志存在
     */
    private IotRunLogDO validateRunLogExists(Long id) {
        IotRunLogDO log = runLogMapper.selectById(id);
        if (log == null) {
            throw exception(RUN_LOG_NOT_EXISTS);
        }
        return log;
    }

    /**
     * 校验运行时段
     */
    private void validateRunPeriod(LocalDateTime runStartTime, LocalDateTime runEndTime) {
        if (runStartTime == null || runEndTime == null || runEndTime.isBefore(runStartTime)) {
            throw exception(RUN_LOG_PERIOD_INVALID);
        }
    }

    /**
     * 校验操作权限：值班人员可操作本人或本班组记录
     */
    private void validateOperatePermission(IotRunLogDO log) {
        if (hasAdminRole()) {
            return;
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        Long loginDeptId = SecurityFrameworkUtils.getLoginUserDeptId();
        boolean allow = Objects.equals(loginUserId, log.getRecorderUserId())
                || (loginDeptId != null && Objects.equals(loginDeptId, log.getTeamId()));
        if (!allow) {
            throw exception(RUN_LOG_FORBIDDEN);
        }
    }

    /**
     * 解析值班班组快照
     */
    private DutyTeamSnapshot resolveDutyTeamSnapshot(Long dutyTeamId, String dutyTeamName) {
        String manualTeamName = trimToNull(dutyTeamName);
        DutyTeamSnapshot snapshot = new DutyTeamSnapshot();
        if (dutyTeamId == null) {
            snapshot.teamName = manualTeamName == null ? "" : manualTeamName;
            return snapshot;
        }

        IotShiftTeamDO shiftTeam = shiftTeamMapper.selectById(dutyTeamId);
        if (shiftTeam != null) {
            snapshot.teamId = shiftTeam.getId();
            snapshot.teamName = manualTeamName == null ? trimToEmpty(shiftTeam.getTeamName()) : manualTeamName;
            return snapshot;
        }

        DeptRespDTO dept = deptApi.getDept(dutyTeamId);
        if (dept != null) {
            snapshot.teamId = dutyTeamId;
            snapshot.teamName = manualTeamName == null ? trimToEmpty(dept.getName()) : manualTeamName;
            return snapshot;
        }

        snapshot.teamId = dutyTeamId;
        snapshot.teamName = manualTeamName == null ? "" : manualTeamName;
        return snapshot;
    }

    /**
     * 解析关联调令快照
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

    /**
     * 解析巡检标准快照，并校验站点/类型约束
     */
    private InspectionStandardSnapshot resolveInspectionStandardSnapshot(Long standardId, String stationId, String inspectionType) {
        InspectionStandardSnapshot snapshot = new InspectionStandardSnapshot();
        snapshot.inspectionType = trimToEmpty(inspectionType);
        if (standardId == null) {
            return snapshot;
        }
        IotInspectionStandardDO standard = inspectionStandardMapper.selectById(standardId);
        if (standard == null || !Objects.equals(standard.getStatus(), 0)) {
            throw exception(INSPECTION_STANDARD_NOT_EXISTS);
        }
        snapshot.standardId = standard.getId();
        snapshot.standardName = trimToEmpty(standard.getStandardName());
        snapshot.inspectionType = trimToEmpty(standard.getInspectionType());

        String normalizedInspectionType = trimToNull(inspectionType);
        if (normalizedInspectionType != null && !Objects.equals(normalizedInspectionType, snapshot.inspectionType)) {
            throw exception(INSPECTION_STANDARD_NOT_EXISTS);
        }
        String normalizedStationId = trimToNull(stationId);
        if (normalizedStationId != null) {
            List<IotInspectionStandardTargetDO> targets = inspectionStandardTargetMapper.selectListByStandardId(standardId);
            boolean stationMatched = targets.stream().anyMatch(target ->
                    Objects.equals(normalizedStationId, trimToNull(target.getStationId())));
            if (!stationMatched) {
                throw exception(INSPECTION_STANDARD_NOT_EXISTS);
            }
        }
        return snapshot;
    }

    /**
     * 规范化后的巡检结果明细转 JSON
     */
    private String toInspectionResultItemsJson(List<IotRunLogInspectionResultItemVO> normalizedItems) {
        return (normalizedItems == null || normalizedItems.isEmpty()) ? null : JsonUtils.toJsonString(normalizedItems);
    }

    private List<IotRunLogInspectionResultItemVO> normalizeInspectionResultItems(List<IotRunLogInspectionResultItemVO> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<IotRunLogInspectionResultItemVO> normalizedItems = new java.util.ArrayList<>();
        for (IotRunLogInspectionResultItemVO item : items) {
            if (item == null) {
                continue;
            }
            IotRunLogInspectionResultItemVO normalizedItem = new IotRunLogInspectionResultItemVO();
            normalizedItem.setItemId(item.getItemId());
            normalizedItem.setItemName(trimToEmpty(item.getItemName()));
            normalizedItem.setTargetType(trimToEmpty(item.getTargetType()));
            normalizedItem.setTargetId(item.getTargetId());
            normalizedItem.setTargetName(trimToEmpty(item.getTargetName()));
            normalizedItem.setCheckResult(trimToEmpty(item.getCheckResult()));
            normalizedItem.setCheckRemark(trimToEmpty(item.getCheckRemark()));
            normalizedItem.setRecords(normalizeInspectionResultRecords(item.getRecords()));
            normalizedItems.add(normalizedItem);
        }
        return normalizedItems;
    }

    private List<IotRunLogInspectionResultRecordVO> normalizeInspectionResultRecords(List<IotRunLogInspectionResultRecordVO> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<IotRunLogInspectionResultRecordVO> normalizedRecords = new java.util.ArrayList<>();
        for (IotRunLogInspectionResultRecordVO record : records) {
            if (record == null) {
                continue;
            }
            IotRunLogInspectionResultRecordVO normalizedRecord = new IotRunLogInspectionResultRecordVO();
            normalizedRecord.setAttrName(trimToEmpty(record.getAttrName()));
            normalizedRecord.setAttrUnit(trimToEmpty(record.getAttrUnit()));
            normalizedRecord.setStandardValue(trimToEmpty(record.getStandardValue()));
            normalizedRecord.setActualValue(trimToEmpty(record.getActualValue()));
            normalizedRecords.add(normalizedRecord);
        }
        return normalizedRecords;
    }

    /**
     * 校验异常项备注：检查结果为 qualified/unqualified 时，检查备注必填
     */
    private void validateAbnormalCheckRemark(List<IotRunLogInspectionResultItemVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (IotRunLogInspectionResultItemVO item : items) {
            if (item == null) {
                continue;
            }
            if (isAbnormalCheckResult(item.getCheckResult()) && StrUtil.isBlank(item.getCheckRemark())) {
                throw exception(RUN_LOG_ABNORMAL_REMARK_REQUIRED);
            }
        }
    }

    /**
     * 按运行日志异常项自动生成故障记录（仅设备对象）
     */
    private void createFaultRepairsByRunLog(IotRunLogDO log,
                                            List<IotRunLogInspectionResultItemVO> items,
                                            LocalDateTime submitTime) {
        if (log == null || items == null || items.isEmpty()) {
            return;
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        String reporterName = StrUtil.blankToDefault(SecurityFrameworkUtils.getLoginUserNickname(),
                StrUtil.blankToDefault(log.getRecorderUserName(), loginUserId == null ? "" : String.valueOf(loginUserId)));
        String stationId = trimToEmpty(log.getStationId());
        List<String> faultImages = log.getAttachmentFileIds() == null
                ? Collections.emptyList()
                : Arrays.stream(log.getAttachmentFileIds())
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .toList();
        for (IotRunLogInspectionResultItemVO item : items) {
            if (item == null || !isAbnormalCheckResult(item.getCheckResult()) || !isDeviceTargetItem(item)) {
                continue;
            }
            IotDeviceDO device = resolveDeviceForFault(stationId, item);
            if (device == null) {
                continue;
            }
            String faultSymptom = trimToEmpty(item.getCheckRemark());
            if (StrUtil.isBlank(faultSymptom)) {
                throw exception(RUN_LOG_ABNORMAL_REMARK_REQUIRED);
            }
            String deviceType = device.getDeviceType() == null ? "" : String.valueOf(device.getDeviceType());
            if (StrUtil.isBlank(deviceType)) {
                continue;
            }
            IotFaultRepairSaveReqVO saveReqVO = new IotFaultRepairSaveReqVO();
            saveReqVO.setDeviceId(device.getId());
            saveReqVO.setDeviceName(StrUtil.blankToDefault(device.getNickname(), device.getDeviceName()));
            saveReqVO.setDeviceType(deviceType);
            saveReqVO.setFaultType(DEFAULT_FAULT_TYPE_OTHER);
            saveReqVO.setFaultTime(submitTime);
            saveReqVO.setFaultSymptom(faultSymptom);
            saveReqVO.setReporterUserId(loginUserId);
            saveReqVO.setReporterName(reporterName);
            saveReqVO.setFaultImages(faultImages);
            saveReqVO.setRemark(StrUtil.format("运行日志自动生成：{} / {}", trimToEmpty(log.getLogNo()), trimToEmpty(item.getItemName())));
            faultRepairService.createFaultRepair(saveReqVO);
        }
    }

    private IotDeviceDO resolveDeviceForFault(String stationId, IotRunLogInspectionResultItemVO item) {
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

    private boolean isDeviceTargetItem(IotRunLogInspectionResultItemVO item) {
        String targetType = trimToEmpty(item == null ? null : item.getTargetType()).toLowerCase(Locale.ROOT);
        return Objects.equals(targetType, STANDARD_DEVICE_TARGET_TYPE);
    }

    /**
     * 构建响应
     */
    private IotRunLogRespVO buildResp(IotRunLogDO log) {
        IotRunLogRespVO respVO = new IotRunLogRespVO();
        respVO.setId(log.getId());
        respVO.setLogNo(log.getLogNo());
        respVO.setTaskName(log.getLogTitle());
        respVO.setDutyTeamId(log.getTeamId());
        respVO.setDutyTeamName(log.getTeamName());
        respVO.setRecorderUserId(log.getRecorderUserId());
        respVO.setRecorderUserName(log.getRecorderUserName());
        respVO.setRecordTime(log.getRecordTime());
        respVO.setRunStartTime(log.getRunStartTime());
        respVO.setRunEndTime(log.getRunEndTime());
        respVO.setCheckPeriod(log.getCheckPeriod());
        respVO.setStationId(log.getStationId());
        respVO.setInspectionType(log.getInspectionType());
        respVO.setInspectionStandardId(log.getInspectionStandardId());
        respVO.setInspectionStandardName(log.getInspectionStandardName());
        respVO.setInspectionResultItems(parseInspectionResultItems(log.getInspectionResultItemsJson()));
        respVO.setDeviceName(log.getDeviceName());
        respVO.setDeviceNames(parseDeviceNames(log.getDeviceName()));
        respVO.setRunParamsText(log.getRunParamsText());
        respVO.setEventDesc(log.getEventDesc());
        respVO.setDispatchInstructionId(log.getDispatchInstructionId());
        respVO.setDispatchInstructionNo(log.getDispatchInstructionNo());
        respVO.setDispatchInstructionName(log.getDispatchInstructionName());
        respVO.setAttachments(log.getAttachmentFileIds() == null
                ? Collections.emptyList()
                : Arrays.asList(log.getAttachmentFileIds()));
        respVO.setRemark(log.getRemark());
        respVO.setCreateTime(log.getCreateTime());
        return respVO;
    }

    private List<IotRunLogInspectionResultItemVO> parseInspectionResultItems(String resultItemsJson) {
        if (StrUtil.isBlank(resultItemsJson)) {
            return Collections.emptyList();
        }
        try {
            List<IotRunLogInspectionResultItemVO> parsed = JsonUtils.parseArray(resultItemsJson, IotRunLogInspectionResultItemVO.class);
            return parsed == null ? Collections.emptyList() : parsed;
        } catch (Exception ignore) {
            return Collections.emptyList();
        }
    }

    /**
     * 生成记录编号：RL-YYYYMMDD-XXX
     */
    private String generateLogNo() {
        String prefix = "RL-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        String latest = runLogMapper.selectLatestLogNoByPrefix(prefix);
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
     * 是否管理员角色（超管、租户管理员）
     */
    private boolean hasAdminRole() {
        return securityFrameworkService.hasAnyRoles(RoleCodeEnum.SUPER_ADMIN.getCode(), RoleCodeEnum.TENANT_ADMIN.getCode());
    }

    private String resolveLoginUserName() {
        String nickname = trimToNull(SecurityFrameworkUtils.getLoginUserNickname());
        if (nickname != null) {
            return nickname;
        }
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return userId == null ? "" : String.valueOf(userId);
    }

    private List<String> parseDeviceNames(String deviceNameText) {
        String normalizedText = trimToNull(deviceNameText);
        if (normalizedText == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(normalizedText.split("[、,，\\n]"))
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private String[] normalizeAttachments(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new String[0];
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String attachment : attachments) {
            String value = trimToNull(attachment);
            if (value != null) {
                normalized.add(value);
            }
        }
        if (normalized.isEmpty()) {
            return new String[0];
        }
        return normalized.toArray(String[]::new);
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
     * 值班班组快照
     */
    private static class DutyTeamSnapshot {
        private Long teamId;
        private String teamName;
    }

    /**
     * 关联调令快照
     */
    private static class DispatchInstructionSnapshot {
        private Long id;
        private String instructionNo;
        private String instructionName;
    }

    /**
     * 巡检标准快照
     */
    private static class InspectionStandardSnapshot {
        private Long standardId;
        private String standardName = "";
        private String inspectionType = "";
    }
}
