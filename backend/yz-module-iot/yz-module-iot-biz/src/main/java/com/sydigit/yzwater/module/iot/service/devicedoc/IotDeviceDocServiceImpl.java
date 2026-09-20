package com.sydigit.yzwater.module.iot.service.devicedoc;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo.IotDeviceDocPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.devicedoc.vo.IotDeviceDocSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.doc.IotDeviceDocDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.dal.mysql.doc.IotDeviceDocMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_DOC_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;

/**
 * 设备技术资料 Service 实现
 */
@Service
@Validated
public class IotDeviceDocServiceImpl implements IotDeviceDocService {

    @Resource
    private IotDeviceDocMapper deviceDocMapper;
    @Resource
    private IotDeviceMapper deviceMapper;

    @Override
    public Long createDeviceDoc(IotDeviceDocSaveReqVO createReqVO) {
        IotDeviceDO device = validateDeviceExists(createReqVO.getDeviceId());
        IotDeviceDocDO doc = BeanUtils.toBean(createReqVO, IotDeviceDocDO.class);
        fillDeviceSnapshot(doc, device);
        doc.setFileFormat(resolveFileFormat(createReqVO.getFileFormat(), createReqVO.getFileUrl()));
        if (createReqVO.getCreateTime() != null) {
            doc.setCreateTime(createReqVO.getCreateTime());
        }
        deviceDocMapper.insert(doc);
        return doc.getId();
    }

    @Override
    public void updateDeviceDoc(IotDeviceDocSaveReqVO updateReqVO) {
        IotDeviceDocDO existed = validateDeviceDocExists(updateReqVO.getId());
        IotDeviceDO device = validateDeviceExists(updateReqVO.getDeviceId());
        IotDeviceDocDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceDocDO.class);
        fillDeviceSnapshot(updateObj, device);
        updateObj.setFileFormat(resolveFileFormat(updateReqVO.getFileFormat(), updateReqVO.getFileUrl()));
        if (updateReqVO.getCreateTime() == null) {
            updateObj.setCreateTime(existed.getCreateTime());
        }
        deviceDocMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeviceDoc(Long id) {
        validateDeviceDocExists(id);
        deviceDocMapper.deleteById(id);
    }

    @Override
    public IotDeviceDocDO getDeviceDoc(Long id) {
        return deviceDocMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceDocDO> getDeviceDocPage(IotDeviceDocPageReqVO pageReqVO) {
        return deviceDocMapper.selectPage(pageReqVO);
    }

    private IotDeviceDocDO validateDeviceDocExists(Long id) {
        IotDeviceDocDO doc = deviceDocMapper.selectById(id);
        if (doc == null) {
            throw exception(DEVICE_DOC_NOT_EXISTS);
        }
        return doc;
    }

    private IotDeviceDO validateDeviceExists(Long deviceId) {
        IotDeviceDO device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

    private void fillDeviceSnapshot(IotDeviceDocDO doc, IotDeviceDO device) {
        if (doc == null || device == null) {
            return;
        }
        doc.setDeviceCode(device.getSerialNumber());
        doc.setDeviceName(StrUtil.blankToDefault(device.getNickname(), device.getDeviceName()));
        doc.setEquipmentModel(device.getEquipmentModel());
        if (device.getDeviceType() != null) {
            doc.setDeviceType(String.valueOf(device.getDeviceType()));
        } else {
            doc.setDeviceType(null);
        }
    }

    private String resolveFileFormat(String fileFormat, String fileUrl) {
        if (StrUtil.isNotBlank(fileFormat)) {
            return fileFormat;
        }
        if (StrUtil.isBlank(fileUrl)) {
            return null;
        }
        String url = fileUrl;
        int queryIndex = url.indexOf('?');
        if (queryIndex > -1) {
            url = url.substring(0, queryIndex);
        }
        int extIndex = url.lastIndexOf('.');
        if (extIndex < 0 || extIndex == url.length() - 1) {
            return null;
        }
        return url.substring(extIndex + 1).toLowerCase();
    }
}
