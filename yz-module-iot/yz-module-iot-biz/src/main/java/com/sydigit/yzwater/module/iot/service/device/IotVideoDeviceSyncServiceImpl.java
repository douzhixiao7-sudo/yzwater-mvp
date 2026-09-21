package com.sydigit.yzwater.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.module.iot.controller.admin.device.vo.device.IotVideoDeviceSyncRespVO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoCameraDO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoRegionDO;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoCameraMapper;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoRegionMapper;
import com.sydigit.yzwater.module.iot.core.enums.IotDeviceStateEnum;
import com.sydigit.yzwater.module.iot.dal.dataobject.device.IotDeviceDO;
import com.sydigit.yzwater.module.iot.dal.dataobject.product.IotProductDO;
import com.sydigit.yzwater.module.iot.dal.mysql.device.IotDeviceMapper;
import com.sydigit.yzwater.module.iot.enums.product.IotLocationTypeEnum;
import com.sydigit.yzwater.module.iot.service.product.IotProductService;
import com.sydigit.yzwater.module.service.video.YzVideoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 视频摄像头同步到 IoT 设备实现
 */
@Service
@Validated
@Slf4j
public class IotVideoDeviceSyncServiceImpl implements IotVideoDeviceSyncService {

    private static final Long VIDEO_PRODUCT_ID = 2037413550229770241L;
    private static final Integer VIDEO_DEVICE_TYPE = 1;
    private static final String VIDEO_SERIAL_PREFIX = "YZ_VIDEO_";
    private static final String VIDEO_SYNC_SOURCE = "YZ_VIDEO_CAMERA_SYNC";
    private static final Map<String, String> REGION_STATION_MAP = Map.of(
            "潘家河闸站", "1",
            "金斗河闸站", "7",
            "沙河闸站", "2",
            "十二圩闸站", "0",
            "卧虎闸", "8",
            "幸福河湖", "4",
            "江堤监控", "5"
    );

    @Resource
    private YzVideoService yzVideoService;
    @Resource
    private YzVideoRegionMapper yzVideoRegionMapper;
    @Resource
    private YzVideoCameraMapper yzVideoCameraMapper;
    @Resource
    private IotDeviceMapper iotDeviceMapper;
    @Resource
    private IotProductService iotProductService;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public IotVideoDeviceSyncRespVO syncVideoDevices() {
        // 1. 先同步海康区域、摄像头到本地表，确保使用的是最新数据
        Map<String, Object> regionSyncResult = safeSyncFromHikvision("region", yzVideoService::syncRegions);
        Map<String, Object> cameraSyncResult = safeSyncFromHikvision("camera", yzVideoService::syncCameras);

        // 2. 校验产品存在，并构建区域编码 -> 区域名称映射
        IotProductDO product = iotProductService.getProduct(VIDEO_PRODUCT_ID);
        Assert.notNull(product, "未找到视频产品，productId=" + VIDEO_PRODUCT_ID);

        Map<String, String> regionNameMap = yzVideoRegionMapper.selectList().stream()
                .filter(region -> StrUtil.isNotBlank(region.getIndexCode()))
                .collect(Collectors.toMap(YzVideoRegionDO::getIndexCode,
                        YzVideoRegionDO::getName, (a, b) -> a, LinkedHashMap::new));

        // 3. 构建本次目标设备集合
        List<YzVideoCameraDO> cameraList = yzVideoCameraMapper.selectList();
        Map<String, IotDeviceDO> targetDeviceMap = new LinkedHashMap<>();
        int skippedCount = 0;
        for (YzVideoCameraDO camera : cameraList) {
            String cameraIndexCode = StrUtil.trimToNull(camera.getCameraIndexCode());
            String cameraName = StrUtil.trimToNull(camera.getCameraName());
            if (cameraIndexCode == null || cameraName == null) {
                skippedCount++;
                continue;
            }
            String regionName = StrUtil.trimToNull(regionNameMap.get(camera.getRegionIndexCode()));
            String stationId = regionName == null ? null : REGION_STATION_MAP.get(regionName);
            if (stationId == null) {
                skippedCount++;
                continue;
            }

            String serialNumber = buildSerialNumber(cameraIndexCode);
            IotDeviceDO device = new IotDeviceDO();
            device.setProductId(VIDEO_PRODUCT_ID);
            device.setProductKey(product.getProductKey());
            device.setDeviceType(VIDEO_DEVICE_TYPE);
            device.setStationId(stationId);
            device.setDeviceName(cameraName);
            device.setSerialNumber(serialNumber);
            device.setLongitude(parseCoordinate(camera.getLon()));
            device.setLatitude(parseCoordinate(camera.getLat()));
            device.setLocationType(IotLocationTypeEnum.MANUAL.getType());
            device.setConfig(buildSyncConfig(cameraIndexCode, camera.getRegionIndexCode(), regionName));
            // 新增设备默认未激活；更新时不覆盖状态
            device.setState(IotDeviceStateEnum.INACTIVE.getState());
            targetDeviceMap.put(serialNumber, device);
        }

        // 4. 查询当前已同步的历史设备，做增删改比对
        List<IotDeviceDO> existingDeviceList = iotDeviceMapper.selectList(new LambdaQueryWrapper<IotDeviceDO>()
                .eq(IotDeviceDO::getProductId, VIDEO_PRODUCT_ID)
                .eq(IotDeviceDO::getDeviceType, VIDEO_DEVICE_TYPE)
                .likeRight(IotDeviceDO::getSerialNumber, VIDEO_SERIAL_PREFIX));
        Map<String, IotDeviceDO> existingDeviceMap = existingDeviceList.stream()
                .filter(device -> StrUtil.isNotBlank(device.getSerialNumber()))
                .collect(Collectors.toMap(IotDeviceDO::getSerialNumber, v -> v, (a, b) -> a, LinkedHashMap::new));

        List<IotDeviceDO> toInsert = new ArrayList<>();
        List<IotDeviceDO> toUpdate = new ArrayList<>();
        for (Map.Entry<String, IotDeviceDO> entry : targetDeviceMap.entrySet()) {
            IotDeviceDO target = entry.getValue();
            IotDeviceDO existing = existingDeviceMap.remove(entry.getKey());
            if (existing == null) {
                toInsert.add(target);
                continue;
            }
            if (hasManagedDeviceChanged(existing, target)) {
                IotDeviceDO update = new IotDeviceDO();
                update.setId(existing.getId());
                update.setProductId(target.getProductId());
                update.setProductKey(target.getProductKey());
                update.setDeviceType(target.getDeviceType());
                update.setStationId(target.getStationId());
                update.setDeviceName(target.getDeviceName());
                update.setSerialNumber(target.getSerialNumber());
                update.setLongitude(target.getLongitude());
                update.setLatitude(target.getLatitude());
                update.setLocationType(target.getLocationType());
                update.setConfig(target.getConfig());
                toUpdate.add(update);
            }
        }

        List<Long> toDeleteIds = existingDeviceMap.values().stream()
                .map(IotDeviceDO::getId)
                .filter(Objects::nonNull)
                .toList();

        // 设备增删改单独包事务，避免海康调用异常将当前事务标记为 rollback-only
        transactionTemplate.executeWithoutResult(status -> {
            if (CollUtil.isNotEmpty(toInsert)) {
                iotDeviceMapper.insertBatch(toInsert);
            }
            for (IotDeviceDO device : toUpdate) {
                iotDeviceMapper.updateById(device);
            }
            if (CollUtil.isNotEmpty(toDeleteIds)) {
                iotDeviceMapper.deleteBatchIds(toDeleteIds);
            }
        });

        IotVideoDeviceSyncRespVO respVO = new IotVideoDeviceSyncRespVO();
        respVO.setRegionRemoteCount(extractCount(regionSyncResult, "remoteCount"));
        respVO.setRegionInsertedCount(extractCount(regionSyncResult, "inserted"));
        respVO.setRegionUpdatedCount(extractCount(regionSyncResult, "updated"));
        respVO.setRegionDeletedCount(extractCount(regionSyncResult, "deleted"));
        respVO.setCameraRemoteCount(extractCount(cameraSyncResult, "remoteCount"));
        respVO.setCameraInsertedCount(extractCount(cameraSyncResult, "inserted"));
        respVO.setCameraUpdatedCount(extractCount(cameraSyncResult, "updated"));
        respVO.setCameraDeletedCount(extractCount(cameraSyncResult, "deleted"));
        respVO.setTargetCount(targetDeviceMap.size());
        respVO.setSkippedCount(skippedCount);
        respVO.setInsertedCount(toInsert.size());
        respVO.setUpdatedCount(toUpdate.size());
        respVO.setDeletedCount(toDeleteIds.size());
        return respVO;
    }

    private String buildSerialNumber(String cameraIndexCode) {
        return VIDEO_SERIAL_PREFIX + cameraIndexCode;
    }

    private String buildSyncConfig(String cameraIndexCode, String regionIndexCode, String regionName) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("syncSource", VIDEO_SYNC_SOURCE);
        jsonObject.put("cameraIndexCode", cameraIndexCode);
        jsonObject.put("regionIndexCode", regionIndexCode);
        jsonObject.put("regionName", regionName);
        return jsonObject.toJSONString();
    }

    private BigDecimal parseCoordinate(String coordinate) {
        String trimmed = StrUtil.trimToNull(coordinate);
        if (trimmed == null) {
            return null;
        }
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer extractCount(Map<String, Object> syncResult, String key) {
        if (syncResult == null) {
            return 0;
        }
        return Convert.toInt(syncResult.get(key), 0);
    }

    private Map<String, Object> safeSyncFromHikvision(String syncType, Supplier<Map<String, Object>> supplier) {
        try {
            Map<String, Object> result = supplier.get();
            return result == null ? Map.of() : result;
        } catch (Exception ex) {
            log.warn("调用海康{}同步失败，改为使用数据库缓存数据继续处理，原因：{}", syncType, ex.getMessage());
            return Map.of(
                    "remoteCount", 0,
                    "inserted", 0,
                    "updated", 0,
                    "deleted", 0
            );
        }
    }

    private boolean hasManagedDeviceChanged(IotDeviceDO existing, IotDeviceDO target) {
        return ObjectUtil.notEqual(existing.getProductId(), target.getProductId())
                || ObjectUtil.notEqual(existing.getProductKey(), target.getProductKey())
                || ObjectUtil.notEqual(existing.getDeviceType(), target.getDeviceType())
                || ObjectUtil.notEqual(existing.getStationId(), target.getStationId())
                || ObjectUtil.notEqual(existing.getDeviceName(), target.getDeviceName())
                || ObjectUtil.notEqual(existing.getSerialNumber(), target.getSerialNumber())
                || bigDecimalNotEqual(existing.getLongitude(), target.getLongitude())
                || bigDecimalNotEqual(existing.getLatitude(), target.getLatitude())
                || ObjectUtil.notEqual(existing.getLocationType(), target.getLocationType())
                || ObjectUtil.notEqual(existing.getConfig(), target.getConfig());
    }

    private boolean bigDecimalNotEqual(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return false;
        }
        if (left == null || right == null) {
            return true;
        }
        return left.compareTo(right) != 0;
    }
}
