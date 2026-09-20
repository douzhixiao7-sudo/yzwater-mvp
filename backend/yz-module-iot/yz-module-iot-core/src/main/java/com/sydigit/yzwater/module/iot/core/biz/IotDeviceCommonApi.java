package com.sydigit.yzwater.module.iot.core.biz;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotDeviceRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotGenesisDeviceConfigRespDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigListReqDTO;
import com.sydigit.yzwater.module.iot.core.biz.dto.IotMqttDeviceConfigRespDTO;

import java.util.List;

/**
 * IoT 设备通用 API
 *
 * @author haohao
 */
public interface IotDeviceCommonApi {

    /**
     * 设备认证
     *
     * @param authReqDTO 认证请求
     * @return 认证结果
     */
    CommonResult<Boolean> authDevice(IotDeviceAuthReqDTO authReqDTO);

    /**
     * 获取设备信息
     *
     * @param infoReqDTO 设备信息请求
     * @return 设备信息
     */
    CommonResult<IotDeviceRespDTO> getDevice(IotDeviceGetReqDTO infoReqDTO);

    /**
     * 获取 GENESIS64 设备配置列表
     *
     * @param listReqDTO 查询参数
     * @return 配置列表
     */
    CommonResult<List<IotGenesisDeviceConfigRespDTO>> getGenesisDeviceConfigList(
            IotGenesisDeviceConfigListReqDTO listReqDTO);

    /**
     * 获取 MQTT Source 设备配置列表
     *
     * @param listReqDTO 查询参数
     * @return 配置列表
     */
    CommonResult<List<IotMqttDeviceConfigRespDTO>> getMqttDeviceConfigList(
            IotMqttDeviceConfigListReqDTO listReqDTO);

}
