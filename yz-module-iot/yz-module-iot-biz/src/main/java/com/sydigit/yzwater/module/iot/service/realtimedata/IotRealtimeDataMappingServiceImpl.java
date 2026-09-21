package com.sydigit.yzwater.module.iot.service.realtimedata;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.common.pojo.PageResult;
import com.sydigit.yzwater.framework.common.pojo.PageParam;
import com.sydigit.yzwater.framework.common.util.object.BeanUtils;
import com.sydigit.yzwater.framework.tenant.core.aop.TenantIgnore;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchAddByStationPreviewRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchAddByStationReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchAddByStationRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchImportReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingBatchImportRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingImportItemReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingImportItemRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingPageReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingSaveReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.realtimedata.vo.mapping.IotRealtimeDataMappingSyncRespVO;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.device.IotDevicePageReqVO;
import com.sydigit.yzwater.module.iot.dal.dataobject.realtimedata.IotRealtimeDataMappingDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import com.sydigit.yzwater.module.iot.dal.mysql.realtimedata.IotRealtimeDataMappingMapper;
import com.sydigit.yzwater.module.iot.service.device.IotDeviceService;
import com.sydigit.yzwater.module.iot.service.device.property.IotDevicePropertyTagService;
import com.sydigit.yzwater.module.iot.service.thingmodel.IotThingModelService;
import com.sydigit.yzwater.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sydigit.yzwater.framework.common.util.collection.CollectionUtils.convertSet;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.DEVICE_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.REALTIME_DATA_MAPPING_NOT_EXISTS;
import static com.sydigit.yzwater.module.iot.enums.ErrorCodeConstants.REALTIME_DATA_MAPPING_POINT_EXISTS;

/**
 * IoT 实时数据点位映射 Service 实现类
 */
@Service
@Validated
public class IotRealtimeDataMappingServiceImpl implements IotRealtimeDataMappingService {

    @Resource
    private IotRealtimeDataMappingMapper mappingMapper;
    @Resource
    private IotRealtimeDataSourceService sourceService;
    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotDevicePropertyTagService devicePropertyTagService;
    @Resource
    private IotThingModelService thingModelService;

    @Override
    public Long createMapping(IotRealtimeDataMappingSaveReqVO createReqVO) {
        sourceService.validateSourceExists(createReqVO.getSourceId());
        createReqVO.setPointName(StrUtil.trimToNull(createReqVO.getPointName()));
        createReqVO.setIdentifier(StrUtil.trimToNull(createReqVO.getIdentifier()));
        validatePointUnique(createReqVO.getSourceId(), createReqVO.getDeviceId(), createReqVO.getPointName(), null);
        IotRealtimeDataMappingDO mapping = BeanUtils.toBean(createReqVO, IotRealtimeDataMappingDO.class);
        if (mapping.getSort() == null) {
            mapping.setSort(0);
        }
        mappingMapper.insert(mapping);
        return mapping.getId();
    }

    @Override
    public void updateMapping(IotRealtimeDataMappingSaveReqVO updateReqVO) {
        validateMappingExists(updateReqVO.getId());
        sourceService.validateSourceExists(updateReqVO.getSourceId());
        updateReqVO.setPointName(StrUtil.trimToNull(updateReqVO.getPointName()));
        updateReqVO.setIdentifier(StrUtil.trimToNull(updateReqVO.getIdentifier()));
        validatePointUnique(updateReqVO.getSourceId(), updateReqVO.getDeviceId(), updateReqVO.getPointName(),
                updateReqVO.getId());
        IotRealtimeDataMappingDO updateObj = BeanUtils.toBean(updateReqVO, IotRealtimeDataMappingDO.class);
        if (updateObj.getSort() == null) {
            updateObj.setSort(0);
        }
        mappingMapper.updateById(updateObj);
    }

    @Override
    public void deleteMapping(Long id) {
        validateMappingExists(id);
        mappingMapper.deleteById(id);
    }

    @Override
    public void deleteMappingList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        mappingMapper.deleteBatch(IotRealtimeDataMappingDO::getId, ids);
    }

    @Override
    public IotRealtimeDataMappingDO getMapping(Long id) {
        return mappingMapper.selectById(id);
    }

    @Override
    public PageResult<IotRealtimeDataMappingDO> getMappingPage(IotRealtimeDataMappingPageReqVO pageReqVO) {
        if (StrUtil.isBlank(pageReqVO.getDeviceName())) {
            return mappingMapper.selectPage(pageReqVO);
        }
        IotDevicePageReqVO devicePageReqVO = new IotDevicePageReqVO();
        devicePageReqVO.setDeviceName(pageReqVO.getDeviceName());
        devicePageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<IotDeviceDO> devicePageResult = deviceService.getDevicePage(devicePageReqVO);
        if (CollUtil.isEmpty(devicePageResult.getList())) {
            return PageResult.empty();
        }
        Set<Long> deviceIds = convertSet(devicePageResult.getList(), IotDeviceDO::getId);
        return mappingMapper.selectPage(pageReqVO, deviceIds);
    }

    @Override
    @TenantIgnore
    public List<IotRealtimeDataMappingDO> getMappingListBySourceIdAndEnabledForJob(Long sourceId, Boolean enabled) {
        return mappingMapper.selectListBySourceIdAndEnabled(sourceId, enabled);
    }

    @Override
    public IotRealtimeDataMappingDO validateMappingExists(Long id) {
        IotRealtimeDataMappingDO mapping = mappingMapper.selectById(id);
        if (mapping == null) {
            throw exception(REALTIME_DATA_MAPPING_NOT_EXISTS);
        }
        return mapping;
    }

    @Override
    public IotRealtimeDataMappingSyncRespVO syncMappingByDevice(Long deviceId) {
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        List<IotRealtimeDataMappingDO> mappings = mappingMapper.selectListByDeviceId(deviceId);
        Map<String, String> identifierLabelMap = buildIdentifierLabelMap(device.getProductId(), deviceId);
        LocalDateTime now = LocalDateTime.now();

        int totalCount = mappings.size();
        int updatedCount = 0;
        int disabledCount = 0;
        int invalidCount = 0;
        int unchangedCount = 0;

        for (IotRealtimeDataMappingDO mapping : mappings) {
            if (mapping == null) {
                continue;
            }
            String identifier = StrUtil.trimToNull(mapping.getIdentifier());
            IotRealtimeDataMappingDO update = new IotRealtimeDataMappingDO();
            update.setId(mapping.getId());
            update.setLastSyncTime(now);
            if (StrUtil.isBlank(identifier)) {
                invalidCount++;
                update.setEnabled(Boolean.FALSE);
                update.setSyncStatus("INVALID");
                update.setSyncMessage("标识符为空，无法匹配物模型");
                mappingMapper.updateById(update);
                continue;
            }

            String latestLabel = identifierLabelMap.get(identifier);
            if (StrUtil.isBlank(latestLabel)) {
                disabledCount++;
                update.setEnabled(Boolean.FALSE);
                update.setSyncStatus("NOT_FOUND");
                update.setSyncMessage("物模型已删除");
                mappingMapper.updateById(update);
                continue;
            }

            String currentPointName = StrUtil.trimToEmpty(mapping.getPointName());
            if (!StrUtil.equals(currentPointName, latestLabel)) {
                updatedCount++;
                update.setPointName(latestLabel);
                update.setSyncStatus("UPDATED");
                update.setSyncMessage("点位名称已同步");
                mappingMapper.updateById(update);
                continue;
            }

            unchangedCount++;
            update.setSyncStatus("NORMAL");
            update.setSyncMessage("无变化");
            mappingMapper.updateById(update);
        }

        IotRealtimeDataMappingSyncRespVO respVO = new IotRealtimeDataMappingSyncRespVO();
        respVO.setDeviceId(deviceId);
        respVO.setTotalCount(totalCount);
        respVO.setUpdatedCount(updatedCount);
        respVO.setDisabledCount(disabledCount);
        respVO.setInvalidCount(invalidCount);
        respVO.setUnchangedCount(unchangedCount);
        return respVO;
    }

    @Override
    public List<String> getPointNameListBySourceAndDevice(Long sourceId, Long deviceId) {
        if (sourceId == null || deviceId == null) {
            return List.of();
        }
        List<IotRealtimeDataMappingDO> mappings = mappingMapper.selectListBySourceIdAndDeviceId(sourceId, deviceId);
        if (CollUtil.isEmpty(mappings)) {
            return List.of();
        }
        List<String> result = new java.util.ArrayList<>(mappings.size());
        for (IotRealtimeDataMappingDO mapping : mappings) {
            if (mapping == null) {
                continue;
            }
            String pointName = StrUtil.trimToNull(mapping.getPointName());
            if (pointName != null) {
                result.add(pointName);
            }
        }
        return result;
    }

    @Override
    public List<IotRealtimeDataMappingImportItemRespVO> getImportPreview(Long sourceId, Long deviceId) {
        if (sourceId == null || deviceId == null) {
            return List.of();
        }
        sourceService.validateSourceExists(sourceId);
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
        Map<String, String> tagNameMap = devicePropertyTagService.getTagNameMap(deviceId);
        if (CollUtil.isEmpty(thingModels)) {
            return List.of();
        }
        Set<String> usedPointNames = loadExistingPointNameSet(sourceId, deviceId);
        Set<String> added = new HashSet<>();
        List<IotRealtimeDataMappingImportItemRespVO> result = new ArrayList<>();
        for (IotThingModelDO model : thingModels) {
            if (model == null) {
                continue;
            }
            String identifier = StrUtil.trimToNull(model.getIdentifier());
            if (identifier == null) {
                continue;
            }
            String pointName = StrUtil.trimToNull(tagNameMap.get(identifier));
            if (pointName == null) {
                pointName = StrUtil.trimToNull(model.getDescription());
            }
            if (pointName == null) {
                pointName = StrUtil.trimToNull(model.getName());
            }
            if (pointName == null) {
                pointName = identifier;
            }
            if (usedPointNames.contains(pointName)) {
                continue;
            }
            if (!added.add(pointName)) {
                continue;
            }
            IotRealtimeDataMappingImportItemRespVO item = new IotRealtimeDataMappingImportItemRespVO();
            item.setPointName(pointName);
            item.setIdentifier(identifier);
            result.add(item);
        }
        return result;
    }

    @Override
    public List<IotRealtimeDataMappingImportItemRespVO> getImportPreviewByTag(Long sourceId, Long deviceId) {
        if (sourceId == null || deviceId == null) {
            return List.of();
        }
        sourceService.validateSourceExists(sourceId);
        IotDeviceDO device = deviceService.getDevice(deviceId);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
        Map<String, String> tagNameMap = devicePropertyTagService.getTagNameMap(deviceId);
        if (CollUtil.isEmpty(thingModels) || CollUtil.isEmpty(tagNameMap)) {
            return List.of();
        }
        Set<String> usedPointNames = loadExistingPointNameSet(sourceId, deviceId);
        Set<String> added = new HashSet<>();
        List<IotRealtimeDataMappingImportItemRespVO> result = new ArrayList<>();
        for (IotThingModelDO model : thingModels) {
            if (model == null) {
                continue;
            }
            String identifier = StrUtil.trimToNull(model.getIdentifier());
            if (identifier == null) {
                continue;
            }
            String pointName = StrUtil.trimToNull(tagNameMap.get(identifier));
            if (pointName == null) {
                continue;
            }
            if (usedPointNames.contains(pointName)) {
                continue;
            }
            if (!added.add(pointName)) {
                continue;
            }
            IotRealtimeDataMappingImportItemRespVO item = new IotRealtimeDataMappingImportItemRespVO();
            item.setPointName(pointName);
            item.setIdentifier(identifier);
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotRealtimeDataMappingBatchImportRespVO importBatch(IotRealtimeDataMappingBatchImportReqVO reqVO) {
        if (reqVO == null || CollUtil.isEmpty(reqVO.getItems())) {
            IotRealtimeDataMappingBatchImportRespVO empty = new IotRealtimeDataMappingBatchImportRespVO();
            empty.setTotalCount(0);
            empty.setImportedCount(0);
            empty.setSkippedCount(0);
            return empty;
        }
        sourceService.validateSourceExists(reqVO.getSourceId());
        IotDeviceDO device = deviceService.getDevice(reqVO.getDeviceId());
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        Map<String, String> identifierLabelMap = buildIdentifierLabelMap(device.getProductId(), reqVO.getDeviceId());
        Set<String> usedPointNames = loadExistingPointNameSet(reqVO.getSourceId(), reqVO.getDeviceId());
        Set<String> added = new HashSet<>();
        List<IotRealtimeDataMappingDO> toSave = new ArrayList<>();
        for (IotRealtimeDataMappingImportItemReqVO item : reqVO.getItems()) {
            if (item == null) {
                continue;
            }
            String identifier = StrUtil.trimToNull(item.getIdentifier());
            if (identifier == null || !identifierLabelMap.containsKey(identifier)) {
                continue;
            }
            String pointName = StrUtil.trimToNull(item.getPointName());
            if (pointName == null) {
                pointName = identifierLabelMap.get(identifier);
            }
            if (pointName == null) {
                continue;
            }
            // 去重：已存在或重复的点位名称不再导入
            if (usedPointNames.contains(pointName) || !added.add(pointName)) {
                continue;
            }
            IotRealtimeDataMappingDO mapping = new IotRealtimeDataMappingDO();
            mapping.setSourceId(reqVO.getSourceId());
            mapping.setDeviceId(reqVO.getDeviceId());
            mapping.setPointName(pointName);
            mapping.setIdentifier(identifier);
            mapping.setEnabled(Boolean.TRUE);
            mapping.setSort(0);
            toSave.add(mapping);
        }
        if (CollUtil.isNotEmpty(toSave)) {
            mappingMapper.insertBatch(toSave);
        }
        int totalCount = reqVO.getItems().size();
        int importedCount = toSave.size();
        IotRealtimeDataMappingBatchImportRespVO respVO = new IotRealtimeDataMappingBatchImportRespVO();
        respVO.setTotalCount(totalCount);
        respVO.setImportedCount(importedCount);
        respVO.setSkippedCount(totalCount - importedCount);
        return respVO;
    }

    @Override
    public List<IotRealtimeDataMappingBatchAddByStationPreviewRespVO> getBatchAddPreview(Long sourceId, String stationId) {
        if (sourceId == null || StrUtil.isBlank(stationId)) {
            return List.of();
        }
        sourceService.validateSourceExists(sourceId);
        List<IotDeviceDO> devices = getDeviceListByStationId(stationId);
        if (CollUtil.isEmpty(devices)) {
            return List.of();
        }

        List<IotRealtimeDataMappingDO> existingMappings = mappingMapper.selectListBySourceId(sourceId);
        Map<Long, List<IotRealtimeDataMappingDO>> mappingByDevice = existingMappings.stream()
                .filter(mapping -> mapping.getDeviceId() != null)
                .collect(Collectors.groupingBy(IotRealtimeDataMappingDO::getDeviceId));
        Set<String> usedPointNames = collectPointNames(existingMappings);

        List<IotRealtimeDataMappingBatchAddByStationPreviewRespVO> result = new ArrayList<>();
        for (IotDeviceDO device : devices) {
            if (device == null) {
                continue;
            }
            List<IotRealtimeDataMappingDO> deviceMappings = mappingByDevice.getOrDefault(device.getId(), List.of());
            Set<String> devicePointNames = collectPointNames(deviceMappings);
            usedPointNames.removeAll(devicePointNames);

            List<IotRealtimeDataMappingImportItemRespVO> items = buildImportItems(device, false, usedPointNames);
            IotRealtimeDataMappingBatchAddByStationPreviewRespVO item = new IotRealtimeDataMappingBatchAddByStationPreviewRespVO();
            item.setDeviceId(device.getId());
            item.setDeviceName(device.getDeviceName());
            item.setExistingCount(devicePointNames.size());
            item.setImportCount(items.size());
            item.setItems(items);
            result.add(item);

            if (CollUtil.isEmpty(items)) {
                usedPointNames.addAll(devicePointNames);
            } else {
                usedPointNames.addAll(collectPointNamesFromItems(items));
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotRealtimeDataMappingBatchAddByStationRespVO batchAddByStation(IotRealtimeDataMappingBatchAddByStationReqVO reqVO) {
        if (reqVO == null || reqVO.getSourceId() == null || StrUtil.isBlank(reqVO.getStationId())) {
            IotRealtimeDataMappingBatchAddByStationRespVO empty = new IotRealtimeDataMappingBatchAddByStationRespVO();
            empty.setDeviceCount(0);
            empty.setImportedCount(0);
            empty.setDeletedCount(0);
            empty.setSkippedDeviceCount(0);
            return empty;
        }
        Long sourceId = reqVO.getSourceId();
        sourceService.validateSourceExists(sourceId);

        List<IotDeviceDO> devices = getDeviceListByStationId(reqVO.getStationId());
        if (CollUtil.isEmpty(devices)) {
            IotRealtimeDataMappingBatchAddByStationRespVO empty = new IotRealtimeDataMappingBatchAddByStationRespVO();
            empty.setDeviceCount(0);
            empty.setImportedCount(0);
            empty.setDeletedCount(0);
            empty.setSkippedDeviceCount(0);
            return empty;
        }

        List<IotRealtimeDataMappingDO> existingMappings = mappingMapper.selectListBySourceId(sourceId);
        Map<Long, List<IotRealtimeDataMappingDO>> mappingByDevice = existingMappings.stream()
                .filter(mapping -> mapping.getDeviceId() != null)
                .collect(Collectors.groupingBy(IotRealtimeDataMappingDO::getDeviceId));
        Set<String> usedPointNames = collectPointNames(existingMappings);

        int deletedCount = 0;
        int skippedDeviceCount = 0;
        List<IotRealtimeDataMappingDO> toSave = new ArrayList<>();

        for (IotDeviceDO device : devices) {
            if (device == null) {
                continue;
            }
            List<IotRealtimeDataMappingDO> deviceMappings = mappingByDevice.getOrDefault(device.getId(), List.of());
            Set<String> devicePointNames = collectPointNames(deviceMappings);
            usedPointNames.removeAll(devicePointNames);

            List<IotRealtimeDataMappingImportItemRespVO> items = buildImportItems(device, false, usedPointNames);
            if (CollUtil.isEmpty(items)) {
                skippedDeviceCount++;
                usedPointNames.addAll(devicePointNames);
                continue;
            }

            if (CollUtil.isNotEmpty(deviceMappings)) {
                mappingMapper.deleteBySourceIdAndDeviceId(sourceId, device.getId());
                deletedCount += deviceMappings.size();
            }

            for (IotRealtimeDataMappingImportItemRespVO item : items) {
                IotRealtimeDataMappingDO mapping = new IotRealtimeDataMappingDO();
                mapping.setSourceId(sourceId);
                mapping.setDeviceId(device.getId());
                mapping.setPointName(item.getPointName());
                mapping.setIdentifier(item.getIdentifier());
                mapping.setEnabled(Boolean.TRUE);
                mapping.setSort(0);
                toSave.add(mapping);
            }
            usedPointNames.addAll(collectPointNamesFromItems(items));
        }

        if (CollUtil.isNotEmpty(toSave)) {
            mappingMapper.insertBatch(toSave);
        }

        IotRealtimeDataMappingBatchAddByStationRespVO respVO = new IotRealtimeDataMappingBatchAddByStationRespVO();
        respVO.setDeviceCount(devices.size());
        respVO.setImportedCount(toSave.size());
        respVO.setDeletedCount(deletedCount);
        respVO.setSkippedDeviceCount(skippedDeviceCount);
        return respVO;
    }

    private Map<String, String> buildIdentifierLabelMap(Long productId, Long deviceId) {
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                productId, IotThingModelTypeEnum.PROPERTY.getType());
        Map<String, String> map = new HashMap<>();
        if (CollUtil.isEmpty(thingModels)) {
            return map;
        }
        Map<String, String> tagNameMap = devicePropertyTagService.getTagNameMap(deviceId);
        for (IotThingModelDO model : thingModels) {
            if (model == null) {
                continue;
            }
            String identifier = StrUtil.trimToNull(model.getIdentifier());
            if (identifier == null) {
                continue;
            }
            String tagName = StrUtil.trimToNull(tagNameMap.get(identifier));
            if (tagName != null) {
                map.put(identifier, tagName);
                continue;
            }
            String label = StrUtil.trimToNull(model.getDescription());
            if (label == null) {
                label = StrUtil.trimToNull(model.getName());
            }
            if (label == null) {
                label = identifier;
            }
            map.putIfAbsent(identifier, label);
        }
        return map;
    }

    private Set<String> loadExistingPointNameSet(Long sourceId, Long deviceId) {
        Set<String> result = new HashSet<>();
        if (sourceId != null && deviceId != null) {
            List<IotRealtimeDataMappingDO> mappings = mappingMapper.selectListBySourceIdAndDeviceId(sourceId, deviceId);
            for (IotRealtimeDataMappingDO mapping : mappings) {
                if (mapping == null) {
                    continue;
                }
                String pointName = StrUtil.trimToNull(mapping.getPointName());
                if (pointName != null) {
                    result.add(pointName);
                }
            }
        }
        return result;
    }

    private void validatePointUnique(Long sourceId, Long deviceId, String pointName, Long id) {
        String normalizedPointName = StrUtil.trimToNull(pointName);
        if (sourceId == null || deviceId == null || normalizedPointName == null) {
            return;
        }
        IotRealtimeDataMappingDO mapping =
                mappingMapper.selectBySourceIdAndDeviceIdAndPointName(sourceId, deviceId, normalizedPointName);
        if (mapping == null) {
            return;
        }
        if (id == null || !mapping.getId().equals(id)) {
            throw exception(REALTIME_DATA_MAPPING_POINT_EXISTS);
        }
    }

    private List<IotDeviceDO> getDeviceListByStationId(String stationId) {
        if (StrUtil.isBlank(stationId)) {
            return List.of();
        }
        IotDevicePageReqVO reqVO = new IotDevicePageReqVO();
        reqVO.setStationId(stationId);
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<IotDeviceDO> pageResult = deviceService.getDevicePage(reqVO);
        return pageResult.getList();
    }

    private Set<String> collectPointNames(List<IotRealtimeDataMappingDO> mappings) {
        Set<String> result = new HashSet<>();
        if (CollUtil.isEmpty(mappings)) {
            return result;
        }
        for (IotRealtimeDataMappingDO mapping : mappings) {
            if (mapping == null) {
                continue;
            }
            String pointName = StrUtil.trimToNull(mapping.getPointName());
            if (pointName != null) {
                result.add(pointName);
            }
        }
        return result;
    }

    private Set<String> collectPointNamesFromItems(List<IotRealtimeDataMappingImportItemRespVO> items) {
        Set<String> result = new HashSet<>();
        if (CollUtil.isEmpty(items)) {
            return result;
        }
        for (IotRealtimeDataMappingImportItemRespVO item : items) {
            if (item == null) {
                continue;
            }
            String pointName = StrUtil.trimToNull(item.getPointName());
            if (pointName != null) {
                result.add(pointName);
            }
        }
        return result;
    }

    private List<IotRealtimeDataMappingImportItemRespVO> buildImportItems(IotDeviceDO device,
                                                                          boolean onlyTag,
                                                                          Set<String> usedPointNames) {
        if (device == null) {
            return List.of();
        }
        List<IotThingModelDO> thingModels = thingModelService.getThingModelListByProductIdAndType(
                device.getProductId(), IotThingModelTypeEnum.PROPERTY.getType());
        Map<String, String> tagNameMap = devicePropertyTagService.getTagNameMap(device.getId());
        if (CollUtil.isEmpty(thingModels)) {
            return List.of();
        }
        if (onlyTag && CollUtil.isEmpty(tagNameMap)) {
            return List.of();
        }
        Set<String> added = new HashSet<>();
        List<IotRealtimeDataMappingImportItemRespVO> result = new ArrayList<>();
        for (IotThingModelDO model : thingModels) {
            if (model == null) {
                continue;
            }
            String identifier = StrUtil.trimToNull(model.getIdentifier());
            if (identifier == null) {
                continue;
            }
            String pointName = StrUtil.trimToNull(tagNameMap.get(identifier));
            if (onlyTag) {
                if (pointName == null) {
                    continue;
                }
            } else {
                if (pointName == null) {
                    pointName = StrUtil.trimToNull(model.getDescription());
                }
                if (pointName == null) {
                    pointName = StrUtil.trimToNull(model.getName());
                }
                if (pointName == null) {
                    pointName = identifier;
                }
            }
            if (pointName == null) {
                continue;
            }
            if (usedPointNames != null && usedPointNames.contains(pointName)) {
                continue;
            }
            if (!added.add(pointName)) {
                continue;
            }
            IotRealtimeDataMappingImportItemRespVO item = new IotRealtimeDataMappingImportItemRespVO();
            item.setPointName(pointName);
            item.setIdentifier(identifier);
            result.add(item);
        }
        return result;
    }

}
