package com.sydigit.yzwater.module.service.video;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.sydigit.yzwater.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.sydigit.yzwater.framework.common.exception.util.ServiceExceptionUtil;
import com.sydigit.yzwater.framework.common.util.json.JsonUtils;
import com.sydigit.yzwater.framework.common.util.servlet.ServletUtils;
import com.sydigit.yzwater.module.config.video.YzVideoHkProperties;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoEventSubscribeReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPlaybackUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPreviewUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPtzControlReqVO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoCameraDO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoEventDO;
import com.sydigit.yzwater.module.dal.dataobject.video.YzVideoRegionDO;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoCameraMapper;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoEventMapper;
import com.sydigit.yzwater.module.dal.mysql.video.YzVideoRegionMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.BufferedReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 视频监控服务
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class YzVideoService {

    private static final String ARTEMIS_PATH = "/artemis";
    private static final String CONTENT_TYPE = "application/json";
    private static final int MAX_PAGE_GUARD = 2000;
    private static final int ONLINE_CHUNK_SIZE = 200;

    private final ArtemisConfig artemisConfig;
    private final YzVideoHkProperties properties;
    private final YzVideoRegionMapper regionMapper;
    private final YzVideoCameraMapper cameraMapper;
    private final YzVideoEventMapper eventMapper;

    /**
     * 同步视频区域到 yz_video_regions
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncRegions() {
        ensureHkConfigured();
        List<YzVideoRegionDO> remoteList = fetchAllRegions();
        Map<String, YzVideoRegionDO> remoteMap = remoteList.stream()
                .collect(Collectors.toMap(YzVideoRegionDO::getIndexCode, v -> v, (a, b) -> b, LinkedHashMap::new));
        List<YzVideoRegionDO> localList = regionMapper.selectList();
        Map<String, YzVideoRegionDO> localMap = localList.stream()
                .collect(Collectors.toMap(YzVideoRegionDO::getIndexCode, v -> v, (a, b) -> a, LinkedHashMap::new));

        List<YzVideoRegionDO> toInsert = new ArrayList<>();
        List<YzVideoRegionDO> toUpdate = new ArrayList<>();
        for (Map.Entry<String, YzVideoRegionDO> entry : remoteMap.entrySet()) {
            YzVideoRegionDO local = localMap.get(entry.getKey());
            if (local == null) {
                toInsert.add(entry.getValue());
                continue;
            }
            if (hasRegionChanged(local, entry.getValue())) {
                toUpdate.add(entry.getValue());
            }
        }

        Set<String> remoteIds = new HashSet<>(remoteMap.keySet());
        List<String> toDeleteIds = localMap.keySet().stream()
                .filter(id -> !remoteIds.contains(id))
                .toList();

        if (CollUtil.isNotEmpty(toInsert)) {
            regionMapper.insertBatch(toInsert);
        }
        for (YzVideoRegionDO item : toUpdate) {
            updateRegionByIndexCode(item);
        }
        if (CollUtil.isNotEmpty(toDeleteIds)) {
            regionMapper.deleteBatchIds(toDeleteIds);
        }
        return MapUtil.<String, Object>builder(new LinkedHashMap<>())
                .put("success", true)
                .put("remoteCount", remoteMap.size())
                .put("inserted", toInsert.size())
                .put("updated", toUpdate.size())
                .put("deleted", toDeleteIds.size())
                .build();
    }

    /**
     * 同步视频摄像头到 yz_video_cameras
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncCameras() {
        ensureHkConfigured();
        List<YzVideoCameraDO> remoteList = fetchAllCameras();
        Map<String, YzVideoCameraDO> remoteMap = remoteList.stream()
                .collect(Collectors.toMap(YzVideoCameraDO::getCameraIndexCode, v -> v, (a, b) -> b, LinkedHashMap::new));
        List<YzVideoCameraDO> localList = cameraMapper.selectList();
        Map<String, YzVideoCameraDO> localMap = localList.stream()
                .collect(Collectors.toMap(YzVideoCameraDO::getCameraIndexCode, v -> v, (a, b) -> a, LinkedHashMap::new));

        List<YzVideoCameraDO> toInsert = new ArrayList<>();
        List<YzVideoCameraDO> toUpdate = new ArrayList<>();
        for (Map.Entry<String, YzVideoCameraDO> entry : remoteMap.entrySet()) {
            YzVideoCameraDO remote = entry.getValue();
            YzVideoCameraDO local = localMap.get(entry.getKey());
            if (local == null) {
                toInsert.add(remote);
                continue;
            }
            YzVideoCameraDO mergeTarget = mergeCameraForPersist(local, remote);
            if (hasCameraChanged(local, mergeTarget)) {
                toUpdate.add(mergeTarget);
            }
        }

        Set<String> remoteIds = new HashSet<>(remoteMap.keySet());
        List<String> toDeleteIds = localMap.keySet().stream()
                .filter(id -> !remoteIds.contains(id))
                .toList();

        if (CollUtil.isNotEmpty(toInsert)) {
            cameraMapper.insertBatch(toInsert);
        }
        for (YzVideoCameraDO item : toUpdate) {
            updateCameraByIndexCode(item);
        }
        if (CollUtil.isNotEmpty(toDeleteIds)) {
            cameraMapper.deleteBatchIds(toDeleteIds);
        }
        return MapUtil.<String, Object>builder(new LinkedHashMap<>())
                .put("success", true)
                .put("remoteCount", remoteMap.size())
                .put("inserted", toInsert.size())
                .put("updated", toUpdate.size())
                .put("deleted", toDeleteIds.size())
                .build();
    }

    /**
     * 获取预览流地址
     */
    public Map<String, Object> getPreviewUrl(VideoPreviewUrlReqVO reqVO) {
        ensureHkConfigured();
        JSONObject body = new JSONObject();
        body.set("cameraIndexCode", reqVO.getCameraIndexCode());
        body.set("streamType", reqVO.getStreamType() == null ? 0 : reqVO.getStreamType());
        body.set("protocol", StrUtil.blankToDefault(reqVO.getProtocol(), "ws"));
        body.set("transmode", reqVO.getTransmode() == null ? 1 : reqVO.getTransmode());
        body.set("expand", StrUtil.blankToDefault(reqVO.getExpand(), "transcode=0"));
        body.set("streamform", StrUtil.blankToDefault(reqVO.getStreamform(), "ps"));
        JSONObject ret = doPost("/api/video/v2/cameras/previewURLs", body);
        return rewritePreviewUrlResponse(toMap(ret));
    }

    /**
     * 获取回放流地址
     */
    public Map<String, Object> getPlaybackUrl(VideoPlaybackUrlReqVO reqVO) {
        ensureHkConfigured();
        JSONObject body = new JSONObject();
        body.set("cameraIndexCode", reqVO.getCameraIndexCode());
        body.set("recordLocation", reqVO.getRecordLocation() == null ? 0 : reqVO.getRecordLocation());
        body.set("protocol", StrUtil.blankToDefault(reqVO.getProtocol(), "ws"));
        body.set("transmode", reqVO.getTransmode() == null ? 1 : reqVO.getTransmode());
        body.set("beginTime", reqVO.getBeginTime());
        body.set("endTime", reqVO.getEndTime());
        body.set("uuid", StrUtil.blankToDefault(reqVO.getUuid(), ""));
        body.set("expand", StrUtil.blankToDefault(reqVO.getExpand(), "transcode=0"));
        body.set("streamform", StrUtil.blankToDefault(reqVO.getStreamform(), "ps"));
        body.set("lockType", reqVO.getLockType() == null ? 0 : reqVO.getLockType());
        JSONObject ret = doPost("/api/video/v2/cameras/playbackURLs", body);
        return toMap(ret);
    }

    /**
     * 同步摄像头在线状态
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncCameraOnlineStatus() {
        ensureHkConfigured();
        List<YzVideoCameraDO> cameras = cameraMapper.selectList();
        if (CollUtil.isEmpty(cameras)) {
            return MapUtil.<String, Object>builder(new LinkedHashMap<>())
                    .put("success", true)
                    .put("total", 0)
                    .put("onlineCount", 0)
                    .build();
        }

        List<String> cameraCodes = cameras.stream()
                .map(YzVideoCameraDO::getCameraIndexCode)
                .filter(StrUtil::isNotBlank)
                .toList();
        if (CollUtil.isEmpty(cameraCodes)) {
            return MapUtil.<String, Object>builder(new LinkedHashMap<>())
                    .put("success", true)
                    .put("total", 0)
                    .put("onlineCount", 0)
                    .build();
        }

        Map<String, String> onlineMap = new LinkedHashMap<>();
        for (List<String> chunk : CollUtil.split(cameraCodes, ONLINE_CHUNK_SIZE)) {
            JSONObject body = new JSONObject();
            body.set("regionId", "root00000000");
            body.set("includeSubNode", "1");
            body.set("indexCodes", chunk.toArray(String[]::new));
            body.set("status", "1");
            body.set("pageSize", resolvePageSize());
            body.set("pageNo", 1);

            JSONObject ret = doPost("/api/nms/v1/online/camera/get", body);
            ensureArtemisSuccess(ret, "/api/nms/v1/online/camera/get");
            JSONArray list = getDataList(ret);
            for (int i = 0; i < list.size(); i++) {
                JSONObject item = list.getJSONObject(i);
                String indexCode = item.getStr("indexCode");
                if (StrUtil.isBlank(indexCode)) {
                    continue;
                }
                String online = StrUtil.blankToDefault(item.getStr("online"), "1");
                onlineMap.put(indexCode, online);
            }
        }

        // 先将已入库监控点统一置离线，再批量回写在线状态，避免离线状态滞留
        cameraMapper.update(null, new LambdaUpdateWrapper<YzVideoCameraDO>()
                .set(YzVideoCameraDO::getOnline, "0"));
        onlineMap.forEach((indexCode, online) -> cameraMapper.update(null, new LambdaUpdateWrapper<YzVideoCameraDO>()
                .eq(YzVideoCameraDO::getCameraIndexCode, indexCode)
                .set(YzVideoCameraDO::getOnline, online)));

        return MapUtil.<String, Object>builder(new LinkedHashMap<>())
                .put("success", true)
                .put("total", cameraCodes.size())
                .put("onlineCount", onlineMap.size())
                .build();
    }

    /**
     * 云台控制
     */
    public Map<String, Object> controlCamera(VideoPtzControlReqVO reqVO) {
        ensureHkConfigured();
        JSONObject body = new JSONObject();
        body.set("cameraIndexCode", reqVO.getCameraIndexCode());
        body.set("action", reqVO.getAction());
        body.set("command", reqVO.getCommand());
        body.set("speed", reqVO.getSpeed());
        body.set("presetIndex", reqVO.getPresetIndex() == null ? 0 : reqVO.getPresetIndex());
        JSONObject ret = doPost("/api/video/v1/ptzs/controlling", body);
        return toMap(ret);
    }

    /**
     * 事件订阅
     */
    public Map<String, Object> subscribeEvents(VideoEventSubscribeReqVO reqVO) {
        ensureHkConfigured();
        JSONObject body = new JSONObject();
        body.set("eventTypes", resolveEventTypes(reqVO == null ? null : reqVO.getEventTypes()));
        body.set("eventDest", resolveEventDest(reqVO == null ? null : reqVO.getEventDest()));
        body.set("subType", reqVO == null || reqVO.getSubType() == null ? 0 : reqVO.getSubType());
        body.set("eventLvl", resolveEventLevels(reqVO == null ? null : reqVO.getEventLevels()));
        JSONObject ret = doPost("/api/eventService/v1/eventSubscriptionByEventTypes", body);
        return toMap(ret);
    }

    /**
     * 接收事件回调并入库
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> receiveEvent(HttpServletRequest request) {
        String rawBody = readBody(request);
        Map<String, Object> payload = parseCallbackBody(rawBody);

        YzVideoEventDO eventDO = new YzVideoEventDO();
        eventDO.setId(IdUtil.fastSimpleUUID());
        eventDO.setCreateTime(LocalDateTime.now());
        eventDO.setEventRevContent(payload);
        eventMapper.insert(eventDO);

        return MapUtil.<String, Object>builder(new LinkedHashMap<>())
                .put("id", eventDO.getId())
                .put("createTime", eventDO.getCreateTime())
                .build();
    }

    private List<YzVideoRegionDO> fetchAllRegions() {
        List<JSONObject> list = fetchPagedList("/api/resource/v1/regions");
        List<YzVideoRegionDO> regions = new ArrayList<>(list.size());
        for (JSONObject item : list) {
            YzVideoRegionDO region = new YzVideoRegionDO();
            region.setIndexCode(item.getStr("indexCode"));
            region.setName(item.getStr("name"));
            region.setParentIndexCode(item.getStr("parentIndexCode"));
            region.setTreeCode(item.getStr("treeCode"));
            if (StrUtil.isBlank(region.getIndexCode())) {
                continue;
            }
            regions.add(region);
        }
        return regions;
    }

    private List<YzVideoCameraDO> fetchAllCameras() {
        List<JSONObject> list = fetchPagedList("/api/resource/v1/cameras");
        List<YzVideoCameraDO> cameras = new ArrayList<>(list.size());
        int geoRawLogCount = 0;
        for (JSONObject item : list) {
            YzVideoCameraDO camera = new YzVideoCameraDO();
            camera.setCameraIndexCode(item.getStr("cameraIndexCode"));
            camera.setCameraName(item.getStr("cameraName"));
            camera.setRegionIndexCode(item.getStr("regionIndexCode"));
            camera.setChannelNo(parseNullableInteger(item.getStr("channelNo")));
            camera.setCameraType(item.getStr("cameraType"));
            camera.setCameraTypeName(item.getStr("cameraTypeName"));
            camera.setLon(firstNotBlank(item.getStr("lon"), item.getStr("longitude")));
            camera.setLat(firstNotBlank(item.getStr("lat"), item.getStr("latitude")));
            camera.setOnline(item.getStr("online"));
            if (geoRawLogCount < 3) {
                log.info("[HikCameraGeo][YzVideo][raw] indexCode={}, name={}, lon={}, longitude={}, lat={}, latitude={}, keys={}",
                        item.getStr("cameraIndexCode"), item.getStr("cameraName"),
                        item.getStr("lon"), item.getStr("longitude"), item.getStr("lat"), item.getStr("latitude"),
                        item.keySet());
                geoRawLogCount++;
            }
            if (StrUtil.isBlank(camera.getCameraIndexCode())) {
                continue;
            }
            cameras.add(camera);
        }
        logHikCameraGeoDiagnostics("YzVideo", list, true);
        return cameras;
    }

    private List<JSONObject> fetchPagedList(String apiPath) {
        int pageNo = 1;
        Integer total = null;
        int pageSize = resolvePageSize();
        List<JSONObject> all = new ArrayList<>();
        while (pageNo <= MAX_PAGE_GUARD) {
            JSONObject body = new JSONObject();
            body.set("pageNo", pageNo);
            body.set("pageSize", pageSize);

            JSONObject ret = doPost(apiPath, body);
            ensureArtemisSuccess(ret, apiPath);
            JSONObject data = ret.getJSONObject("data");
            if (data != null && data.containsKey("total")) {
                total = parseNullableInteger(String.valueOf(data.get("total")));
            }
            JSONArray list = getDataList(ret);
            if (CollUtil.isEmpty(list)) {
                break;
            }
            if (apiPath.contains("cameras") && pageNo == 1) {
                log.info("[HikCameraGeo][YzVideo][page1-first-item] {}", list.getJSONObject(0));
            }
            for (int i = 0; i < list.size(); i++) {
                all.add(list.getJSONObject(i));
            }
            if (total != null && all.size() >= total) {
                break;
            }
            if (list.size() < pageSize) {
                break;
            }
            pageNo++;
        }
        if (pageNo > MAX_PAGE_GUARD) {
            log.warn("[fetchPagedList][分页超过保护阈值][apiPath={}, maxPage={}]", apiPath, MAX_PAGE_GUARD);
        }
        return all;
    }

    private int resolvePageSize() {
        Integer pageSize = properties.getPageSize();
        if (pageSize == null || pageSize <= 0) {
            return 500;
        }
        return Math.min(pageSize, 1000);
    }

    private JSONObject doPost(String apiPath, JSONObject body) {
        String responseText;
        try {
            responseText = doPostStringArtemisCompat(buildPath(apiPath), body.toString());
        } catch (Exception ex) {
            log.error("[doPost][调用海康接口异常][apiPath={}, body={}]", apiPath, body, ex);
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "调用海康接口失败：{}", ex.getMessage());
        }
        if (StrUtil.isBlank(responseText)) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "海康接口返回为空：{}", apiPath);
        }
        try {
            return JSONUtil.parseObj(responseText);
        } catch (Exception ex) {
            log.error("[doPost][解析海康接口响应失败][apiPath={}, body={}, response={}]", apiPath, body,
                    truncate(responseText, 1000), ex);
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "海康接口响应解析失败：{}", apiPath);
        }
    }

    /**
     * Artemis SDK 在 1.1.3 与 1.1.15.RELEASE 的 doPostStringArtemis 方法签名不同，
     * 这里做反射兼容，避免环境中实际解析版本不一致导致编译/运行失败。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private String doPostStringArtemisCompat(Map<String, String> path, String body) throws Exception {
        try {
            Method newMethod = ArtemisHttpUtil.class.getMethod("doPostStringArtemis",
                    ArtemisConfig.class, Map.class, String.class, Map.class, String.class, String.class, Map.class);
            return (String) newMethod.invoke(null, artemisConfig, path, body, null, CONTENT_TYPE, null, null);
        } catch (NoSuchMethodException ex) {
            return invokeLegacyArtemisMethod(path, body);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getTargetException();
            if (cause instanceof Exception targetEx) {
                throw targetEx;
            }
            throw new RuntimeException(cause);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private String invokeLegacyArtemisMethod(Map<String, String> path, String body) throws Exception {
        try {
            Method legacyMethod6 = ArtemisHttpUtil.class.getMethod("doPostStringArtemis",
                    Map.class, String.class, Map.class, String.class, String.class, Map.class);
            return (String) legacyMethod6.invoke(null, path, body, null, CONTENT_TYPE, null, null);
        } catch (NoSuchMethodException ex) {
            Method legacyMethod5 = ArtemisHttpUtil.class.getMethod("doPostStringArtemis",
                    Map.class, String.class, Map.class, String.class, String.class);
            return (String) legacyMethod5.invoke(null, path, body, null, CONTENT_TYPE, null);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getTargetException();
            if (cause instanceof Exception targetEx) {
                throw targetEx;
            }
            throw new RuntimeException(cause);
        }
    }

    private Map<String, String> buildPath(String apiPath) {
        Map<String, String> path = new LinkedHashMap<>(2);
        path.put("https://", ARTEMIS_PATH + apiPath);
        return path;
    }

    private void ensureArtemisSuccess(JSONObject response, String apiPath) {
        String code = response.getStr("code");
        if ("0".equals(code)) {
            return;
        }
        String msg = firstNotBlank(response.getStr("msg"), response.getStr("message"), response.getStr("data"));
        log.warn("[ensureArtemisSuccess][海康返回失败][apiPath={}, code={}, msg={}]", apiPath, code, msg);
        throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                "海康接口调用失败：{}，code={}，msg={}",
                apiPath, StrUtil.blankToDefault(code, "未知"), StrUtil.blankToDefault(msg, "无"));
    }

    private JSONArray getDataList(JSONObject response) {
        JSONObject data = response.getJSONObject("data");
        if (data == null || !data.containsKey("list")) {
            return new JSONArray();
        }
        JSONArray list = data.getJSONArray("list");
        return list == null ? new JSONArray() : list;
    }

    private long[] resolveEventTypes(List<Long> eventTypes) {
        if (CollUtil.isNotEmpty(eventTypes)) {
            return eventTypes.stream().mapToLong(Long::longValue).toArray();
        }
        String configValue = StrUtil.trimToEmpty(properties.getEventTypes());
        List<String> tokens = StrUtil.splitTrim(configValue, ',');
        if (CollUtil.isEmpty(tokens)) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.ERROR_CONFIGURATION.getCode(),
                    "视频事件类型未配置，请检查 yz.video.hk.event-types");
        }
        return tokens.stream().mapToLong(Long::parseLong).toArray();
    }

    private int[] resolveEventLevels(List<Integer> eventLevels) {
        if (CollUtil.isNotEmpty(eventLevels)) {
            return eventLevels.stream().mapToInt(Integer::intValue).toArray();
        }
        String configValue = StrUtil.trimToEmpty(properties.getEventLevels());
        List<String> tokens = StrUtil.splitTrim(configValue, ',');
        if (CollUtil.isEmpty(tokens)) {
            return new int[]{0};
        }
        return tokens.stream().mapToInt(Integer::parseInt).toArray();
    }

    private String resolveEventDest(String eventDest) {
        String value = StrUtil.trimToNull(eventDest);
        if (value != null) {
            return value;
        }
        value = StrUtil.trimToNull(properties.getEventDest());
        if (value == null) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.ERROR_CONFIGURATION.getCode(),
                    "视频事件回调地址未配置，请检查 yz.video.hk.event-dest");
        }
        return value;
    }

    private void ensureHkConfigured() {
        if (!properties.isEnabled()) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.ERROR_CONFIGURATION.getCode(),
                    "视频监控能力已禁用，请检查配置 yz.video.hk.enabled");
        }
        if (StrUtil.hasBlank(artemisConfig.getHost(), artemisConfig.getAppKey(), artemisConfig.getAppSecret())) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.ERROR_CONFIGURATION.getCode(),
                    "海康参数未配置完整，请检查 yz.video.hk.host/app-key/app-secret");
        }
    }

    private String readBody(HttpServletRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception0(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR.getCode(),
                    "读取事件回调内容失败：{}", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseCallbackBody(String rawBody) {
        if (StrUtil.isBlank(rawBody)) {
            return MapUtil.<String, Object>builder(new LinkedHashMap<>())
                    .put("raw", "")
                    .build();
        }
        try {
            return JsonUtils.parseObject(rawBody, Map.class);
        } catch (Exception ex) {
            return MapUtil.<String, Object>builder(new LinkedHashMap<>())
                    .put("raw", rawBody)
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(JSONObject jsonObject) {
        return JsonUtils.parseObject(jsonObject.toString(), Map.class);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> rewritePreviewUrlResponse(Map<String, Object> result) {
        if (MapUtil.isEmpty(result)) {
            return result;
        }
        Object dataObj = result.get("data");
        if (!(dataObj instanceof Map<?, ?> dataMap)) {
            return result;
        }
        Object urlObj = dataMap.get("url");
        if (!(urlObj instanceof String previewUrl)) {
            return result;
        }
        String rewrittenUrl = rewritePreviewStreamUrl(previewUrl);
        if (StrUtil.equals(previewUrl, rewrittenUrl)) {
            return result;
        }
        ((Map<String, Object>) dataMap).put("url", rewrittenUrl);
        return result;
    }

    private String rewritePreviewStreamUrl(String previewUrl) {
        String publicBaseUrl = resolvePreviewBaseUrl();
        if (publicBaseUrl == null || StrUtil.isBlank(previewUrl)) {
            return previewUrl;
        }
        try {
            URI originalUri = URI.create(previewUrl);
            URI publicBaseUri = URI.create(publicBaseUrl);
            URI rewrittenUri = new URI(
                    StrUtil.blankToDefault(publicBaseUri.getScheme(), originalUri.getScheme()),
                    publicBaseUri.getUserInfo(),
                    StrUtil.blankToDefault(publicBaseUri.getHost(), originalUri.getHost()),
                    publicBaseUri.getPort(),
                    mergeUrlPath(publicBaseUri.getPath(), originalUri.getPath()),
                    originalUri.getQuery(),
                    originalUri.getFragment()
            );
            return rewrittenUri.toString();
        } catch (Exception ex) {
            log.warn("[rewritePreviewStreamUrl][重写预览流地址失败][previewUrl={}, publicBaseUrl={}]",
                    previewUrl, publicBaseUrl, ex);
            return previewUrl;
        }
    }

    private String resolvePreviewBaseUrl() {
        HttpServletRequest request = ServletUtils.getRequest();
        if (request != null) {
            if (shouldKeepOriginalPreviewUrl(request)) {
                return null;
            }
            String requestBaseUrl = resolvePreviewBaseUrl(request);
            if (StrUtil.isNotBlank(requestBaseUrl)) {
                return requestBaseUrl;
            }
        }
        return StrUtil.trimToNull(properties.getPreviewPublicBaseUrl());
    }

    String resolvePreviewBaseUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedHost = firstHeaderValue(request.getHeader("X-Forwarded-Host"));
        String forwardedPort = firstHeaderValue(request.getHeader("X-Forwarded-Port"));
        String forwardedProto = firstHeaderValue(request.getHeader("X-Forwarded-Proto"));
        if (shouldKeepOriginalPreviewUrl(request)) {
            return null;
        }
        String host = forwardedHost;
        if (StrUtil.isBlank(host)) {
            host = firstHeaderValue(request.getHeader("Host"));
        }
        String scheme = forwardedProto;
        if (StrUtil.isBlank(scheme)) {
            scheme = request.getScheme();
        }
        if (StrUtil.isBlank(host)) {
            host = buildHost(request.getServerName(), request.getServerPort(), scheme);
        }
        host = appendPortIfNecessary(host, forwardedPort, scheme);
        if (StrUtil.isBlank(host) || StrUtil.isBlank(scheme)) {
            return null;
        }
        return toWebSocketScheme(scheme) + "://" + host;
    }

    private String firstHeaderValue(String value) {
        return StrUtil.trim(StrUtil.subBefore(value, ",", false));
    }

    private String buildHost(String serverName, int serverPort, String scheme) {
        if (StrUtil.isBlank(serverName)) {
            return null;
        }
        if (serverPort <= 0 || isDefaultPort(scheme, serverPort)) {
            return serverName;
        }
        return serverName + ":" + serverPort;
    }

    private String appendPortIfNecessary(String host, String portText, String scheme) {
        if (StrUtil.isBlank(host) || StrUtil.isBlank(portText)) {
            return host;
        }
        if (host.contains(":")) {
            return host;
        }
        int port = Integer.parseInt(portText);
        if (port <= 0 || isDefaultPort(scheme, port)) {
            return host;
        }
        return host + ":" + port;
    }

    private boolean shouldKeepOriginalPreviewUrl(HttpServletRequest request) {
        String forwardedHost = firstHeaderValue(request.getHeader("X-Forwarded-Host"));
        String forwardedPort = firstHeaderValue(request.getHeader("X-Forwarded-Port"));
        String forwardedProto = firstHeaderValue(request.getHeader("X-Forwarded-Proto"));
        return StrUtil.isAllBlank(forwardedHost, forwardedPort, forwardedProto)
                && !isStandardEntryPort(request.getServerPort());
    }

    private boolean isStandardEntryPort(int port) {
        return port == 80 || port == 443;
    }

    private boolean isDefaultPort(String scheme, int port) {
        return ("http".equalsIgnoreCase(scheme) || "ws".equalsIgnoreCase(scheme)) && port == 80
                || ("https".equalsIgnoreCase(scheme) || "wss".equalsIgnoreCase(scheme)) && port == 443;
    }

    private String toWebSocketScheme(String scheme) {
        return "https".equalsIgnoreCase(scheme) || "wss".equalsIgnoreCase(scheme) ? "wss" : "ws";
    }

    private String mergeUrlPath(String basePath, String sourcePath) {
        String normalizedSourcePath = StrUtil.blankToDefault(sourcePath, "/");
        if (StrUtil.isBlank(basePath) || "/".equals(basePath)) {
            return normalizedSourcePath;
        }
        return StrUtil.removeSuffix(basePath, "/")
                + (StrUtil.startWith(normalizedSourcePath, "/") ? normalizedSourcePath : "/" + normalizedSourcePath);
    }

    private static String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static Integer parseNullableInteger(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean hasRegionChanged(YzVideoRegionDO local, YzVideoRegionDO remote) {
        return !Objects.equals(local.getName(), remote.getName())
                || !Objects.equals(local.getParentIndexCode(), remote.getParentIndexCode())
                || !Objects.equals(local.getTreeCode(), remote.getTreeCode());
    }

    private void updateRegionByIndexCode(YzVideoRegionDO region) {
        regionMapper.update(null, new LambdaUpdateWrapper<YzVideoRegionDO>()
                .eq(YzVideoRegionDO::getIndexCode, region.getIndexCode())
                .set(YzVideoRegionDO::getName, region.getName())
                .set(YzVideoRegionDO::getParentIndexCode, region.getParentIndexCode())
                .set(YzVideoRegionDO::getTreeCode, region.getTreeCode()));
    }

    private YzVideoCameraDO mergeCameraForPersist(YzVideoCameraDO local, YzVideoCameraDO remote) {
        YzVideoCameraDO target = new YzVideoCameraDO();
        target.setCameraIndexCode(remote.getCameraIndexCode());
        target.setCameraName(remote.getCameraName());
        target.setRegionIndexCode(remote.getRegionIndexCode());
        target.setChannelNo(remote.getChannelNo());
        target.setLon(remote.getLon());
        target.setLat(remote.getLat());
        target.setCameraType(remote.getCameraType());
        target.setCameraTypeName(remote.getCameraTypeName());
        target.setOnline(StrUtil.isBlank(remote.getOnline()) ? local.getOnline() : remote.getOnline());
        return target;
    }

    private boolean hasCameraChanged(YzVideoCameraDO local, YzVideoCameraDO target) {
        return !Objects.equals(local.getCameraName(), target.getCameraName())
                || !Objects.equals(local.getRegionIndexCode(), target.getRegionIndexCode())
                || !Objects.equals(local.getChannelNo(), target.getChannelNo())
                || !Objects.equals(local.getLon(), target.getLon())
                || !Objects.equals(local.getLat(), target.getLat())
                || !Objects.equals(local.getOnline(), target.getOnline())
                || !Objects.equals(local.getCameraType(), target.getCameraType())
                || !Objects.equals(local.getCameraTypeName(), target.getCameraTypeName());
    }

    private void updateCameraByIndexCode(YzVideoCameraDO camera) {
        cameraMapper.update(null, new LambdaUpdateWrapper<YzVideoCameraDO>()
                .eq(YzVideoCameraDO::getCameraIndexCode, camera.getCameraIndexCode())
                .set(YzVideoCameraDO::getCameraName, camera.getCameraName())
                .set(YzVideoCameraDO::getRegionIndexCode, camera.getRegionIndexCode())
                .set(YzVideoCameraDO::getChannelNo, camera.getChannelNo())
                .set(YzVideoCameraDO::getLon, camera.getLon())
                .set(YzVideoCameraDO::getLat, camera.getLat())
                .set(YzVideoCameraDO::getOnline, camera.getOnline())
                .set(YzVideoCameraDO::getCameraType, camera.getCameraType())
                .set(YzVideoCameraDO::getCameraTypeName, camera.getCameraTypeName()));
    }

    /**
     * 仅诊断海康 /api/resource/v1/cameras 是否返回经纬度，不参与业务逻辑。
     */
    private void logHikCameraGeoDiagnostics(String source, List<JSONObject> items, boolean useLongitudeFallback) {
        if (CollUtil.isEmpty(items)) {
            log.info("[HikCameraGeo][{}][summary] total=0, withLon=0, withLat=0, api=/api/resource/v1/cameras",
                    source);
            return;
        }
        long withLon = 0;
        long withLat = 0;
        JSONObject sampleWithGeo = null;
        JSONObject sampleWithoutGeo = null;
        for (JSONObject item : items) {
            if (item == null) {
                continue;
            }
            String lon = useLongitudeFallback
                    ? firstNotBlank(item.getStr("lon"), item.getStr("longitude"))
                    : item.getStr("lon");
            String lat = useLongitudeFallback
                    ? firstNotBlank(item.getStr("lat"), item.getStr("latitude"))
                    : item.getStr("lat");
            if (StrUtil.isNotBlank(lon)) {
                withLon++;
                if (sampleWithGeo == null) {
                    sampleWithGeo = item;
                }
            } else if (sampleWithoutGeo == null) {
                sampleWithoutGeo = item;
            }
            if (StrUtil.isNotBlank(lat)) {
                withLat++;
            }
        }
        log.info("[HikCameraGeo][{}][summary] total={}, withLon={}, withLat={}, api=/api/resource/v1/cameras, "
                        + "useLongitudeFallback={}",
                source, items.size(), withLon, withLat, useLongitudeFallback);
        if (sampleWithGeo != null) {
            log.info("[HikCameraGeo][{}][sample-with-geo] indexCode={}, lon={}, longitude={}, lat={}, latitude={}",
                    source, sampleWithGeo.getStr("cameraIndexCode"), sampleWithGeo.getStr("lon"),
                    sampleWithGeo.getStr("longitude"), sampleWithGeo.getStr("lat"), sampleWithGeo.getStr("latitude"));
        }
        if (sampleWithoutGeo != null) {
            log.info("[HikCameraGeo][{}][sample-without-geo] indexCode={}, name={}, lon={}, longitude={}, lat={}, "
                            + "latitude={}",
                    source, sampleWithoutGeo.getStr("cameraIndexCode"), sampleWithoutGeo.getStr("cameraName"),
                    sampleWithoutGeo.getStr("lon"), sampleWithoutGeo.getStr("longitude"),
                    sampleWithoutGeo.getStr("lat"), sampleWithoutGeo.getStr("latitude"));
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
