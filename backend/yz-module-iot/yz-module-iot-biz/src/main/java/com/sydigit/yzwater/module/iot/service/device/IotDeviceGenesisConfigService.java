package com.sydigit.yzwater.module.iot.service.device;

import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisConfigSaveReqVO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceGenesisConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 设备 GENESIS64 连接配置 Service 接口
 */
public interface IotDeviceGenesisConfigService {

    void saveDeviceGenesisConfig(@Valid IotDeviceGenesisConfigSaveReqVO saveReqVO);

    IotDeviceGenesisConfigDO getDeviceGenesisConfig(Long id);

    IotDeviceGenesisConfigDO getDeviceGenesisConfigByDeviceId(Long deviceId);

    List<IotDeviceGenesisConfigDO> getDeviceGenesisConfigList(IotGenesisDeviceConfigListReqDTO listReqDTO);

}
