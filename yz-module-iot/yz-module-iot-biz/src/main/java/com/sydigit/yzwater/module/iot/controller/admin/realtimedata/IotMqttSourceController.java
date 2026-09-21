package com.sydigit.yzwater.module.iot.controller.admin.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource.IotMqttSourcePageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource.IotMqttSourceRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource.IotMqttSourceSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mqttsource.IotMqttSourceSimpleRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotMqttSourceDO;
import com.sydigit.yzwater.module.iot.service.realtimedata.IotMqttSourceService;
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

@Tag(name = "管理后台 - IoT MQTT 数据源")
@RestController
@RequestMapping("/iot/mqtt-source")
@Validated
public class IotMqttSourceController {

    @Resource
    private IotMqttSourceService mqttSourceService;

    @PostMapping("/create")
    @Operation(summary = "创建 MQTT 数据源")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:create')")
    public CommonResult<Long> createSource(@Valid @RequestBody IotMqttSourceSaveReqVO createReqVO) {
        return success(mqttSourceService.createSource(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 MQTT 数据源")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:update')")
    public CommonResult<Boolean> updateSource(@Valid @RequestBody IotMqttSourceSaveReqVO updateReqVO) {
        mqttSourceService.updateSource(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 MQTT 数据源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:delete')")
    public CommonResult<Boolean> deleteSource(@RequestParam("id") Long id) {
        mqttSourceService.deleteSource(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 MQTT 数据源")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:query')")
    public CommonResult<IotMqttSourceRespVO> getSource(@RequestParam("id") Long id) {
        IotMqttSourceDO source = mqttSourceService.getSource(id);
        return success(BeanUtils.toBean(source, IotMqttSourceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 MQTT 数据源分页")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-source:query')")
    public CommonResult<PageResult<IotMqttSourceRespVO>> getSourcePage(@Valid IotMqttSourcePageReqVO pageReqVO) {
        PageResult<IotMqttSourceDO> pageResult = mqttSourceService.getSourcePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotMqttSourceRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得 MQTT 数据源精简列表")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<IotMqttSourceSimpleRespVO>> getSourceSimpleList(
            @RequestParam(value = "enabled", required = false) Boolean enabled) {
        return success(BeanUtils.toBean(mqttSourceService.getSourceList(enabled), IotMqttSourceSimpleRespVO.class));
    }

}
