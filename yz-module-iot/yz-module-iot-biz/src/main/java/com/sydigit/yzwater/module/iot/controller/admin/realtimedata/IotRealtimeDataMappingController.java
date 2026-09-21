package com.sydigit.yzwater.module.iot.controller.admin.realtimedata;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchImportReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchImportRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchAddByStationPreviewRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchAddByStationReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchAddByStationRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingImportItemRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingSyncRespVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataMappingDO;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.realtimedata.IotRealtimeDataMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.hutool.core.collection.CollUtil.isNotEmpty;
import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT 实时数据点位映射")
@RestController
@RequestMapping("/iot/realtime-data-mapping")
@Validated
public class IotRealtimeDataMappingController {

    @Resource
    private IotRealtimeDataMappingService mappingService;
    @Resource
    private IotDeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建点位映射")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:create')")
    public CommonResult<Long> createMapping(@Valid @RequestBody IotRealtimeDataMappingSaveReqVO createReqVO) {
        return success(mappingService.createMapping(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新点位映射")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:update')")
    public CommonResult<Boolean> updateMapping(@Valid @RequestBody IotRealtimeDataMappingSaveReqVO updateReqVO) {
        mappingService.updateMapping(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除点位映射")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:delete')")
    public CommonResult<Boolean> deleteMapping(@RequestParam("id") Long id) {
        mappingService.deleteMapping(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除点位映射")
    @Parameter(name = "ids", description = "编号数组", required = true)
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:delete')")
    public CommonResult<Boolean> deleteMappingList(@RequestParam("ids") Collection<Long> ids) {
        mappingService.deleteMappingList(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得点位映射")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:query')")
    public CommonResult<IotRealtimeDataMappingRespVO> getMapping(@RequestParam("id") Long id) {
        IotRealtimeDataMappingDO mapping = mappingService.getMapping(id);
        IotRealtimeDataMappingRespVO respVO = BeanUtils.toBean(mapping, IotRealtimeDataMappingRespVO.class);
        if (mapping != null && mapping.getDeviceId() != null) {
            IotDeviceDO device = deviceService.getDevice(mapping.getDeviceId());
            if (device != null) {
                respVO.setDeviceName(device.getDeviceName());
            }
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得点位映射分页")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:query')")
    public CommonResult<PageResult<IotRealtimeDataMappingRespVO>> getMappingPage(@Valid IotRealtimeDataMappingPageReqVO pageReqVO) {
        PageResult<IotRealtimeDataMappingDO> pageResult = mappingService.getMappingPage(pageReqVO);
        PageResult<IotRealtimeDataMappingRespVO> result = BeanUtils.toBean(pageResult, IotRealtimeDataMappingRespVO.class);
        if (isNotEmpty(pageResult.getList())) {
            Set<Long> deviceIds = convertSet(pageResult.getList(), IotRealtimeDataMappingDO::getDeviceId);
            Map<Long, IotDeviceDO> deviceMap = deviceService.getDeviceMap(deviceIds);
            result.getList().forEach(item -> {
                IotDeviceDO device = deviceMap.get(item.getDeviceId());
                if (device != null) {
                    item.setDeviceName(device.getDeviceName());
                }
            });
        }
        return success(result);
    }

    @GetMapping("/point-name-list")
    @Operation(summary = "按采集源和设备获取已录入点位名称列表")
    @Parameter(name = "sourceId", description = "采集源编号", required = true)
    @Parameter(name = "deviceId", description = "设备编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:query')")
    public CommonResult<java.util.List<String>> getPointNameList(@RequestParam("sourceId") Long sourceId,
                                                                 @RequestParam("deviceId") Long deviceId) {
        return success(mappingService.getPointNameListBySourceAndDevice(sourceId, deviceId));
    }

    @GetMapping("/import-preview")
    @Operation(summary = "获取批量导入预览列表")
    @Parameter(name = "sourceId", description = "采集源编号", required = true)
    @Parameter(name = "deviceId", description = "设备编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:query')")
    public CommonResult<List<IotRealtimeDataMappingImportItemRespVO>> getImportPreview(
            @RequestParam("sourceId") Long sourceId,
            @RequestParam("deviceId") Long deviceId) {
        return success(mappingService.getImportPreview(sourceId, deviceId));
    }

    @GetMapping("/import-preview-by-tag")
    @Operation(summary = "获取批量导入预览列表（仅标签）")
    @Parameter(name = "sourceId", description = "采集源编号", required = true)
    @Parameter(name = "deviceId", description = "设备编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:query')")
    public CommonResult<List<IotRealtimeDataMappingImportItemRespVO>> getImportPreviewByTag(
            @RequestParam("sourceId") Long sourceId,
            @RequestParam("deviceId") Long deviceId) {
        return success(mappingService.getImportPreviewByTag(sourceId, deviceId));
    }

    @PostMapping("/import-batch")
    @Operation(summary = "批量导入点位映射")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:create')")
    public CommonResult<IotRealtimeDataMappingBatchImportRespVO> importBatch(
            @Valid @RequestBody IotRealtimeDataMappingBatchImportReqVO reqVO) {
        return success(mappingService.importBatch(reqVO));
    }

    @PostMapping("/sync-by-device")
    @Operation(summary = "按设备同步点位映射")
    @Parameter(name = "deviceId", description = "设备编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:update')")
    public CommonResult<IotRealtimeDataMappingSyncRespVO> syncMappingByDevice(@RequestParam("deviceId") Long deviceId) {
        return success(mappingService.syncMappingByDevice(deviceId));
    }

    @GetMapping("/batch-add-preview")
    @Operation(summary = "站点设备批量新增点位映射预览")
    @Parameter(name = "sourceId", description = "采集源编号", required = true)
    @Parameter(name = "stationId", description = "站点编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:query')")
    public CommonResult<List<IotRealtimeDataMappingBatchAddByStationPreviewRespVO>> getBatchAddPreview(
            @RequestParam("sourceId") Long sourceId,
            @RequestParam("stationId") String stationId) {
        return success(mappingService.getBatchAddPreview(sourceId, stationId));
    }

    @PostMapping("/batch-add-by-station")
    @Operation(summary = "站点设备批量新增点位映射")
    @PreAuthorize("@ss.hasPermission('iot:realtime-data-mapping:create')")
    public CommonResult<IotRealtimeDataMappingBatchAddByStationRespVO> batchAddByStation(
            @Valid @RequestBody IotRealtimeDataMappingBatchAddByStationReqVO reqVO) {
        return success(mappingService.batchAddByStation(reqVO));
    }

}
