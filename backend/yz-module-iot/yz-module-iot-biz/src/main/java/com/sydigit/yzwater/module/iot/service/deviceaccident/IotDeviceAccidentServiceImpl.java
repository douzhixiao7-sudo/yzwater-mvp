package com.sydigit.yzwater.module.iot.service.deviceaccident;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.security.core.util.SecurityFrameworkUtils;
import com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo.IotDeviceAccidentPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo.IotDeviceAccidentSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.accident.IotDeviceAccidentDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.mysql.accident.IotDeviceAccidentMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_ACCIDENT_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;

/**
 * 设备事故 Service 实现
 */
@Service
@Validated
public class IotDeviceAccidentServiceImpl implements IotDeviceAccidentService {

    @Resource
    private IotDeviceAccidentMapper deviceAccidentMapper;
    @Resource
    private IotDeviceMapper deviceMapper;

    @Override
    public Long createDeviceAccident(IotDeviceAccidentSaveReqVO createReqVO) {
        IotDeviceDO device = validateDeviceExists(createReqVO.getDeviceId());
        IotDeviceAccidentDO accident = BeanUtils.toBean(createReqVO, IotDeviceAccidentDO.class);
        fillAccidentLocation(accident, device);
        accident.setAttachments(convertAttachments(createReqVO.getAttachments()));
        ensureNotNullFields(accident);
        deviceAccidentMapper.insert(accident);
        return accident.getId();
    }

    @Override
    public void updateDeviceAccident(IotDeviceAccidentSaveReqVO updateReqVO) {
        IotDeviceAccidentDO existed = validateDeviceAccidentExists(updateReqVO.getId());
        IotDeviceDO device = validateDeviceExists(updateReqVO.getDeviceId());
        IotDeviceAccidentDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceAccidentDO.class);
        fillAccidentLocation(updateObj, device);
        if (updateReqVO.getAttachments() == null) {
            updateObj.setAttachments(existed.getAttachments());
        } else {
            updateObj.setAttachments(convertAttachments(updateReqVO.getAttachments()));
        }
        if (StrUtil.isBlank(updateObj.getAccidentLocation())) {
            updateObj.setAccidentLocation(existed.getAccidentLocation());
        }
        ensureNotNullFields(updateObj);
        deviceAccidentMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeviceAccident(Long id) {
        validateDeviceAccidentExists(id);
        deviceAccidentMapper.deleteById(id);
    }

    @Override
    public IotDeviceAccidentDO getDeviceAccident(Long id) {
        return deviceAccidentMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceAccidentDO> getDeviceAccidentPage(IotDeviceAccidentPageReqVO pageReqVO) {
        return deviceAccidentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceAccidentDO> getDeviceAccidentList(IotDeviceAccidentPageReqVO pageReqVO) {
        return deviceAccidentMapper.selectList(pageReqVO);
    }

    private IotDeviceAccidentDO validateDeviceAccidentExists(Long id) {
        IotDeviceAccidentDO accident = deviceAccidentMapper.selectById(id);
        if (accident == null) {
            throw exception(DEVICE_ACCIDENT_NOT_EXISTS);
        }
        return accident;
    }

    private IotDeviceDO validateDeviceExists(Long deviceId) {
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    private void fillAccidentLocation(IotDeviceAccidentDO accident, IotDeviceDO device) {
        if (device == null || !StrUtil.isBlank(accident.getAccidentLocation())) {
            return;
        }
        accident.setAccidentLocation(device.getAddress());
    }

    private void ensureNotNullFields(IotDeviceAccidentDO accident) {
        if (StrUtil.isBlank(accident.getHandleResult())) {
            accident.setHandleResult("");
        }
        if (StrUtil.isBlank(accident.getResponsibleName())) {
            String nickname = SecurityFrameworkUtils.getLoginUserNickname();
            if (StrUtil.isNotBlank(nickname)) {
                accident.setResponsibleName(nickname);
            }
        }
    }

    private String[] convertAttachments(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new String[0];
        }
        return attachments.toArray(new String[0]);
    }
}
