package com.sydigit.yzwater.module.iot.service.inspectionplan;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanLineOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanStandardOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanTargetOptionRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.inspectionplan.vo.IotInspectionPlanTargetSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionline.IotInspectionLineDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionplan.IotInspectionPlanTargetDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.inspectionstandard.IotInspectionStandardDO;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionline.IotInspectionLineMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionplan.IotInspectionPlanMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionplan.IotInspectionPlanTargetMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.inspectionstandard.IotInspectionStandardMapper;
import com.sydigit.yzwater.module.iot.service.inspectiontask.IotInspectionTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_CYCLE_MONTH_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_CYCLE_UNIT_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_DATE_RANGE_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_UPDATE_FORBIDDEN_TASK_STATUS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_LINE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_NAME_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_OBJECT_TYPE_INVALID;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_STANDARD_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_TARGET_EMPTY;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.INSPECTION_PLAN_TARGET_NOT_EXISTS;

/**
 * 巡检计划 Service 实现
 */
@Service
@Validated
@Slf4j
public class IotInspectionPlanServiceImpl implements IotInspectionPlanService {

    private static final Integer OBJECT_TYPE_DEVICE = 1;
    private static final Integer OBJECT_TYPE_LOCATION = 2;
    private static final int DEFAULT_TARGET_OPTION_LIMIT = 300;
    private static final int MAX_TARGET_OPTION_LIMIT = 1000;
    private static final int DEFAULT_STANDARD_OPTION_LIMIT = 300;
    private static final int MAX_STANDARD_OPTION_LIMIT = 1000;
    private static final int DEFAULT_LINE_OPTION_LIMIT = 200;
    private static final int MAX_LINE_OPTION_LIMIT = 1000;
    private static final int DEFAULT_DUE_PLAN_LIMIT = 100;
    private static final int MAX_DUE_PLAN_LIMIT = 500;
    private static final Set<String> SUPPORTED_CYCLE_UNITS = Set.of("DAY", "WEEK", "MONTH", "QUARTER", "YEAR");
    private static final DateTimeFormatter CYCLE_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Resource
    private IotInspectionPlanMapper planMapper;
    @Resource
    private IotInspectionPlanTargetMapper targetMapper;
    @Resource
    private IotInspectionStandardMapper standardMapper;
    @Resource
    private IotInspectionLineMapper lineMapper;
    @Resource
    private IotInspectionTaskService taskService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(IotInspectionPlanSaveReqVO createReqVO) {
        validatePlanNameUnique(null, createReqVO.getPlanName());
        Integer objectType = normalizeObjectType(createReqVO.getObjectType());
        List<IotInspectionPlanTargetSaveReqVO> targets = normalizeTargets(createReqVO.getTargets());
        String cycleUnit = normalizeCycleUnit(createReqVO.getCycleUnit());
        Long standardId = validateStandardExists(createReqVO.getStandardId());
        Long lineId = validateLineExists(createReqVO.getLineId());
        Map<Long, TargetMeta> targetMetaMap = resolveTargetMetaMap(objectType, targets);

        IotInspectionPlanDO plan = buildPlanDO(createReqVO, objectType, cycleUnit, standardId, lineId);
        plan.setTargetCount(targets.size());
        plan.setStationId(resolveStationId(createReqVO.getStationId(), objectType, targets, targetMetaMap));
        plan.setPlanStatus(calculatePlanStatus(plan.getPlanStartTime(), plan.getPlanEndTime(), plan.getPlanStatus()));
        planMapper.insert(plan);

        saveTargets(plan.getId(), objectType, targets, targetMetaMap);
        taskService.createTaskFromPlan(plan.getId());
        advanceGenerateCursor(plan, plan.getNextGenerateTime());
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(IotInspectionPlanSaveReqVO updateReqVO) {
        IotInspectionPlanDO existed = validatePlanExists(updateReqVO.getId());
        if (!taskService.isPlanEditable(updateReqVO.getId())) {
            throw exception(INSPECTION_PLAN_UPDATE_FORBIDDEN_TASK_STATUS);
        }
        validatePlanNameUnique(updateReqVO.getId(), updateReqVO.getPlanName());
        Integer objectType = normalizeObjectType(updateReqVO.getObjectType());
        List<IotInspectionPlanTargetSaveReqVO> targets = normalizeTargets(updateReqVO.getTargets());
        String cycleUnit = normalizeCycleUnit(updateReqVO.getCycleUnit());
        Long standardId = validateStandardExists(updateReqVO.getStandardId());
        Long lineId = validateLineExists(updateReqVO.getLineId());
        Map<Long, TargetMeta> targetMetaMap = resolveTargetMetaMap(objectType, targets);

        IotInspectionPlanDO updateObj = buildPlanDO(updateReqVO, objectType, cycleUnit, standardId, lineId);
        updateObj.setId(updateReqVO.getId());
        updateObj.setGeneratedTaskCount(existed.getGeneratedTaskCount() == null ? 0 : existed.getGeneratedTaskCount());
        updateObj.setLastGenerateTime(existed.getLastGenerateTime());
        updateObj.setNextGenerateTime(existed.getNextGenerateTime() == null
                ? updateObj.getPlanStartTime()
                : existed.getNextGenerateTime());
        updateObj.setEnableStatus(existed.getEnableStatus() == null ? 0 : existed.getEnableStatus());
        updateObj.setTargetCount(targets.size());
        updateObj.setStationId(resolveStationId(updateReqVO.getStationId(), objectType, targets, targetMetaMap));
        Integer baseStatus = Objects.equals(existed.getPlanStatus(), 2) ? 2 : null;
        updateObj.setPlanStatus(calculatePlanStatus(updateObj.getPlanStartTime(), updateObj.getPlanEndTime(), baseStatus));
        planMapper.updateById(updateObj);

        targetMapper.deleteByPlanId(updateReqVO.getId());
        saveTargets(updateReqVO.getId(), objectType, targets, targetMetaMap);
        taskService.createTaskFromPlan(updateReqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long id) {
        IotInspectionPlanDO existed = validatePlanExists(id);
        taskService.cascadeDeletePlanAutoTasks(id);
        targetMapper.deleteByPlanId(id);
        renamePlanBeforeDelete(existed);
        planMapper.deleteById(id);
    }

    @Override
    public IotInspectionPlanDO getPlan(Long id) {
        IotInspectionPlanDO plan = planMapper.selectById(id);
        if (plan != null) {
            plan.setPlanStatus(calculatePlanStatus(plan.getPlanStartTime(), plan.getPlanEndTime(), plan.getPlanStatus()));
        }
        return plan;
    }

    @Override
    public IotInspectionPlanDO validatePlanExists(Long id) {
        IotInspectionPlanDO plan = planMapper.selectById(id);
        if (plan == null) {
            throw exception(INSPECTION_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    @Override
    public PageResult<IotInspectionPlanDO> getPlanPage(IotInspectionPlanPageReqVO pageReqVO) {
        LocalDateTime[] cycleMonthRange = parseCycleMonthRange(pageReqVO.getCycleMonth());
        PageResult<IotInspectionPlanDO> pageResult = planMapper.selectPage(pageReqVO, cycleMonthRange[0], cycleMonthRange[1]);
        pageResult.getList().forEach(plan ->
                plan.setPlanStatus(calculatePlanStatus(plan.getPlanStartTime(), plan.getPlanEndTime(), plan.getPlanStatus())));
        return pageResult;
    }

    @Override
    public List<IotInspectionPlanTargetDO> getPlanTargetList(Long planId) {
        return targetMapper.selectListByPlanId(planId);
    }

    @Override
    public List<IotInspectionPlanTargetDO> getPlanTargetList(Collection<Long> planIds) {
        return targetMapper.selectListByPlanIds(planIds);
    }

    @Override
    public List<IotInspectionPlanTargetOptionRespVO> listTargetOptions(Integer objectType,
                                                                       String stationId,
                                                                       String keyword,
                                                                       Integer limit) {
        Integer normalizedObjectType = normalizeObjectType(objectType);
        int validLimit = normalizeLimit(limit, DEFAULT_TARGET_OPTION_LIMIT, MAX_TARGET_OPTION_LIMIT);
        if (Objects.equals(normalizedObjectType, OBJECT_TYPE_DEVICE)) {
            return queryDeviceOptions(trimToNull(stationId), keyword, validLimit);
        }
        return queryLocationOptions(keyword, validLimit);
    }

    @Override
    public List<IotInspectionPlanStandardOptionRespVO> listStandardOptions(String stationId,
                                                                           String inspectionType,
                                                                           String keyword,
                                                                           Integer limit) {
        int validLimit = normalizeLimit(limit, DEFAULT_STANDARD_OPTION_LIMIT, MAX_STANDARD_OPTION_LIMIT);
        String normalizedStationId = trimToNull(stationId);
        String normalizedInspectionType = trimToNull(inspectionType);
        String normalizedKeyword = trimToNull(keyword);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT s.id, s.standard_name, s.inspection_type, s.suggest_cycle_unit, s.update_time ")
                .append("FROM yz_equipment_inspection_standard s ")
                .append("LEFT JOIN yz_equipment_inspection_standard_target t ON t.standard_id = s.id AND t.deleted = 0 ")
                .append("WHERE s.deleted = 0 AND s.status = 0");
        List<Object> args = new ArrayList<>();

        if (normalizedStationId != null) {
            sql.append(" AND t.station_id = ?");
            args.add(normalizedStationId);
        }
        if (normalizedInspectionType != null) {
            sql.append(" AND s.inspection_type = ?");
            args.add(normalizedInspectionType);
        }
        if (normalizedKeyword != null) {
            sql.append(" AND s.standard_name LIKE ?");
            args.add("%" + normalizedKeyword + "%");
        }
        sql.append(" ORDER BY s.update_time DESC, s.id DESC LIMIT ?");
        args.add(validLimit);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new IotInspectionPlanStandardOptionRespVO(
                        rs.getLong("id"),
                        rs.getString("standard_name"),
                        rs.getString("inspection_type"),
                        rs.getString("suggest_cycle_unit")),
                args.toArray());
    }

    @Override
    public List<IotInspectionPlanLineOptionRespVO> listLineOptions(String stationId,
                                                                   String inspectionType,
                                                                   String keyword,
                                                                   Integer limit) {
        int validLimit = normalizeLimit(limit, DEFAULT_LINE_OPTION_LIMIT, MAX_LINE_OPTION_LIMIT);
        String normalizedStationId = trimToNull(stationId);
        String normalizedInspectionType = trimToNull(inspectionType);
        String normalizedKeyword = trimToNull(keyword);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, line_name, station_id, inspection_type ")
                .append("FROM yz_equipment_inspection_line ")
                .append("WHERE deleted = 0 AND status = 0");
        List<Object> args = new ArrayList<>();
        if (normalizedStationId != null) {
            sql.append(" AND station_id = ?");
            args.add(normalizedStationId);
        }
        if (normalizedInspectionType != null) {
            sql.append(" AND inspection_type = ?");
            args.add(normalizedInspectionType);
        }
        if (normalizedKeyword != null) {
            sql.append(" AND line_name LIKE ?");
            args.add("%" + normalizedKeyword + "%");
        }
        sql.append(" ORDER BY update_time DESC, id DESC LIMIT ?");
        args.add(validLimit);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new IotInspectionPlanLineOptionRespVO(
                        rs.getLong("id"),
                        rs.getString("line_name"),
                        rs.getString("station_id"),
                        rs.getString("inspection_type")),
                args.toArray());
    }

    @Override
    public Map<Long, String> getStandardNameMap(Collection<Long> standardIds) {
        if (standardIds == null || standardIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<IotInspectionStandardDO> standards = standardMapper.selectBatchIds(standardIds);
        if (standards == null || standards.isEmpty()) {
            return Collections.emptyMap();
        }
        return standards.stream().collect(Collectors.toMap(
                IotInspectionStandardDO::getId,
                item -> StrUtil.blankToDefault(item.getStandardName(), "标准-" + item.getId()),
                (a, b) -> a,
                LinkedHashMap::new));
    }

    @Override
    public Map<Long, String> getLineNameMap(Collection<Long> lineIds) {
        if (lineIds == null || lineIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<IotInspectionLineDO> lines = lineMapper.selectBatchIds(lineIds);
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyMap();
        }
        return lines.stream().collect(Collectors.toMap(
                IotInspectionLineDO::getId,
                item -> StrUtil.blankToDefault(item.getLineName(), "线路-" + item.getId()),
                (a, b) -> a,
                LinkedHashMap::new));
    }

    @Override
    public int generateDuePlanTasks(Integer limit) {
        int validLimit = normalizeLimit(limit, DEFAULT_DUE_PLAN_LIMIT, MAX_DUE_PLAN_LIMIT);
        LocalDateTime now = LocalDateTime.now();
        List<IotInspectionPlanDO> duePlans = planMapper.selectDueGeneratePlans(now, validLimit);
        if (duePlans == null || duePlans.isEmpty()) {
            return 0;
        }
        int successCount = 0;
        for (IotInspectionPlanDO plan : duePlans) {
            if (plan == null || plan.getId() == null) {
                continue;
            }
            LocalDateTime expectedNextTime = plan.getNextGenerateTime();
            LocalDateTime windowStartTime = expectedNextTime == null ? plan.getPlanStartTime() : expectedNextTime;
            if (windowStartTime == null || windowStartTime.isAfter(now)) {
                continue;
            }
            LocalDateTime windowEndTime = calculateWindowEndTime(plan, windowStartTime);
            try {
                taskService.createTaskFromPlanByWindow(plan.getId(), windowStartTime, windowEndTime);
                if (advanceGenerateCursor(plan, expectedNextTime) > 0) {
                    successCount++;
                }
            } catch (Exception ex) {
                log.error("[generateDuePlanTasks][计划自动生成任务失败][planId={}]", plan.getId(), ex);
            }
        }
        return successCount;
    }

    /**
     * 构建计划数据对象。
     */
    private IotInspectionPlanDO buildPlanDO(IotInspectionPlanSaveReqVO reqVO,
                                            Integer objectType,
                                            String cycleUnit,
                                            Long standardId,
                                            Long lineId) {
        LocalDateTime startTime = reqVO.getPlanStartDate();
        LocalDateTime endTime = reqVO.getPlanEndDate();
        if (endTime.isBefore(startTime)) {
            throw exception(INSPECTION_PLAN_DATE_RANGE_INVALID);
        }

        IotInspectionPlanDO plan = new IotInspectionPlanDO();
        plan.setPlanName(reqVO.getPlanName().trim());
        plan.setInspectionType(trimToEmpty(reqVO.getInspectionType()));
        plan.setObjectType(objectType);
        plan.setStandardId(standardId);
        plan.setLineId(lineId);
        plan.setCycleUnit(cycleUnit);
        plan.setCycleValue(buildCycleValue(reqVO.getCycleValue()));
        plan.setPlanStartTime(startTime);
        plan.setPlanEndTime(endTime);
        plan.setExecutorUserId(reqVO.getExecutorUserId());
        plan.setExecutorName(trimToEmpty(reqVO.getExecutorName()));
        plan.setExecuteDeptId(reqVO.getExecuteDeptId());
        plan.setExecuteDeptName(trimToEmpty(reqVO.getExecuteDeptName()));
        plan.setEnableStatus(0);
        plan.setLastGenerateTime(null);
        plan.setNextGenerateTime(startTime);
        plan.setGeneratedTaskCount(0);
        plan.setRemark(trimToEmpty(reqVO.getRemark()));
        return plan;
    }

    /**
     * 保存计划对象明细。
     */
    private void saveTargets(Long planId,
                             Integer objectType,
                             List<IotInspectionPlanTargetSaveReqVO> targets,
                             Map<Long, TargetMeta> targetMetaMap) {
        for (int i = 0; i < targets.size(); i++) {
            IotInspectionPlanTargetSaveReqVO targetReq = targets.get(i);
            TargetMeta targetMeta = targetMetaMap.get(targetReq.getTargetId());
            if (targetMeta == null) {
                throw exception(INSPECTION_PLAN_TARGET_NOT_EXISTS);
            }
            IotInspectionPlanTargetDO targetDO = new IotInspectionPlanTargetDO();
            targetDO.setPlanId(planId);
            targetDO.setTargetType(objectType);
            targetDO.setTargetSort(targetReq.getTargetSort() == null ? i + 1 : targetReq.getTargetSort());
            targetDO.setTargetName(targetMeta.targetName);
            targetDO.setStationId(StrUtil.blankToDefault(targetMeta.stationId, ""));
            if (Objects.equals(objectType, OBJECT_TYPE_DEVICE)) {
                targetDO.setDeviceId(targetReq.getTargetId());
                targetDO.setLocationId(null);
            } else {
                targetDO.setLocationId(targetReq.getTargetId());
                targetDO.setDeviceId(null);
            }
            targetMapper.insert(targetDO);
        }
    }

    /**
     * 校验计划名称唯一。
     */
    private void validatePlanNameUnique(Long id, String planName) {
        if (StrUtil.isBlank(planName)) {
            return;
        }
        IotInspectionPlanDO existed = planMapper.selectByName(planName.trim());
        if (existed == null) {
            return;
        }
        if (!Objects.equals(existed.getId(), id)) {
            throw exception(INSPECTION_PLAN_NAME_EXISTS);
        }
    }

    /**
     * 校验并返回标准 ID。
     */
    private Long validateStandardExists(Long standardId) {
        if (standardId == null) {
            return null;
        }
        IotInspectionStandardDO standard = standardMapper.selectById(standardId);
        if (standard == null) {
            throw exception(INSPECTION_PLAN_STANDARD_NOT_EXISTS);
        }
        return standardId;
    }

    /**
     * 校验并返回线路 ID。
     */
    private Long validateLineExists(Long lineId) {
        if (lineId == null) {
            return null;
        }
        IotInspectionLineDO line = lineMapper.selectById(lineId);
        if (line == null) {
            throw exception(INSPECTION_PLAN_LINE_NOT_EXISTS);
        }
        return lineId;
    }

    /**
     * 规范化对象列表并去重。
     */
    private List<IotInspectionPlanTargetSaveReqVO> normalizeTargets(List<IotInspectionPlanTargetSaveReqVO> targets) {
        if (targets == null || targets.isEmpty()) {
            throw exception(INSPECTION_PLAN_TARGET_EMPTY);
        }
        Set<Long> exists = new LinkedHashSet<>();
        List<IotInspectionPlanTargetSaveReqVO> result = new ArrayList<>();
        for (IotInspectionPlanTargetSaveReqVO target : targets) {
            if (target == null || target.getTargetId() == null) {
                continue;
            }
            if (!exists.add(target.getTargetId())) {
                continue;
            }
            IotInspectionPlanTargetSaveReqVO normalized = new IotInspectionPlanTargetSaveReqVO();
            normalized.setTargetId(target.getTargetId());
            normalized.setTargetName(trimToNull(target.getTargetName()));
            normalized.setTargetSort(target.getTargetSort());
            result.add(normalized);
        }
        if (result.isEmpty()) {
            throw exception(INSPECTION_PLAN_TARGET_EMPTY);
        }
        for (int i = 0; i < result.size(); i++) {
            IotInspectionPlanTargetSaveReqVO item = result.get(i);
            if (item.getTargetSort() == null || item.getTargetSort() <= 0) {
                item.setTargetSort(i + 1);
            }
        }
        return result;
    }

    /**
     * 根据对象类型查询名称快照。
     */
    private Map<Long, TargetMeta> resolveTargetMetaMap(Integer objectType,
                                                       List<IotInspectionPlanTargetSaveReqVO> targets) {
        List<Long> targetIds = targets.stream().map(IotInspectionPlanTargetSaveReqVO::getTargetId).toList();
        Map<Long, TargetMeta> targetMetaMap;
        if (Objects.equals(objectType, OBJECT_TYPE_DEVICE)) {
            targetMetaMap = queryDeviceMetaMap(targetIds);
        } else if (Objects.equals(objectType, OBJECT_TYPE_LOCATION)) {
            targetMetaMap = queryLocationMetaMap(targetIds);
        } else {
            throw exception(INSPECTION_PLAN_OBJECT_TYPE_INVALID);
        }

        for (Long targetId : targetIds) {
            if (!targetMetaMap.containsKey(targetId)) {
                throw exception(INSPECTION_PLAN_TARGET_NOT_EXISTS);
            }
        }
        return targetMetaMap;
    }

    /**
     * 查询设备对象元数据。
     */
    private Map<Long, TargetMeta> queryDeviceMetaMap(List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = String.join(",", Collections.nCopies(targetIds.size(), "?"));
        String sql = "SELECT id, station_id, COALESCE(NULLIF(nickname, ''), NULLIF(device_name, ''), CONCAT('设备-', id)) AS name "
                + "FROM iot_device WHERE deleted = 0 AND id IN (" + placeholders + ")";
        List<TargetMeta> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new TargetMeta(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("station_id")),
                targetIds.toArray());
        Map<Long, TargetMeta> result = new HashMap<>();
        for (TargetMeta row : rows) {
            result.put(row.targetId, row);
        }
        return result;
    }

    /**
     * 查询位置对象元数据。
     */
    private Map<Long, TargetMeta> queryLocationMetaMap(List<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String placeholders = String.join(",", Collections.nCopies(targetIds.size(), "?"));
        String sql = "SELECT id, COALESCE(NULLIF(name, ''), CONCAT('位置-', id)) AS name "
                + "FROM iot_device_location WHERE deleted = 0 AND id IN (" + placeholders + ")";
        List<TargetMeta> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new TargetMeta(
                rs.getLong("id"),
                rs.getString("name"),
                ""),
                targetIds.toArray());
        Map<Long, TargetMeta> result = new HashMap<>();
        for (TargetMeta row : rows) {
            result.put(row.targetId, row);
        }
        return result;
    }

    /**
     * 查询设备下拉选项。
     */
    private List<IotInspectionPlanTargetOptionRespVO> queryDeviceOptions(String stationId,
                                                                         String keyword,
                                                                         int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, station_id, ")
                .append("COALESCE(NULLIF(nickname, ''), NULLIF(device_name, ''), CONCAT('设备-', id)) AS name ")
                .append("FROM iot_device WHERE deleted = 0");
        List<Object> args = new ArrayList<>();

        if (stationId != null) {
            sql.append(" AND station_id = ?");
            args.add(stationId);
        }

        String normalizedKeyword = trimToNull(keyword);
        if (normalizedKeyword != null) {
            sql.append(" AND (nickname LIKE ? OR device_name LIKE ? OR serial_number LIKE ?)");
            String fuzzy = "%" + normalizedKeyword + "%";
            args.add(fuzzy);
            args.add(fuzzy);
            args.add(fuzzy);
        }

        sql.append(" ORDER BY update_time DESC, id DESC LIMIT ?");
        args.add(limit);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new IotInspectionPlanTargetOptionRespVO(
                        rs.getLong("id"),
                        rs.getString("name"),
                        OBJECT_TYPE_DEVICE,
                        rs.getString("station_id")),
                args.toArray());
    }

    /**
     * 查询位置下拉选项。
     */
    private List<IotInspectionPlanTargetOptionRespVO> queryLocationOptions(String keyword, int limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, COALESCE(NULLIF(name, ''), CONCAT('位置-', id)) AS name ")
                .append("FROM iot_device_location WHERE deleted = 0");
        List<Object> args = new ArrayList<>();

        String normalizedKeyword = trimToNull(keyword);
        if (normalizedKeyword != null) {
            sql.append(" AND name LIKE ?");
            args.add("%" + normalizedKeyword + "%");
        }

        sql.append(" ORDER BY sort ASC, id ASC LIMIT ?");
        args.add(limit);

        return jdbcTemplate.query(sql.toString(),
                (rs, rowNum) -> new IotInspectionPlanTargetOptionRespVO(
                        rs.getLong("id"),
                        rs.getString("name"),
                        OBJECT_TYPE_LOCATION,
                        null),
                args.toArray());
    }

    /**
     * 计算本次生成窗口结束时间，时长沿用计划配置的开始/结束跨度。
     */
    private LocalDateTime calculateWindowEndTime(IotInspectionPlanDO plan, LocalDateTime windowStartTime) {
        if (plan.getPlanStartTime() == null || plan.getPlanEndTime() == null || windowStartTime == null) {
            return windowStartTime;
        }
        long seconds = java.time.Duration.between(plan.getPlanStartTime(), plan.getPlanEndTime()).getSeconds();
        if (seconds <= 0) {
            return windowStartTime;
        }
        return windowStartTime.plusSeconds(seconds);
    }

    /**
     * 推进巡检计划生成游标（last/next/count）。
     */
    private int advanceGenerateCursor(IotInspectionPlanDO plan, LocalDateTime expectedNextTime) {
        if (plan == null || plan.getId() == null) {
            return 0;
        }
        LocalDateTime generateTime = expectedNextTime == null ? plan.getPlanStartTime() : expectedNextTime;
        if (generateTime == null) {
            return 0;
        }
        LocalDateTime nextGenerateTime = calculateNextGenerateTime(generateTime,
                plan.getCycleUnit(),
                buildCycleValue(plan.getCycleValue()));
        return planMapper.updateGenerateCursor(plan.getId(), expectedNextTime, generateTime, nextGenerateTime);
    }

    /**
     * 根据计划周期推进下次生成时间。
     */
    private LocalDateTime calculateNextGenerateTime(LocalDateTime baseTime, String cycleUnit, Integer cycleValue) {
        if (baseTime == null) {
            return null;
        }
        int safeCycleValue = cycleValue == null || cycleValue <= 0 ? 1 : cycleValue;
        if (StrUtil.isBlank(cycleUnit)) {
            return baseTime.plusDays(safeCycleValue);
        }
        return switch (cycleUnit.toUpperCase(Locale.ROOT)) {
            case "DAY" -> baseTime.plusDays(safeCycleValue);
            case "WEEK" -> baseTime.plusWeeks(safeCycleValue);
            case "MONTH" -> baseTime.plusMonths(safeCycleValue);
            case "QUARTER" -> baseTime.plusMonths(3L * safeCycleValue);
            case "YEAR" -> baseTime.plusYears(safeCycleValue);
            default -> baseTime.plusDays(safeCycleValue);
        };
    }

    /**
     * 解析周期月份范围。
     */
    private LocalDateTime[] parseCycleMonthRange(String cycleMonth) {
        String normalizedCycleMonth = trimToNull(cycleMonth);
        if (normalizedCycleMonth == null) {
            return new LocalDateTime[]{null, null};
        }
        try {
            YearMonth yearMonth = YearMonth.parse(normalizedCycleMonth, CYCLE_MONTH_FORMATTER);
            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.atEndOfMonth().atTime(LocalTime.of(23, 59, 59));
            return new LocalDateTime[]{start, end};
        } catch (DateTimeParseException ex) {
            throw exception(INSPECTION_PLAN_CYCLE_MONTH_INVALID);
        }
    }

    /**
     * 计算计划状态：未开始/未完成/已完成/已逾期。
     */
    private Integer calculatePlanStatus(LocalDateTime planStartTime,
                                        LocalDateTime planEndTime,
                                        Integer existedStatus) {
        if (Objects.equals(existedStatus, 2)) {
            return 2;
        }
        LocalDateTime now = LocalDateTime.now();
        if (planStartTime != null && now.isBefore(planStartTime)) {
            return 0;
        }
        if (planEndTime != null && now.isAfter(planEndTime)) {
            return 3;
        }
        return 1;
    }

    /**
     * 规范化对象类型。
     */
    private Integer normalizeObjectType(Integer objectType) {
        if (Objects.equals(objectType, OBJECT_TYPE_DEVICE)
                || Objects.equals(objectType, OBJECT_TYPE_LOCATION)) {
            return objectType;
        }
        throw exception(INSPECTION_PLAN_OBJECT_TYPE_INVALID);
    }

    /**
     * 规范化周期单位。
     */
    private String normalizeCycleUnit(String cycleUnit) {
        String normalizedCycleUnit = trimToNull(cycleUnit);
        if (normalizedCycleUnit == null) {
            throw exception(INSPECTION_PLAN_CYCLE_UNIT_INVALID);
        }
        normalizedCycleUnit = normalizedCycleUnit.toUpperCase(Locale.ROOT);
        if (!SUPPORTED_CYCLE_UNITS.contains(normalizedCycleUnit)) {
            throw exception(INSPECTION_PLAN_CYCLE_UNIT_INVALID);
        }
        return normalizedCycleUnit;
    }

    /**
     * 限制查询条数。
     */
    private int normalizeLimit(Integer limit, int defaultLimit, int maxLimit) {
        if (limit == null || limit <= 0) {
            return defaultLimit;
        }
        return Math.min(limit, maxLimit);
    }

    /**
     * 规范化周期值。
     */
    private Integer buildCycleValue(Integer cycleValue) {
        if (cycleValue == null || cycleValue <= 0) {
            return 1;
        }
        return cycleValue;
    }

    /**
     * 根据对象回填站点。
     */
    private String resolveStationId(String reqStationId,
                                    Integer objectType,
                                    List<IotInspectionPlanTargetSaveReqVO> targets,
                                    Map<Long, TargetMeta> targetMetaMap) {
        String normalizedStationId = trimToNull(reqStationId);
        if (normalizedStationId != null) {
            return normalizedStationId;
        }
        if (!Objects.equals(objectType, OBJECT_TYPE_DEVICE)) {
            return "";
        }
        for (IotInspectionPlanTargetSaveReqVO target : targets) {
            TargetMeta targetMeta = targetMetaMap.get(target.getTargetId());
            if (targetMeta != null && StrUtil.isNotBlank(targetMeta.stationId)) {
                return targetMeta.stationId;
            }
        }
        return "";
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
     * 逻辑删除前改名，规避唯一索引 (tenant_id, plan_name, deleted) 在 deleted=1 场景下冲突。
     */
    private void renamePlanBeforeDelete(IotInspectionPlanDO existed) {
        if (existed == null || existed.getId() == null) {
            return;
        }
        IotInspectionPlanDO updateObj = new IotInspectionPlanDO();
        updateObj.setId(existed.getId());
        updateObj.setPlanName("DEL-" + existed.getId());
        planMapper.updateById(updateObj);
    }

    /**
     * 对象元数据。
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

}
