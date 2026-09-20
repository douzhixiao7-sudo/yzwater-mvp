package com.sydigit.yzwater.module.iot.service.deviceaccident;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo.IotDeviceAccidentPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo.IotDeviceAccidentSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.accident.IotDeviceAccidentDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 设备事故 Service 接口
 */
public interface IotDeviceAccidentService {

    Long createDeviceAccident(@Valid IotDeviceAccidentSaveReqVO createReqVO);

    void updateDeviceAccident(@Valid IotDeviceAccidentSaveReqVO updateReqVO);

    void deleteDeviceAccident(Long id);

    IotDeviceAccidentDO getDeviceAccident(Long id);

    PageResult<IotDeviceAccidentDO> getDeviceAccidentPage(IotDeviceAccidentPageReqVO pageReqVO);

    List<IotDeviceAccidentDO> getDeviceAccidentList(IotDeviceAccidentPageReqVO pageReqVO);
}