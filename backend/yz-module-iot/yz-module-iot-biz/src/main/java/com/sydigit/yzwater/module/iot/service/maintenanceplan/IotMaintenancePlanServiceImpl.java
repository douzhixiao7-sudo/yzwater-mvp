package com.sydigit.yzwater.module.iot.service.maintenanceplan;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanCreateReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanSubmitReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanSpareUsageVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoAuditReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.maintenanceplan.IotMaintenancePlanDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.maintenanceplan.IotMaintenancePlanSpareUsageDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.maintenanceplan.IotMaintenancePlanMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.spare.IotSpareIoMapper;
import com.sydigit.yzwater.module.iot.enums.maintenanceplan.IotMaintenancePlanStatusEnum;
import com.sydigit.yzwater.module.iot.enums.maintenanceplan.IotMaintenancePlanTypeEnum;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareIoAuditStatusEnum;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareIoTypeEnum;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareUsageTypeEnum;
import com.sydigit.yzwater.module.iot.service.spare.IotSpareIoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.MAINTENANCE_PLAN_ALREADY_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.MAINTENANCE_PLAN_ALREADY_COMPLETED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.MAINTENANCE_PLAN_NOT_EXISTS;

/**
 * 养护计划 Service 实现
 */
@Service
@Validated
public class IotMaintenancePlanServiceImpl implements IotMaintenancePlanService {

    @Resource
    private IotMaintenancePlanMapper maintenancePlanMapper;
    @Resource
    private IotDeviceMapper deviceMapper;
    @Resource
    private IotSpareIoMapper spareIoMapper;
    @Resource
    private IotSpareIoService spareIoService;

    @Override
    public PageResult<IotMaintenancePlanDO> getMaintenancePlanPage(IotMaintenancePlanPageReqVO pageReqVO) {
        ensureAutoPlans();
        Collection<Long> deviceIds = resolveDeviceIds(pageReqVO);
        if (deviceIds != null && deviceIds.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        return maintenancePlanMapper.selectPage(pageReqVO, deviceIds);
    }

    @Override
    public IotMaintenancePlanDO getMaintenancePlan(Long id) {
        return maintenancePlanMapper.selectById(id);
    }

    @Override
    public Long createMaintenancePlan(IotMaintenancePlanCreateReqVO createReqVO) {
        IotDeviceDO device = deviceMapper.selectById(createReqVO.getDeviceId());
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        if (maintenancePlanMapper.selectCountByDeviceIdAndPlanDate(createReqVO.getDeviceId(), createReqVO.getPlanDate()) > 0) {
            throw exception(MAINTENANCE_PLAN_ALREADY_EXISTS);
        }
        IotMaintenancePlanDO plan = new IotMaintenancePlanDO();
        plan.setDeviceId(createReqVO.getDeviceId());
        plan.setStationId(device.getStationId());
        plan.setPlanDate(createReqVO.getPlanDate());
        plan.setMaintainType(StrUtil.blankToDefault(createReqVO.getMaintainType(), IotMaintenancePlanTypeEnum.PERIODIC.getType()));
        plan.setMaintainItems(StrUtil.blankToDefault(createReqVO.getMaintainItems(), ""));
        plan.setSpareUsages(Collections.emptyList());
        plan.setStatus(IotMaintenancePlanStatusEnum.PENDING.getStatus());
        plan.setRemark(StrUtil.blankToDefault(createReqVO.getRemark(), ""));
        plan.setSourceType("manual");
        maintenancePlanMapper.insert(plan);
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitMaintenancePlan(IotMaintenancePlanSubmitReqVO submitReqVO) {
        IotMaintenancePlanDO plan = validateMaintenancePlanExists(submitReqVO.getId());
        if (Objects.equals(plan.getStatus(), IotMaintenancePlanStatusEnum.COMPLETED.getStatus())) {
            throw exception(MAINTENANCE_PLAN_ALREADY_COMPLETED);
        }
        IotMaintenancePlanDO updateObj = new IotMaintenancePlanDO();
        updateObj.setId(plan.getId());
        updateObj.setMaintainType(StrUtil.blankToDefault(submitReqVO.getMaintainType(), plan.getMaintainType()));
        updateObj.setMaintainItems(submitReqVO.getMaintainItems());
        updateObj.setSpareUsages(convertSpareUsages(submitReqVO.getSpareUsages()));
        updateObj.setFinishTime(submitReqVO.getFinishTime() != null ? submitReqVO.getFinishTime() : LocalDateTime.now());
        updateObj.setStatus(IotMaintenancePlanStatusEnum.COMPLETED.getStatus());
        updateObj.setMaintainerUserId(SecurityFrameworkUtils.getLoginUserId());
        updateObj.setMaintainerName(StrUtil.blankToDefault(submitReqVO.getMaintainerName(),
                SecurityFrameworkUtils.getLoginUserNickname()));
        updateObj.setRemark(submitReqVO.getRemark());
        maintenancePlanMapper.updateById(updateObj);
        updateObj.setDeviceId(plan.getDeviceId());
        applySpareUsage(updateObj);
    }

    @Override
    public void syncAutoMaintenancePlanByDevice(Long deviceId) {
        if (deviceId == null) {
            return;
        }
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null || device.getLastMaintainTime() == null || device.getMaintainCycleDays() == null) {
            return;
        }
        Integer cycleDays = device.getMaintainCycleDays();
        if (cycleDays <= 0) {
            return;
        }
        LocalDate planDate = device.getLastMaintainTime().plusDays(cycleDays).toLocalDate();
        if (planDate.isAfter(LocalDate.now())) {
            return;
        }
        if (maintenancePlanMapper.selectCountByDeviceIdAndPlanDate(deviceId, planDate) > 0) {
            return;
        }
        IotMaintenancePlanDO plan = new IotMaintenancePlanDO();
        plan.setDeviceId(device.getId());
        plan.setStationId(device.getStationId());
        plan.setPlanDate(planDate);
        plan.setMaintainType(IotMaintenancePlanTypeEnum.PERIODIC.getType());
        plan.setMaintainItems("");
        plan.setSpareUsages(Collections.emptyList());
        plan.setStatus(IotMaintenancePlanStatusEnum.PENDING.getStatus());
        plan.setRemark("");
        plan.setSourceType("auto");
        maintenancePlanMapper.insert(plan);
    }

    private IotMaintenancePlanDO validateMaintenancePlanExists(Long id) {
        IotMaintenancePlanDO plan = maintenancePlanMapper.selectById(id);
        if (plan == null) {
            throw exception(MAINTENANCE_PLAN_NOT_EXISTS);
        }
        return plan;
    }

    private void ensureAutoPlans() {
        LocalDate today = LocalDate.now();
        List<IotDeviceDO> devices = deviceMapper.selectList(new com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX<IotDeviceDO>()
                .isNotNull(IotDeviceDO::getLastMaintainTime)
                .isNotNull(IotDeviceDO::getMaintainCycleDays));
        if (devices == null || devices.isEmpty()) {
            return;
        }

        List<IotDeviceDO> dueDevices = new ArrayList<>();
        List<LocalDate> dueDates = new ArrayList<>();
        for (IotDeviceDO device : devices) {
            if (device.getId() == null || device.getStationId() == null || device.getLastMaintainTime() == null) {
                continue;
            }
            Integer cycleDays = device.getMaintainCycleDays();
            if (cycleDays == null || cycleDays <= 0) {
                continue;
            }
            LocalDate planDate = device.getLastMaintainTime().plusDays(cycleDays).toLocalDate();
            if (planDate.isAfter(today)) {
                continue;
            }
            dueDevices.add(device);
            dueDates.add(planDate);
        }

        if (dueDevices.isEmpty()) {
            return;
        }

        List<Long> deviceIds = dueDevices.stream().map(IotDeviceDO::getId).collect(Collectors.toList());
        LocalDate minDate = dueDates.stream().min(LocalDate::compareTo).orElse(today);
        LocalDate maxDate = dueDates.stream().max(LocalDate::compareTo).orElse(today);
        List<IotMaintenancePlanDO> exists = maintenancePlanMapper
                .selectListByDeviceIdsAndPlanDateRange(deviceIds, minDate, maxDate);
        Set<String> existsKeys = exists.stream()
                .filter(item -> item.getDeviceId() != null && item.getPlanDate() != null)
                .map(item -> item.getDeviceId() + "_" + item.getPlanDate())
                .collect(Collectors.toSet());

        for (IotDeviceDO device : dueDevices) {
            Integer cycleDays = device.getMaintainCycleDays();
            if (cycleDays == null || cycleDays <= 0) {
                continue;
            }
            LocalDate planDate = device.getLastMaintainTime().plusDays(cycleDays).toLocalDate();
            String key = device.getId() + "_" + planDate;
            if (existsKeys.contains(key)) {
                continue;
            }
            IotMaintenancePlanDO plan = new IotMaintenancePlanDO();
            plan.setDeviceId(device.getId());
            plan.setStationId(device.getStationId());
            plan.setPlanDate(planDate);
            plan.setMaintainType(IotMaintenancePlanTypeEnum.PERIODIC.getType());
            plan.setMaintainItems("");
            plan.setSpareUsages(Collections.emptyList());
            plan.setStatus(IotMaintenancePlanStatusEnum.PENDING.getStatus());
            plan.setRemark("");
            plan.setSourceType("auto");
            maintenancePlanMapper.insert(plan);
        }
    }

    private Collection<Long> resolveDeviceIds(IotMaintenancePlanPageReqVO pageReqVO) {
        if (pageReqVO.getDeviceId() != null) {
            return Collections.singletonList(pageReqVO.getDeviceId());
        }
        if (StrUtil.isBlank(pageReqVO.getDeviceName()) && StrUtil.isBlank(pageReqVO.getDeviceType())) {
            return null;
        }
        com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX<IotDeviceDO> wrapper =
                new com.sydigit.yzwater.framework.mybatis.core.query.LambdaQueryWrapperX<>();
        if (StrUtil.isNotBlank(pageReqVO.getDeviceName())) {
            wrapper.and(query -> query.like(IotDeviceDO::getNickname, pageReqVO.getDeviceName())
                    .or().like(IotDeviceDO::getDeviceName, pageReqVO.getDeviceName()));
        }
        if (StrUtil.isNotBlank(pageReqVO.getDeviceType()) && NumberUtil.isInteger(pageReqVO.getDeviceType())) {
            wrapper.eq(IotDeviceDO::getDeviceType, Integer.valueOf(pageReqVO.getDeviceType()));
        }
        List<IotDeviceDO> devices = deviceMapper.selectList(wrapper);
        if (devices == null || devices.isEmpty()) {
            return Collections.emptyList();
        }
        return devices.stream().map(IotDeviceDO::getId).collect(Collectors.toList());
    }

    private void applySpareUsage(IotMaintenancePlanDO plan) {
        List<IotMaintenancePlanSpareUsageDO> spareUsages = plan.getSpareUsages();
        if (spareUsages == null || spareUsages.isEmpty()) {
            return;
        }
        if (spareIoMapper.selectCountByUsage(IotSpareUsageTypeEnum.MAINTENANCE.getType(), plan.getId()) > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (IotMaintenancePlanSpareUsageDO usage : spareUsages) {
            if (usage == null || usage.getSpareId() == null || usage.getQty() == null || usage.getQty() <= 0) {
                continue;
            }
            IotSpareIoSaveReqVO saveReqVO = new IotSpareIoSaveReqVO();
            saveReqVO.setSpareId(usage.getSpareId());
            saveReqVO.setIoType(IotSpareIoTypeEnum.OUT.getType());
            saveReqVO.setIoTime(now);
            saveReqVO.setIoQty(usage.getQty());
            saveReqVO.setUsageType(IotSpareUsageTypeEnum.MAINTENANCE.getType());
            saveReqVO.setUsageId(plan.getId());
            saveReqVO.setRemark("养护任务消耗");
            Long ioId = spareIoService.createSpareIo(saveReqVO);
            IotSpareIoAuditReqVO auditReqVO = new IotSpareIoAuditReqVO();
            auditReqVO.setId(ioId);
            auditReqVO.setAuditStatus(IotSpareIoAuditStatusEnum.APPROVED.getStatus());
            auditReqVO.setAuditRemark("养护任务自动出库");
            spareIoService.auditSpareIo(auditReqVO);
        }
    }

    private List<IotMaintenancePlanSpareUsageDO> convertSpareUsages(List<IotMaintenancePlanSpareUsageVO> spareUsages) {
        if (spareUsages == null || spareUsages.isEmpty()) {
            return Collections.emptyList();
        }
        return spareUsages.stream()
                .filter(item -> item != null && item.getSpareId() != null && item.getQty() != null && item.getQty() > 0)
                .map(item -> BeanUtils.toBean(item, IotMaintenancePlanSpareUsageDO.class))
                .collect(Collectors.toList());
    }
}
