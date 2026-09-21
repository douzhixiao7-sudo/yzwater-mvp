package com.sydigit.yzwater.module.iot.controller.admin.device;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisPointPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisPointRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.genesis.IotDeviceGenesisPointSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceGenesisPointDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.sydigit.yzwater.module.iot.controller.admin.thingmodel.vo.IotThingModelRespVO;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceGenesisPointService;
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

@Tag(name = "管理后台 - IoT 设备 GENESIS64 点位配置")
@RestController
@RequestMapping("/iot/device-genesis-point")
@Validated
public class IotDeviceGenesisPointController {

    @Resource
    private IotDeviceGenesisPointService genesisPointService;

    @PostMapping("/create")
    @Operation(summary = "创建设备 GENESIS64 点位配置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Long> createDeviceGenesisPoint(@Valid @RequestBody IotDeviceGenesisPointSaveReqVO createReqVO) {
        return success(genesisPointService.createDeviceGenesisPoint(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备 GENESIS64 点位配置")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> updateDeviceGenesisPoint(@Valid @RequestBody IotDeviceGenesisPointSaveReqVO updateReqVO) {
        genesisPointService.updateDeviceGenesisPoint(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备 GENESIS64 点位配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:delete')")
    public CommonResult<Boolean> deleteDeviceGenesisPoint(@RequestParam("id") Long id) {
        genesisPointService.deleteDeviceGenesisPoint(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备 GENESIS64 点位配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<IotDeviceGenesisPointRespVO> getDeviceGenesisPoint(@RequestParam("id") Long id) {
        IotDeviceGenesisPointDO genesisPoint = genesisPointService.getDeviceGenesisPoint(id);
        return success(BeanUtils.toBean(genesisPoint, IotDeviceGenesisPointRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备 GENESIS64 点位配置分页")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<PageResult<IotDeviceGenesisPointRespVO>> getDeviceGenesisPointPage(
            @Valid IotDeviceGenesisPointPageReqVO pageReqVO) {
        PageResult<IotDeviceGenesisPointDO> pageResult = genesisPointService.getDeviceGenesisPointPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDeviceGenesisPointRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得设备 GENESIS64 可配置物模型属性列表")
    @PreAuthorize("@ss.hasPermission('iot:device:query')")
    public CommonResult<List<IotThingModelRespVO>> getDeviceGenesisPointList(
            @Valid IotDeviceGenesisPointPageReqVO reqVO) {
        List<IotThingModelDO> list = genesisPointService.getDeviceGenesisPointAvailableThingModelList(reqVO);
        return success(BeanUtils.toBean(list, IotThingModelRespVO.class));
    }

}
