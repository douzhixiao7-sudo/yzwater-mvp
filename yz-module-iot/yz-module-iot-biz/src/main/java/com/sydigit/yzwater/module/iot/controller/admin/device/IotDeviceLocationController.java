package com.sydigit.yzwater.module.iot.controller.admin.device;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.location.IotDeviceLocationNodeRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.location.IotDeviceLocationRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.location.IotDeviceLocationSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceLocationDO;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceLocationService;
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

@Tag(name = "管理后台 - IoT 设备位置")
@RestController
@RequestMapping("/iot/device-location")
@Validated
public class IotDeviceLocationController {

    @Resource
    private IotDeviceLocationService locationService;

    @GetMapping("/tree")
    @Operation(summary = "获得位置树")
    @PreAuthorize("@ss.hasPermission('iot:device-location:query')")
    public CommonResult<List<IotDeviceLocationNodeRespVO>> getLocationTree() {
        return success(BeanUtils.toBean(locationService.getLocationTree(), IotDeviceLocationNodeRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建位置")
    @PreAuthorize("@ss.hasPermission('iot:device-location:create')")
    public CommonResult<Long> createLocation(@Valid @RequestBody IotDeviceLocationSaveReqVO createReqVO) {
        return success(locationService.createLocation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新位置")
    @PreAuthorize("@ss.hasPermission('iot:device-location:update')")
    public CommonResult<Boolean> updateLocation(@Valid @RequestBody IotDeviceLocationSaveReqVO updateReqVO) {
        locationService.updateLocation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除位置")
    @Parameter(name = "id", description = "位置编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-location:delete')")
    public CommonResult<Boolean> deleteLocation(@RequestParam("id") Long id) {
        locationService.deleteLocation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得位置详情")
    @Parameter(name = "id", description = "位置编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:device-location:query')")
    public CommonResult<IotDeviceLocationRespVO> getLocation(@RequestParam("id") Long id) {
        IotDeviceLocationDO location = locationService.getLocation(id);
        return success(BeanUtils.toBean(location, IotDeviceLocationRespVO.class));
    }

}
