package com.sydigit.yzwater.module.iot.controller.admin.device;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttMappingPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttMappingRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.mqtt.IotDeviceMqttMappingSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.thingmodel.vo.IotThingModelRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceMqttMappingDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceMqttMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 设备 MQTT 属性映射")
@RestController
@RequestMapping("/iot/device-mqtt-mapping")
@Validated
public class IotDeviceMqttMappingController {

    @Resource
    private IotDeviceMqttMappingService deviceMqttMappingService;

    @PostMapping("/create")
    @Operation(summary = "创建设备 MQTT 属性映射")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Long> createDeviceMqttMapping(@Valid @RequestBody IotDeviceMqttMappingSaveReqVO createReqVO) {
        return success(deviceMqttMappingService.createDeviceMqttMapping(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备 MQTT 属性映射")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDeviceMqttMapping(@Valid @RequestBody IotDeviceMqttMappingSaveReqVO updateReqVO) {
        deviceMqttMappingService.updateDeviceMqttMapping(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备 MQTT 属性映射")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteDeviceMqttMapping(@RequestParam("id") Long id) {
        deviceMqttMappingService.deleteDeviceMqttMapping(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备 MQTT 属性映射")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceMqttMappingRespVO> getDeviceMqttMapping(@RequestParam("id") Long id) {
        IotDeviceMqttMappingDO mapping = deviceMqttMappingService.getDeviceMqttMapping(id);
        return success(BeanUtils.toBean(mapping, IotDeviceMqttMappingRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备 MQTT 属性映射分页")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<IotDeviceMqttMappingRespVO>> getDeviceMqttMappingPage(
            @Valid IotDeviceMqttMappingPageReqVO pageReqVO) {
        PageResult<IotDeviceMqttMappingDO> pageResult = deviceMqttMappingService.getDeviceMqttMappingPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceMqttMappingRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得设备 MQTT 可配置物模型属性列表")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<IotThingModelRespVO>> getDeviceMqttMappingList(
            @Valid IotDeviceMqttMappingPageReqVO reqVO) {
        List<IotThingModelDO> list = deviceMqttMappingService.getDeviceMqttMappingAvailableThingModelList(reqVO);
        return success(BeanUtils.toBean(list, IotThingModelRespVO.class));
    }

}
