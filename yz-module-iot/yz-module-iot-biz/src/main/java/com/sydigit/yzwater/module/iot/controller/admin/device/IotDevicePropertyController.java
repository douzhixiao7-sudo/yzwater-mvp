package com.sydigit.yzwater.module.iot.controller.admin.device;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyDetailRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.property.IotDevicePropertyTagSaveReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.sydigit.yzwater.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.device.property.IotDevicePropertyService;
import com.sydigit.yzwater.module.iot.service.device.property.IotDevicePropertyTagService;
import com.sydigit.yzwater.module.iot.service.thingmodel.IotThingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 设备属性")
@RestController
@RequestMapping("/iot/device/property")
@Validated
public class IotDevicePropertyController {

    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    private IotDevicePropertyTagService devicePropertyTagService;
    @Resource
    private IotThingModelService thingModelService;
    @Resource
    private IotDeviceService deviceService;

    @GetMapping("/get-latest")
    @Operation(summary = "获取设备属性最新属性")
    @Parameter(name = "deviceId", description = "设备编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<List<IotDevicePropertyDetailRespVO>> getLatestDeviceProperties(
            @RequestParam("deviceId") Long deviceId) {
        // 1.1 获取设备信息
        IotDeviceDO device = deviceService.getDevice(deviceId);
        Assert.notNull(device, "设备不存在");
        // 1.2 获取设备最新属性
        Map<String, IotDevicePropertyDO> properties = devicePropertyService.getLatestDeviceProperties(deviceId);
        Map<String, String> tagNameMap = devicePropertyTagService.getTagNameMap(deviceId);
        // 1.3 根据 productId + type 查询属性类型的物模型
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());

        // 2. 基于 thingModels 遍历，拼接 properties
        return success(convertList(thingModels, thingModel -> {
            ThingModelProperty thingModelProperty = thingModel.getProperty();
            Assert.notNull(thingModelProperty, "属性不能为空");
            IotDevicePropertyDetailRespVO result = new IotDevicePropertyDetailRespVO()
                    .setName(thingModel.getName()).setDescription(thingModel.getDescription())
                    .setDataType(thingModelProperty.getDataType())
                    .setDataSpecs(thingModelProperty.getDataSpecs())
                    .setDataSpecsList(thingModelProperty.getDataSpecsList());
            result.setIdentifier(thingModel.getIdentifier());
            result.setTagName(tagNameMap.get(thingModel.getIdentifier()));
            IotDevicePropertyDO property = properties.get(thingModel.getIdentifier());
            if (property != null) {
                result.setValue(property.getValue())
                        .setUpdateTime(LocalDateTimeUtil.toEpochMilli(property.getUpdateTime()));
            }
            return result;
        }));
    }

    @PostMapping("/tag/save-batch")
    @Operation(summary = "批量保存设备属性标签")
    @PreAuthorize("@ss.hasPermission('iot:device:update')")
    public CommonResult<Boolean> savePropertyTags(@Valid @RequestBody IotDevicePropertyTagSaveReqVO reqVO) {
        devicePropertyTagService.saveTagNames(reqVO);
        return success(true);
    }

    @GetMapping("/history-list")
    @Operation(summary = "获取设备属性历史数据列表")
    @PreAuthorize("@ss.hasPermission('iot:device:property-query')")
    public CommonResult<List<IotDevicePropertyRespVO>> getHistoryDevicePropertyList(
            @Valid IotDevicePropertyHistoryListReqVO listReqVO) {
        return success(devicePropertyService.getHistoryDevicePropertyList(listReqVO));
    }

}
