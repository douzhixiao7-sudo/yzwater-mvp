package com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.collection.CollectionUtils;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanCreateReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanSubmitReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo.IotMaintenancePlanSpareUsageVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.maintenanceplan.IotMaintenancePlanDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.maintenanceplan.IotMaintenancePlanSpareUsageDO;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.maintenanceplan.IotMaintenancePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 养护计划")
@RestController
@RequestMapping("/iot/maintenance-plan")
@Validated
public class IotMaintenancePlanController {

    @Resource
    private IotMaintenancePlanService maintenancePlanService;
    @Resource
    private IotDeviceService deviceService;

    @GetMapping("/page")
    @Operation(summary = "获得养护计划分页")
    @PreAuthorize("@ss.hasPermission('iot:maintenance-plan:query')")
    public CommonResult<PageResult<IotMaintenancePlanRespVO>> getMaintenancePlanPage(
            @Valid IotMaintenancePlanPageReqVO pageReqVO) {
        PageResult<IotMaintenancePlanDO> pageResult = maintenancePlanService.getMaintenancePlanPage(pageReqVO);
        List<IotMaintenancePlanDO> list = pageResult.getList();
        Map<Long, IotDeviceDO> deviceMap = deviceService.getDeviceMap(
                CollectionUtils.convertList(list, IotMaintenancePlanDO::getDeviceId));
        List<IotMaintenancePlanRespVO> respList = CollectionUtils.convertList(list,
                item -> convertResp(item, deviceMap.get(item.getDeviceId())));
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @PostMapping("/create")
    @Operation(summary = "创建养护计划")
    @PreAuthorize("@ss.hasPermission('iot:maintenance-plan:create')")
    public CommonResult<Long> createMaintenancePlan(@Valid @RequestBody IotMaintenancePlanCreateReqVO createReqVO) {
        return success(maintenancePlanService.createMaintenancePlan(createReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得养护计划详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:maintenance-plan:query')")
    public CommonResult<IotMaintenancePlanRespVO> getMaintenancePlan(@RequestParam("id") Long id) {
        IotMaintenancePlanDO plan = maintenancePlanService.getMaintenancePlan(id);
        IotDeviceDO device = plan != null ? deviceService.getDevice(plan.getDeviceId()) : null;
        return success(convertResp(plan, device));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交养护信息")
    @PreAuthorize("@ss.hasPermission('iot:maintenance-plan:submit')")
    public CommonResult<Boolean> submitMaintenancePlan(@Valid @RequestBody IotMaintenancePlanSubmitReqVO submitReqVO) {
        maintenancePlanService.submitMaintenancePlan(submitReqVO);
        return success(true);
    }

    @PostMapping("/sync-by-device")
    @Operation(summary = "同步设备自动养护计划")
    @PreAuthorize("@ss.hasPermission('iot:maintenance-plan:query')")
    public CommonResult<Boolean> syncAutoMaintenancePlanByDevice(@RequestParam("deviceId") Long deviceId) {
        maintenancePlanService.syncAutoMaintenancePlanByDevice(deviceId);
        return success(true);
    }

    private IotMaintenancePlanRespVO convertResp(IotMaintenancePlanDO plan, IotDeviceDO device) {
        if (plan == null) {
            return null;
        }
        IotMaintenancePlanRespVO respVO = BeanUtils.toBean(plan, IotMaintenancePlanRespVO.class);
        // 显式回填创建时间，避免拷贝工具对父类字段漏拷贝
        respVO.setCreateTime(plan.getCreateTime());
        if (device != null) {
            String deviceName = device.getNickname() != null && !device.getNickname().isBlank()
                    ? device.getNickname() : device.getDeviceName();
            respVO.setDeviceName(deviceName);
            if (device.getDeviceType() != null) {
                respVO.setDeviceType(String.valueOf(device.getDeviceType()));
            } else {
                respVO.setDeviceType(device.getEquipmentModel());
            }
            if (respVO.getStationId() == null) {
                respVO.setStationId(device.getStationId());
            }
        }
        respVO.setSpareUsages(convertSpareUsages(plan.getSpareUsages()));
        return respVO;
    }

    private List<IotMaintenancePlanSpareUsageVO> convertSpareUsages(List<IotMaintenancePlanSpareUsageDO> spareUsages) {
        if (spareUsages == null || spareUsages.isEmpty()) {
            return Collections.emptyList();
        }
        return CollectionUtils.convertList(spareUsages, item -> BeanUtils.toBean(item, IotMaintenancePlanSpareUsageVO.class));
    }
}
