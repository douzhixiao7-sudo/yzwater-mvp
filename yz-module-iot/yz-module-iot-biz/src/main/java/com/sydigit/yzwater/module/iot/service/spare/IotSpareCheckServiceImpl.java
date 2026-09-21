package com.sydigit.yzwater.module.iot.service.spare;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.check.IotSpareCheckUpdateReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareCheckDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareDO;
import com.sydigit.yzwater.module.iot.dal.mysql.spare.IotSpareCheckMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.spare.IotSpareMapper;
import com.sydigit.yzwater.module.iot.enums.spare.IotSpareCheckResultEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_CHECK_ALREADY_APPLIED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_CHECK_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_CHECK_NOT_APPLIED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_NOT_EXISTS;

/**
 * 备件盘点 Service 实现
 */
@Service
@Validated
public class IotSpareCheckServiceImpl implements IotSpareCheckService {

    @Resource
    private IotSpareCheckMapper spareCheckMapper;
    @Resource
    private IotSpareMapper spareMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCheck(IotSpareCheckSaveReqVO createReqVO) {
        IotSpareDO spare = validateSpareExists(createReqVO.getSpareId());
        int systemQty = spare.getStockQty() == null ? 0 : spare.getStockQty();
        int actualQty = createReqVO.getActualQty();
        int diffQty = actualQty - systemQty;
        String result = buildResult(diffQty);

        IotSpareCheckDO check = BeanUtils.toBean(createReqVO, IotSpareCheckDO.class);
        check.setCheckTime(createReqVO.getCheckTime() != null ? createReqVO.getCheckTime() : LocalDateTime.now());
        check.setSystemQty(systemQty);
        check.setDiffQty(diffQty);
        check.setResultStatus(result);
        check.setCheckerUserId(SecurityFrameworkUtils.getLoginUserId());
        check.setCheckerUserName(SecurityFrameworkUtils.getLoginUserNickname());

        check.setApplied(true);
        fillApplyInfo(check);
        spareCheckMapper.insert(check);
        applyStock(spare, actualQty);
        return check.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheck(IotSpareCheckUpdateReqVO updateReqVO) {
        IotSpareCheckDO exists = validateCheckExists(updateReqVO.getId());
        if (Boolean.TRUE.equals(exists.getApplied())) {
            // 编辑已生效盘点记录时，先恢复旧库存，再应用新结果，避免库存重复覆盖
            IotSpareDO previousSpare = validateSpareExists(exists.getSpareId());
            previousSpare.setStockQty(exists.getSystemQty());
            spareMapper.updateById(previousSpare);
        }
        IotSpareDO spare = validateSpareExists(updateReqVO.getSpareId());
        int systemQty = spare.getStockQty() == null ? 0 : spare.getStockQty();
        int actualQty = updateReqVO.getActualQty();
        int diffQty = actualQty - systemQty;
        String result = buildResult(diffQty);

        IotSpareCheckDO update = BeanUtils.toBean(updateReqVO, IotSpareCheckDO.class);
        update.setCheckTime(updateReqVO.getCheckTime() != null ? updateReqVO.getCheckTime() : exists.getCheckTime());
        update.setSystemQty(systemQty);
        update.setDiffQty(diffQty);
        update.setResultStatus(result);
        update.setCheckerUserId(SecurityFrameworkUtils.getLoginUserId());
        update.setCheckerUserName(SecurityFrameworkUtils.getLoginUserNickname());
        update.setApplied(true);
        fillApplyInfo(update);
        spareCheckMapper.updateById(update);
        applyStock(spare, actualQty);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCheck(Long id) {
        IotSpareCheckDO check = validateCheckExists(id);
        if (Boolean.TRUE.equals(check.getApplied())) {
            IotSpareDO spare = validateSpareExists(check.getSpareId());
            spare.setStockQty(check.getSystemQty());
            spareMapper.updateById(spare);
        }
        spareCheckMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyCheckResult(Long id) {
        IotSpareCheckDO check = validateCheckExists(id);
        if (Boolean.TRUE.equals(check.getApplied())) {
            throw exception(SPARE_CHECK_ALREADY_APPLIED);
        }
        IotSpareDO spare = validateSpareExists(check.getSpareId());
        applyStock(spare, check.getActualQty());
        fillApplyInfo(check);
        check.setApplied(true);
        spareCheckMapper.updateById(check);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reverseApplyCheckResult(Long id) {
        IotSpareCheckDO check = validateCheckExists(id);
        if (!Boolean.TRUE.equals(check.getApplied())) {
            throw exception(SPARE_CHECK_NOT_APPLIED);
        }
        IotSpareDO spare = validateSpareExists(check.getSpareId());
        spare.setStockQty(check.getSystemQty());
        spareMapper.updateById(spare);
        check.setApplied(false);
        check.setApplyUserId(null);
        check.setApplyUserName(null);
        check.setApplyTime(null);
        spareCheckMapper.updateById(check);
    }

    @Override
    public IotSpareCheckDO getCheck(Long id) {
        return spareCheckMapper.selectById(id);
    }

    @Override
    public PageResult<IotSpareCheckDO> getCheckPage(IotSpareCheckPageReqVO pageReqVO) {
        return spareCheckMapper.selectPage(pageReqVO);
    }

    private IotSpareCheckDO validateCheckExists(Long id) {
        IotSpareCheckDO check = spareCheckMapper.selectById(id);
        if (check == null) {
            throw exception(SPARE_CHECK_NOT_EXISTS);
        }
        return check;
    }

    private IotSpareDO validateSpareExists(Long spareId) {
        IotSpareDO spare = spareMapper.selectById(spareId);
        if (spare == null) {
            throw exception(SPARE_NOT_EXISTS);
        }
        return spare;
    }

    private String buildResult(int diffQty) {
        if (diffQty == 0) {
            return IotSpareCheckResultEnum.NORMAL.getResult();
        }
        if (diffQty > 0) {
            return IotSpareCheckResultEnum.OVER.getResult();
        }
        return IotSpareCheckResultEnum.SHORT.getResult();
    }

    private void applyStock(IotSpareDO spare, Integer actualQty) {
        spare.setStockQty(actualQty);
        spareMapper.updateById(spare);
    }

    private void fillApplyInfo(IotSpareCheckDO check) {
        check.setApplyUserId(SecurityFrameworkUtils.getLoginUserId());
        check.setApplyUserName(SecurityFrameworkUtils.getLoginUserNickname());
        check.setApplyTime(LocalDateTime.now());
    }
}
