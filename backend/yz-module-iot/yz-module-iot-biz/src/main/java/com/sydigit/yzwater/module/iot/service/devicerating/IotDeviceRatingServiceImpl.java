package com.sydigit.yzwater.module.iot.service.devicerating;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo.IotDeviceRatingPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo.IotDeviceRatingSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.rating.IotDeviceRatingDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.rating.IotDeviceRatingMapper;
import com.sydigit.yzwater.module.iot.enums.devicerating.IotDeviceRatingResultEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_RATING_ATTACHMENT_REQUIRED;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_RATING_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_RATING_RECTIFY_REQUIRED;

/**
 * 设备评级 Service 实现
 */
@Service
@Validated
public class IotDeviceRatingServiceImpl implements IotDeviceRatingService {

    @Resource
    private IotDeviceRatingMapper deviceRatingMapper;
    @Resource
    private IotDeviceMapper deviceMapper;

    @Override
    public Long createDeviceRating(IotDeviceRatingSaveReqVO createReqVO) {
        validateDeviceExists(createReqVO.getDeviceId());
        validateUnqualifiedRequirement(createReqVO);
        IotDeviceRatingDO rating = BeanUtils.toBean(createReqVO, IotDeviceRatingDO.class);
        rating.setAttachments(convertAttachments(createReqVO.getAttachments()));
        fillRatingUser(rating, createReqVO);
        deviceRatingMapper.insert(rating);
        return rating.getId();
    }

    @Override
    public void updateDeviceRating(IotDeviceRatingSaveReqVO updateReqVO) {
        IotDeviceRatingDO existed = validateDeviceRatingExists(updateReqVO.getId());
        validateDeviceExists(updateReqVO.getDeviceId());
        validateUnqualifiedRequirement(updateReqVO);
        IotDeviceRatingDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceRatingDO.class);
        updateObj.setAttachments(convertAttachments(updateReqVO.getAttachments()));
        fillRatingUser(updateObj, updateReqVO);
        if (updateObj.getRatingTime() == null) {
            updateObj.setRatingTime(existed.getRatingTime());
        }
        if (StrUtil.isBlank(updateObj.getRatingBasis())) {
            updateObj.setRatingBasis(existed.getRatingBasis());
        }
        if (StrUtil.isBlank(updateObj.getRatingResult())) {
            updateObj.setRatingResult(existed.getRatingResult());
        }
        deviceRatingMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeviceRating(Long id) {
        validateDeviceRatingExists(id);
        deviceRatingMapper.deleteById(id);
    }

    @Override
    public IotDeviceRatingDO getDeviceRating(Long id) {
        return deviceRatingMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceRatingDO> getDeviceRatingPage(IotDeviceRatingPageReqVO pageReqVO) {
        return deviceRatingMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceRatingDO> getDeviceRatingList(IotDeviceRatingPageReqVO pageReqVO) {
        return deviceRatingMapper.selectList(pageReqVO);
    }

    private void validateUnqualifiedRequirement(IotDeviceRatingSaveReqVO reqVO) {
        if (!isUnqualified(reqVO.getRatingResult())) {
            return;
        }
        if (StrUtil.isBlank(reqVO.getRectifyAdvice()) || reqVO.getRectifyDeadline() == null) {
            throw exception(DEVICE_RATING_RECTIFY_REQUIRED);
        }
        if (CollUtil.isEmpty(reqVO.getAttachments())) {
            throw exception(DEVICE_RATING_ATTACHMENT_REQUIRED);
        }
    }

    private boolean isUnqualified(String ratingResult) {
        return StrUtil.equals(ratingResult, IotDeviceRatingResultEnum.UNQUALIFIED.getResult());
    }

    private void fillRatingUser(IotDeviceRatingDO rating, IotDeviceRatingSaveReqVO reqVO) {
        if (rating.getRatingUserId() == null) {
            rating.setRatingUserId(SecurityFrameworkUtils.getLoginUserId());
        }
        if (StrUtil.isBlank(rating.getRatingUserName())) {
            rating.setRatingUserName(SecurityFrameworkUtils.getLoginUserNickname());
        }
    }

    private IotDeviceRatingDO validateDeviceRatingExists(Long id) {
        IotDeviceRatingDO rating = deviceRatingMapper.selectById(id);
        if (rating == null) {
            throw exception(DEVICE_RATING_NOT_EXISTS);
        }
        return rating;
    }

    private IotDeviceDO validateDeviceExists(Long deviceId) {
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    private String[] convertAttachments(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new String[0];
        }
        return attachments.toArray(new String[0]);
    }
}
