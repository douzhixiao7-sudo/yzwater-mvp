package com.sydigit.yzwater.module.service.screen;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenVideoDeviceSummaryRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenVideoPreviewReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenVideoStationCameraRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPtzControlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPreviewUrlReqVO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoCameraDO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoRegionDO;
import com.sydigit.yzwater.module.config.video.YzVideoScreenExcludeResolver;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoCameraMapper;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoRegionMapper;
import com.sydigit.yzwater.module.service.video.YzVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 大屏统计-视频监控 服务
 */
@Service
@Validated
@RequiredArgsConstructor
public class BigScreenVideoMonitoringService {

    private static final String CAMERA_ONLINE_STATUS = "1";
    private static final String UNKNOWN_STATION_NAME = "未命名站点";

    private final YzVideoRegionMapper yzVideoRegionMapper;
    private final YzVideoCameraMapper yzVideoCameraMapper;
    private final YzVideoService yzVideoService;
    private final YzVideoScreenExcludeResolver videoScreenExcludeResolver;

    /**
     * 查询各闸站及其监控设备
     */
    public List<BigScreenVideoStationCameraRespVO> getStationCameraList() {
        Set<String> excludedScreenRegions = videoScreenExcludeResolver.excludedRegionIndexCodes();
        List<YzVideoCameraDO> cameraList = yzVideoCameraMapper.selectList(new LambdaQueryWrapper<YzVideoCameraDO>()
                .select(YzVideoCameraDO::getCameraIndexCode,
                        YzVideoCameraDO::getCameraName,
                        YzVideoCameraDO::getRegionIndexCode,
                        YzVideoCameraDO::getOnline)
                .isNotNull(YzVideoCameraDO::getRegionIndexCode)
                .orderByAsc(YzVideoCameraDO::getCameraName)
                .orderByAsc(YzVideoCameraDO::getCameraIndexCode));
        cameraList = filterOutExcludedScreenRegions(excludedScreenRegions, cameraList);
        if (CollUtil.isEmpty(cameraList)) {
            return Collections.emptyList();
        }

        Map<String, List<YzVideoCameraDO>> camerasByRegion = cameraList.stream()
                .filter(camera -> StrUtil.isNotBlank(camera.getRegionIndexCode()))
                .collect(Collectors.groupingBy(YzVideoCameraDO::getRegionIndexCode, LinkedHashMap::new, Collectors.toList()));
        if (camerasByRegion.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> regionCodes = new LinkedHashSet<>(camerasByRegion.keySet());
        List<YzVideoRegionDO> regionList = yzVideoRegionMapper.selectList(new LambdaQueryWrapper<YzVideoRegionDO>()
                .select(YzVideoRegionDO::getIndexCode, YzVideoRegionDO::getName)
                .in(YzVideoRegionDO::getIndexCode, regionCodes)
                .orderByAsc(YzVideoRegionDO::getName)
                .orderByAsc(YzVideoRegionDO::getIndexCode));

        List<BigScreenVideoStationCameraRespVO> result = new ArrayList<>();
        Set<String> usedRegionCodes = new LinkedHashSet<>();
        for (YzVideoRegionDO region : regionList) {
            usedRegionCodes.add(region.getIndexCode());
            result.add(buildStationCameraResp(region.getIndexCode(), region.getName(),
                    camerasByRegion.getOrDefault(region.getIndexCode(), Collections.emptyList())));
        }

        for (String regionCode : regionCodes) {
            if (usedRegionCodes.contains(regionCode)) {
                continue;
            }
            result.add(buildStationCameraResp(regionCode, null,
                    camerasByRegion.getOrDefault(regionCode, Collections.emptyList())));
        }
        return result;
    }

    /**
     * 视频监控设备统计汇总
     */
    public BigScreenVideoDeviceSummaryRespVO getDeviceSummary() {
        Set<String> excludedScreenRegions = videoScreenExcludeResolver.excludedRegionIndexCodes();
        List<YzVideoCameraDO> cameraList = yzVideoCameraMapper.selectList(new LambdaQueryWrapper<YzVideoCameraDO>()
                .select(YzVideoCameraDO::getCameraIndexCode,
                        YzVideoCameraDO::getRegionIndexCode,
                        YzVideoCameraDO::getOnline));
        cameraList = filterOutExcludedScreenRegions(excludedScreenRegions, cameraList);
        int deviceTotal = cameraList.size();
        int onlineTotal = (int) cameraList.stream()
                .filter(camera -> isOnlineStatus(camera.getOnline()))
                .count();
        int offlineTotal = deviceTotal - onlineTotal;
        BigDecimal onlineRate = BigDecimal.ZERO;
        if (deviceTotal > 0) {
            onlineRate = BigDecimal.valueOf(onlineTotal)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(deviceTotal), 2, RoundingMode.HALF_UP);
        }

        BigScreenVideoDeviceSummaryRespVO respVO = new BigScreenVideoDeviceSummaryRespVO();
        respVO.setDeviceTotal(deviceTotal);
        respVO.setOnlineTotal(onlineTotal);
        respVO.setOfflineTotal(offlineTotal);
        respVO.setRealTimeDeviceOnlineRate(onlineRate);
        return respVO;
    }

    /**
     * 控制监控设备云台
     */
    public Map<String, Object> controlCamera(VideoPtzControlReqVO reqVO) {
        return yzVideoService.controlCamera(reqVO);
    }

    /**
     * 获取监控设备预览流地址
     */
    public Map<String, Object> getPreviewUrl(BigScreenVideoPreviewReqVO reqVO) {
        VideoPreviewUrlReqVO previewReqVO = new VideoPreviewUrlReqVO();
        previewReqVO.setCameraIndexCode(reqVO.getCameraIndexCode());
        previewReqVO.setStreamType(reqVO.getStreamType());
        previewReqVO.setProtocol(reqVO.getProtocol());
        previewReqVO.setTransmode(reqVO.getTransmode());
        previewReqVO.setExpand(reqVO.getExpand());
        previewReqVO.setStreamform(reqVO.getStreamform());
        return yzVideoService.getPreviewUrl(previewReqVO);
    }

    private BigScreenVideoStationCameraRespVO buildStationCameraResp(String regionIndexCode,
                                                                     String stationName,
                                                                     List<YzVideoCameraDO> cameraList) {
        BigScreenVideoStationCameraRespVO respVO = new BigScreenVideoStationCameraRespVO();
        respVO.setRegionIndexCode(regionIndexCode);
        respVO.setStationName(resolveStationName(stationName));
        List<BigScreenVideoStationCameraRespVO.CameraItem> cameraItems = cameraList.stream()
                .map(this::buildCameraItem)
                .toList();
        respVO.setCameraList(cameraItems);
        respVO.setCameraCount(cameraItems.size());
        return respVO;
    }

    private String resolveStationName(String stationName) {
        return StrUtil.blankToDefault(StrUtil.trim(stationName), UNKNOWN_STATION_NAME);
    }

    private BigScreenVideoStationCameraRespVO.CameraItem buildCameraItem(YzVideoCameraDO camera) {
        BigScreenVideoStationCameraRespVO.CameraItem item = new BigScreenVideoStationCameraRespVO.CameraItem();
        item.setCameraIndexCode(camera.getCameraIndexCode());
        item.setCameraName(StrUtil.blankToDefault(camera.getCameraName(), camera.getCameraIndexCode()));
        item.setOnline(camera.getOnline());
        return item;
    }

    private boolean isOnlineStatus(String onlineStatus) {
        return StrUtil.equals(CAMERA_ONLINE_STATUS, onlineStatus)
                || StrUtil.equalsIgnoreCase("true", onlineStatus)
                || StrUtil.equalsIgnoreCase("online", onlineStatus);
    }

    /**
     * 大屏口径：排除配置的海康区域及其下全部摄像头；无区域编码的摄像头保留。
     */
    private List<YzVideoCameraDO> filterOutExcludedScreenRegions(Set<String> excludedRegions,
                                                                 List<YzVideoCameraDO> cameras) {
        if (CollUtil.isEmpty(cameras) || CollUtil.isEmpty(excludedRegions)) {
            return cameras;
        }
        return cameras.stream()
                .filter(camera -> {
                    String regionIndexCode = camera.getRegionIndexCode();
                    if (StrUtil.isBlank(regionIndexCode)) {
                        return true;
                    }
                    return !excludedRegions.contains(StrUtil.trim(regionIndexCode));
                })
                .collect(Collectors.toList());
    }
}
