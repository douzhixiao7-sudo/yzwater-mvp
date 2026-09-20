package com.sydigit.yzwater.module.iot.service.device;

import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttConfigSaveReqVO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 设备 MQTT 配置 Service 接口
 */
public interface IotDeviceMqttConfigService {

    void saveDeviceMqttConfig(@Valid IotDeviceMqttConfigSaveReqVO saveReqVO);

    IotDeviceMqttConfigDO getDeviceMqttConfig(Long id);

    IotDeviceMqttConfigDO getDeviceMqttConfigByDeviceId(Long deviceId);

    List<IotDeviceMqttConfigDO> getDeviceMqttConfigList(IotMqttDeviceConfigListReqDTO listReqDTO);

}
