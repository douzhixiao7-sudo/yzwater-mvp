package com.sydigit.yzwater.module.iot.service.spare;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoAuditReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.io.IotSpareIoSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareIoDO;
import com.sydigit.yzwater.module.iot.dal.mysql.spare.IotSpareIoMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.spare.IotSpareMapper;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareIoAuditStatusEnum;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareIoTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_IO_AUDIT_STATUS_NOT_APPROVED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_IO_AUDIT_STATUS_NOT_PENDING;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_IO_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_IO_REVERSE_STOCK_NOT_ENOUGH;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_IO_STOCK_NOT_ENOUGH;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_NOT_EXISTS;

/**
 * 备件出入库 Service 实现
 */
@Service
@Validated
public class IotSpareIoServiceImpl implements IotSpareIoService {

    @Resource
    private IotSpareIoMapper spareIoMapper;
    @Resource
    private IotSpareMapper spareMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSpareIo(IotSpareIoSaveReqVO createReqVO) {
        validateSpareExists(createReqVO.getSpareId());
        IotSpareIoDO spareIo = BeanUtils.toBean(createReqVO, IotSpareIoDO.class);
        spareIo.setAuditStatus(IotSpareIoAuditStatusEnum.APPROVED.getStatus());
        spareIo.setOperatorUserId(SecurityFrameworkUtils.getLoginUserId());
        spareIo.setOperatorName(SecurityFrameworkUtils.getLoginUserNickname());
        fillApprovedAuditInfo(spareIo, null);
        if (spareIo.getIoTime() == null) {
            spareIo.setIoTime(LocalDateTime.now());
        }
        applyStockChange(spareIo);
        spareIoMapper.insert(spareIo);
        return spareIo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpareIo(IotSpareIoSaveReqVO updateReqVO) {
        IotSpareIoDO exists = validateSpareIoExists(updateReqVO.getId());
        validateSpareExists(updateReqVO.getSpareId());
        if (Objects.equals(exists.getAuditStatus(), IotSpareIoAuditStatusEnum.APPROVED.getStatus())) {
            // 编辑已生效记录时，先回滚旧库存，再应用新库存，保证库存一致性
            rollbackStockChange(exists);
        }
        IotSpareIoDO updateObj = BeanUtils.toBean(updateReqVO, IotSpareIoDO.class);
        updateObj.setAuditStatus(IotSpareIoAuditStatusEnum.APPROVED.getStatus());
        updateObj.setOperatorUserId(exists.getOperatorUserId());
        updateObj.setOperatorName(exists.getOperatorName());
        if (updateObj.getIoTime() == null) {
            updateObj.setIoTime(exists.getIoTime() != null ? exists.getIoTime() : LocalDateTime.now());
        }
        fillApprovedAuditInfo(updateObj, exists.getAuditRemark());
        applyStockChange(updateObj);
        spareIoMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpareIo(Long id) {
        IotSpareIoDO exists = validateSpareIoExists(id);
        if (Objects.equals(exists.getAuditStatus(), IotSpareIoAuditStatusEnum.APPROVED.getStatus())) {
            rollbackStockChange(exists);
        }
        spareIoMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditSpareIo(IotSpareIoAuditReqVO auditReqVO) {
        IotSpareIoDO spareIo = validateSpareIoExists(auditReqVO.getId());
        String auditStatus = auditReqVO.getAuditStatus();
        if (Objects.equals(auditStatus, IotSpareIoAuditStatusEnum.PENDING.getStatus())) {
            if (!Objects.equals(spareIo.getAuditStatus(), IotSpareIoAuditStatusEnum.APPROVED.getStatus())) {
                throw exception(SPARE_IO_AUDIT_STATUS_NOT_APPROVED);
            }
            rollbackStockChange(spareIo);
            spareIo.setAuditStatus(IotSpareIoAuditStatusEnum.PENDING.getStatus());
            spareIo.setAuditRemark(auditReqVO.getAuditRemark());
            spareIo.setAuditUserId(null);
            spareIo.setAuditUserName(null);
            spareIo.setAuditTime(null);
            spareIoMapper.updateById(spareIo);
            return;
        }
        if (!Objects.equals(spareIo.getAuditStatus(), IotSpareIoAuditStatusEnum.PENDING.getStatus())) {
            throw exception(SPARE_IO_AUDIT_STATUS_NOT_PENDING);
        }
        if (Objects.equals(auditStatus, IotSpareIoAuditStatusEnum.APPROVED.getStatus())) {
            applyStockChange(spareIo);
        }
        spareIo.setAuditStatus(auditStatus);
        spareIo.setAuditRemark(auditReqVO.getAuditRemark());
        spareIo.setAuditUserId(SecurityFrameworkUtils.getLoginUserId());
        spareIo.setAuditUserName(SecurityFrameworkUtils.getLoginUserNickname());
        spareIo.setAuditTime(LocalDateTime.now());
        spareIoMapper.updateById(spareIo);
    }

    @Override
    public IotSpareIoDO getSpareIo(Long id) {
        return spareIoMapper.selectById(id);
    }

    @Override
    public PageResult<IotSpareIoDO> getSpareIoPage(IotSpareIoPageReqVO pageReqVO) {
        return spareIoMapper.selectPage(pageReqVO);
    }

    private IotSpareIoDO validateSpareIoExists(Long id) {
        IotSpareIoDO spareIo = spareIoMapper.selectById(id);
        if (spareIo == null) {
            throw exception(SPARE_IO_NOT_EXISTS);
        }
        return spareIo;
    }

    private IotSpareDO validateSpareExists(Long spareId) {
        IotSpareDO spare = spareMapper.selectById(spareId);
        if (spare == null) {
            throw exception(SPARE_NOT_EXISTS);
        }
        return spare;
    }

    private void applyStockChange(IotSpareIoDO spareIo) {
        IotSpareDO spare = validateSpareExists(spareIo.getSpareId());
        int currentStock = spare.getStockQty() == null ? 0 : spare.getStockQty();
        int qty = spareIo.getIoQty() == null ? 0 : spareIo.getIoQty();
        if (Objects.equals(spareIo.getIoType(), IotSpareIoTypeEnum.OUT.getType())) {
            if (currentStock < qty) {
                throw exception(SPARE_IO_STOCK_NOT_ENOUGH);
            }
            spare.setStockQty(currentStock - qty);
        } else {
            spare.setStockQty(currentStock + qty);
        }
        spareMapper.updateById(spare);
    }

    private void rollbackStockChange(IotSpareIoDO spareIo) {
        IotSpareDO spare = validateSpareExists(spareIo.getSpareId());
        int currentStock = spare.getStockQty() == null ? 0 : spare.getStockQty();
        int qty = spareIo.getIoQty() == null ? 0 : spareIo.getIoQty();
        if (Objects.equals(spareIo.getIoType(), IotSpareIoTypeEnum.OUT.getType())) {
            spare.setStockQty(currentStock + qty);
        } else {
            if (currentStock < qty) {
                throw exception(SPARE_IO_REVERSE_STOCK_NOT_ENOUGH);
            }
            spare.setStockQty(currentStock - qty);
        }
        spareMapper.updateById(spare);
    }

    private void fillApprovedAuditInfo(IotSpareIoDO spareIo, String auditRemark) {
        spareIo.setAuditUserId(SecurityFrameworkUtils.getLoginUserId());
        spareIo.setAuditUserName(SecurityFrameworkUtils.getLoginUserNickname());
        spareIo.setAuditTime(LocalDateTime.now());
        spareIo.setAuditRemark(auditRemark);
    }
}
