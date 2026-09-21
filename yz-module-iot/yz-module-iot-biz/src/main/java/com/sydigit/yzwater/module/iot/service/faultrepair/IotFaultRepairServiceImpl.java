package com.sydigit.yzwater.module.iot.service.faultrepair;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.security.core.service.SecurityFrameworkService;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairAuditAssignReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairResultReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo.IotFaultRepairSpareUsageVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.faultrepair.IotFaultRepairDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.faultrepair.IotFaultRepairSpareUsageDO;
import com.sydigit.yzwater.module.iot.dal.mysql.faultrepair.IotFaultRepairMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.spare.IotSpareIoMapper;
import com.sydigit.yzwater.module.iot.enums.faultrepair.IotFaultRepairStatusEnum;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareIoTypeEnum;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareUsageTypeEnum;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.spare.IotSpareIoService;
import com.sydigit.yzwater.module.system.enums.permission.RoleCodeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.FAULT_REPAIR_DELETE_FAIL_HAS_SPARE_IO;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.FAULT_REPAIR_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.FAULT_REPAIR_STATUS_NOT_ASSIGNABLE;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.FAULT_REPAIR_STATUS_NOT_FEEDBACKABLE;

/**
 * 故障维修工单 Service 实现
 */
@Service
@Validated
public class IotFaultRepairServiceImpl implements IotFaultRepairService {

    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private IotFaultRepairMapper faultRepairMapper;
    @Resource
    private IotSpareIoMapper spareIoMapper;
    @Resource
    private IotSpareIoService spareIoService;
    @Resource
    private IotDeviceService deviceService;
    @Resource
    private SecurityFrameworkService securityFrameworkService;

    @Override
    public Long createFaultRepair(IotFaultRepairSaveReqVO createReqVO) {
        IotFaultRepairDO faultRepair = BeanUtils.toBean(createReqVO, IotFaultRepairDO.class);
        fillDeviceSnapshot(faultRepair, createReqVO);
        faultRepair.setFaultImages(convertImages(createReqVO.getFaultImages()));
        faultRepair.setSpareUsages(convertSpareUsages(createReqVO.getSpareUsages()));
        if (StrUtil.isBlank(faultRepair.getStatus())) {
            faultRepair.setStatus(IotFaultRepairStatusEnum.PENDING.getStatus());
        }
        if (StrUtil.isBlank(faultRepair.getOrderNo())) {
            faultRepair.setOrderNo(generateOrderNo());
        }
        if (faultRepair.getReporterUserId() == null) {
            faultRepair.setReporterUserId(SecurityFrameworkUtils.getLoginUserId());
        }
        if (StrUtil.isBlank(faultRepair.getReporterName())) {
            faultRepair.setReporterName(SecurityFrameworkUtils.getLoginUserNickname());
        }
        faultRepairMapper.insert(faultRepair);
        return faultRepair.getId();
    }

    @Override
    public void updateFaultRepair(IotFaultRepairSaveReqVO updateReqVO) {
        IotFaultRepairDO exists = validateFaultRepairExists(updateReqVO.getId());
        IotFaultRepairDO updateObj = BeanUtils.toBean(updateReqVO, IotFaultRepairDO.class);
        fillDeviceSnapshot(updateObj, updateReqVO);
        updateObj.setFaultImages(convertImages(updateReqVO.getFaultImages()));
        updateObj.setSpareUsages(convertSpareUsages(updateReqVO.getSpareUsages()));
        if (StrUtil.isBlank(updateObj.getStatus())) {
            updateObj.setStatus(exists.getStatus());
        }
        if (updateObj.getReporterUserId() == null) {
            updateObj.setReporterUserId(exists.getReporterUserId());
        }
        if (StrUtil.isBlank(updateObj.getReporterName())) {
            updateObj.setReporterName(exists.getReporterName());
        }
        if (StrUtil.isBlank(updateObj.getOrderNo())) {
            updateObj.setOrderNo(exists.getOrderNo());
        }
        if (StrUtil.isBlank(updateObj.getRepairName())) {
            updateObj.setRepairName(exists.getRepairName());
        }
        updateObj.setRepairUserId(exists.getRepairUserId());
        if (updateObj.getFinishTime() == null) {
            updateObj.setFinishTime(exists.getFinishTime());
        }
        if (updateObj.getPlanFinishTime() == null) {
            updateObj.setPlanFinishTime(exists.getPlanFinishTime());
        }
        if (StrUtil.isBlank(updateObj.getRemark())) {
            updateObj.setRemark(exists.getRemark());
        }
        faultRepairMapper.updateById(updateObj);
    }

    @Override
    public void deleteFaultRepair(Long id) {
        validateFaultRepairExists(id);
        if (spareIoMapper.selectCountByUsage(IotSpareUsageTypeEnum.FAULT.getType(), id) > 0) {
            throw exception(FAULT_REPAIR_DELETE_FAIL_HAS_SPARE_IO);
        }
        faultRepairMapper.deleteById(id);
    }

    @Override
    public IotFaultRepairDO getFaultRepair(Long id) {
        return faultRepairMapper.selectById(id);
    }

    @Override
    public PageResult<IotFaultRepairDO> getFaultRepairPage(IotFaultRepairPageReqVO pageReqVO) {
        applyCurrentUserDataScope(pageReqVO);
        return faultRepairMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotFaultRepairDO> getFaultRepairList(IotFaultRepairPageReqVO exportReqVO) {
        applyCurrentUserDataScope(exportReqVO);
        return faultRepairMapper.selectList(exportReqVO);
    }

    @Override
    public void auditAssign(IotFaultRepairAuditAssignReqVO reqVO) {
        IotFaultRepairDO faultRepair = validateFaultRepairExists(reqVO.getId());
        if (!Objects.equals(faultRepair.getStatus(), IotFaultRepairStatusEnum.PENDING.getStatus())) {
            throw exception(FAULT_REPAIR_STATUS_NOT_ASSIGNABLE);
        }
        faultRepair.setStatus(IotFaultRepairStatusEnum.PROCESSING.getStatus());
        faultRepair.setRepairUserId(reqVO.getRepairUserId());
        faultRepair.setRepairName(StrUtil.blankToDefault(reqVO.getRepairName(), SecurityFrameworkUtils.getLoginUserNickname()));
        faultRepair.setPlanFinishTime(reqVO.getPlanFinishTime());
        if (StrUtil.isNotBlank(reqVO.getRemark())) {
            faultRepair.setRemark(reqVO.getRemark());
        }
        faultRepairMapper.updateById(faultRepair);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitResult(IotFaultRepairResultReqVO reqVO) {
        IotFaultRepairDO faultRepair = validateFaultRepairExists(reqVO.getId());
        if (!Objects.equals(faultRepair.getStatus(), IotFaultRepairStatusEnum.PROCESSING.getStatus())
                && !Objects.equals(faultRepair.getStatus(), "assigned")) {
            throw exception(FAULT_REPAIR_STATUS_NOT_FEEDBACKABLE);
        }
        faultRepair.setFinishTime(reqVO.getFinishTime() != null ? reqVO.getFinishTime() : LocalDateTime.now());
        faultRepair.setStatus(StrUtil.blankToDefault(reqVO.getStatus(), IotFaultRepairStatusEnum.COMPLETED.getStatus()));
        if (reqVO.getSpareUsages() != null) {
            faultRepair.setSpareUsages(convertSpareUsages(reqVO.getSpareUsages()));
        }
        if (StrUtil.isNotBlank(reqVO.getRepairName())) {
            faultRepair.setRepairName(reqVO.getRepairName());
        }
        if (faultRepair.getRepairUserId() == null) {
            faultRepair.setRepairUserId(SecurityFrameworkUtils.getLoginUserId());
        }
        if (StrUtil.isNotBlank(reqVO.getRemark())) {
            faultRepair.setRemark(reqVO.getRemark());
        }
        faultRepairMapper.updateById(faultRepair);
        applySpareUsage(faultRepair);
    }

    private String generateOrderNo() {
        String datePart = LocalDate.now().format(ORDER_DATE_FORMATTER);
        String prefix = "GZ" + datePart;
        String maxOrderNo = faultRepairMapper.selectMaxOrderNo(prefix);
        int next = 1;
        if (StrUtil.isNotBlank(maxOrderNo) && maxOrderNo.length() > prefix.length()) {
            String seqStr = maxOrderNo.substring(prefix.length());
            if (StrUtil.isNumeric(seqStr)) {
                next = Integer.parseInt(seqStr) + 1;
            }
        }
        return prefix + String.format("%03d", next);
    }

    private void applyCurrentUserDataScope(IotFaultRepairPageReqVO reqVO) {
        if (reqVO == null || hasAdminRole()) {
            return;
        }
        reqVO.setRepairUserId(SecurityFrameworkUtils.getLoginUserId());
    }

    private boolean hasAdminRole() {
        return securityFrameworkService.hasAnyRoles(
                RoleCodeEnum.SUPER_ADMIN.getCode(),
                RoleCodeEnum.TENANT_ADMIN.getCode(),
                RoleCodeEnum.CRM_ADMIN.getCode());
    }

    private IotFaultRepairDO validateFaultRepairExists(Long id) {
        IotFaultRepairDO faultRepair = faultRepairMapper.selectById(id);
        if (faultRepair == null) {
            throw exception(FAULT_REPAIR_NOT_EXISTS);
        }
        return faultRepair;
    }

    private void fillDeviceSnapshot(IotFaultRepairDO faultRepair, IotFaultRepairSaveReqVO reqVO) {
        if (reqVO.getDeviceId() == null) {
            return;
        }
        IotDeviceDO device = deviceService.validateDeviceExists(reqVO.getDeviceId());
        if (StrUtil.isBlank(faultRepair.getDeviceName())) {
            String deviceName = StrUtil.blankToDefault(device.getNickname(), device.getDeviceName());
            faultRepair.setDeviceName(deviceName);
        }
        if (StrUtil.isBlank(faultRepair.getDeviceType()) && device.getDeviceType() != null) {
            faultRepair.setDeviceType(String.valueOf(device.getDeviceType()));
        }
    }

    private void applySpareUsage(IotFaultRepairDO faultRepair) {
        List<IotFaultRepairSpareUsageDO> spareUsages = faultRepair.getSpareUsages();
        if (spareUsages == null || spareUsages.isEmpty()) {
            return;
        }
        if (spareIoMapper.selectCountByUsage(IotSpareUsageTypeEnum.FAULT.getType(), faultRepair.getId()) > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (IotFaultRepairSpareUsageDO usage : spareUsages) {
            if (usage == null || usage.getSpareId() == null || usage.getQty() == null || usage.getQty() <= 0) {
                continue;
            }
            IotSpareIoSaveReqVO saveReqVO = new IotSpareIoSaveReqVO();
            saveReqVO.setSpareId(usage.getSpareId());
            saveReqVO.setIoType(IotSpareIoTypeEnum.OUT.getType());
            saveReqVO.setIoTime(now);
            saveReqVO.setIoQty(usage.getQty());
            saveReqVO.setUsageType(IotSpareUsageTypeEnum.FAULT.getType());
            saveReqVO.setUsageId(faultRepair.getId());
            saveReqVO.setRemark("故障维修消耗");
            // 备件出入库为“创建即审批通过”模式，避免重复审批导致状态异常
            spareIoService.createSpareIo(saveReqVO);
        }
    }

    private String[] convertImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return new String[0];
        }
        return images.toArray(new String[0]);
    }

    private List<IotFaultRepairSpareUsageDO> convertSpareUsages(List<IotFaultRepairSpareUsageVO> spareUsages) {
        if (spareUsages == null || spareUsages.isEmpty()) {
            return Collections.emptyList();
        }
        return spareUsages.stream()
                .filter(item -> item != null && item.getSpareId() != null && item.getQty() != null && item.getQty() > 0)
                .map(item -> BeanUtils.toBean(item, IotFaultRepairSpareUsageDO.class))
                .collect(Collectors.toList());
    }
}
