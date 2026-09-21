package com.sydigit.yzwater.module.iot.service.spare;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSparePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.spare.vo.spare.IotSpareSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.spare.IotSpareDO;
import com.sydigit.yzwater.module.iot.dal.mysql.spare.IotSpareCheckMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.spare.IotSpareIoMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.spare.IotSpareMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_DELETE_FAIL_HAS_CHECK;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_DELETE_FAIL_HAS_IO;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.SPARE_NOT_EXISTS;

/**
 * 备件台账 Service 实现
 */
@Service
@Validated
public class IotSpareServiceImpl implements IotSpareService {

    @Resource
    private IotSpareMapper spareMapper;
    @Resource
    private IotSpareIoMapper spareIoMapper;
    @Resource
    private IotSpareCheckMapper spareCheckMapper;

    @Override
    public Long createSpare(IotSpareSaveReqVO createReqVO) {
        IotSpareDO spare = BeanUtils.toBean(createReqVO, IotSpareDO.class);
        spare.setSpareImages(convertImages(createReqVO.getSpareImages()));
        spareMapper.insert(spare);
        return spare.getId();
    }

    @Override
    public void updateSpare(IotSpareSaveReqVO updateReqVO) {
        validateSpareExists(updateReqVO.getId());
        IotSpareDO updateObj = BeanUtils.toBean(updateReqVO, IotSpareDO.class);
        updateObj.setSpareImages(convertImages(updateReqVO.getSpareImages()));
        spareMapper.updateById(updateObj);
    }

    @Override
    public void deleteSpare(Long id) {
        validateSpareExists(id);
        if (spareIoMapper.selectCountBySpareId(id) > 0) {
            throw exception(SPARE_DELETE_FAIL_HAS_IO);
        }
        if (spareCheckMapper.selectCountBySpareId(id) > 0) {
            throw exception(SPARE_DELETE_FAIL_HAS_CHECK);
        }
        spareMapper.deleteById(id);
    }

    @Override
    public IotSpareDO validateSpareExists(Long id) {
        IotSpareDO spare = spareMapper.selectById(id);
        if (spare == null) {
            throw exception(SPARE_NOT_EXISTS);
        }
        return spare;
    }

    @Override
    public IotSpareDO getSpare(Long id) {
        return spareMapper.selectById(id);
    }

    @Override
    public IotSpareDO getSpareBasic(Long id) {
        return spareMapper.selectBasicById(id);
    }

    @Override
    public PageResult<IotSpareDO> getSparePage(IotSparePageReqVO pageReqVO) {
        return spareMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotSpareDO> getSpareListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return spareMapper.selectBatchIds(ids);
    }

    @Override
    public List<IotSpareDO> getSpareList() {
        return spareMapper.selectList();
    }

    private String[] convertImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return new String[0];
        }
        return images.toArray(new String[0]);
    }
}
