package com.sydigit.yzwater.module.iot.controller.admin.device;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttConfigRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttConfigSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttConfigDO;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceMqttConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备 MQTT 采集配置")
@RestController
@RequestMapping("/iot/device-mqtt-config")
@Validated
public class IotDeviceMqttConfigController {

    @Resource
    private IotDeviceMqttConfigService deviceMqttConfigService;

    @PostMapping("/save")
    @Operation(summary = "保存设备 MQTT 采集配置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> saveDeviceMqttConfig(@Valid @RequestBody IotDeviceMqttConfigSaveReqVO saveReqVO) {
        deviceMqttConfigService.saveDeviceMqttConfig(saveReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备 MQTT 采集配置")
    @Parameter(name = "id", description = "编号", example = "1024")
    @Parameter(name = "deviceId", description = "设备编号", example = "2048")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceMqttConfigRespVO> getDeviceMqttConfig(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "deviceId", required = false) Long deviceId) {
        IotDeviceMqttConfigDO mqttConfig = null;
        if (id != null) {
            mqttConfig = deviceMqttConfigService.getDeviceMqttConfig(id);
        } else if (deviceId != null) {
            mqttConfig = deviceMqttConfigService.getDeviceMqttConfigByDeviceId(deviceId);
        }
        return success(BeanUtils.toBean(mqttConfig, IotDeviceMqttConfigRespVO.class));
    }

}
