package com.sydigit.yzwater.module.iot.service.device;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisPointPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisPointSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceGenesisPointDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IoT 设备 GENESIS64 点位配置 Service 接口
 */
public interface IotDeviceGenesisPointService {

    Long createDeviceGenesisPoint(@Valid IotDeviceGenesisPointSaveReqVO createReqVO);

    void updateDeviceGenesisPoint(@Valid IotDeviceGenesisPointSaveReqVO updateReqVO);

    void deleteDeviceGenesisPoint(Long id);

    IotDeviceGenesisPointDO getDeviceGenesisPoint(Long id);

    PageResult<IotDeviceGenesisPointDO> getDeviceGenesisPointPage(IotDeviceGenesisPointPageReqVO pageReqVO);

    List<IotThingModelDO> getDeviceGenesisPointAvailableThingModelList(IotDeviceGenesisPointPageReqVO reqVO);

    void updateDeviceGenesisPointByThingModel(Long thingModelId, String identifier, String name);

    Map<Long, List<IotDeviceGenesisPointDO>> getEnabledDeviceGenesisPointMapByDeviceIds(Collection<Long> deviceIds);

}
