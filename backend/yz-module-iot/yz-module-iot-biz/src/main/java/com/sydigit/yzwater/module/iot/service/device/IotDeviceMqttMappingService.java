package com.sydigit.yzwater.module.iot.service.device;

import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttMappingPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttMappingSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttMappingDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IoT 设备 MQTT 属性映射 Service 接口
 */
public interface IotDeviceMqttMappingService {

    Long createDeviceMqttMapping(@Valid IotDeviceMqttMappingSaveReqVO createReqVO);

    void updateDeviceMqttMapping(@Valid IotDeviceMqttMappingSaveReqVO updateReqVO);

    void deleteDeviceMqttMapping(Long id);

    IotDeviceMqttMappingDO getDeviceMqttMapping(Long id);

    PageResult<IotDeviceMqttMappingDO> getDeviceMqttMappingPage(IotDeviceMqttMappingPageReqVO pageReqVO);

    List<IotThingModelDO> getDeviceMqttMappingAvailableThingModelList(IotDeviceMqttMappingPageReqVO reqVO);

    Map<Long, List<IotDeviceMqttMappingDO>> getEnabledDeviceMqttMappingMapByDeviceIds(Collection<Long> deviceIds);

    void updateDeviceMqttMappingByThingModel(Long thingModelId, String identifier, String name, String valueType);

    void disableDeviceMqttMappings(Long productId, String identifier);

}
