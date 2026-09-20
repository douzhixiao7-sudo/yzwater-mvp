package com.sydigit.yzwater.module.iot.controller.admin.openapi;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoEventSubscribeReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPlaybackUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPreviewUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPtzControlReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhAlertLogListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhLegacyResult;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhOpenDataIdReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhUserCenterAccessTokenReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhUserCenterUserinfoReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhVideoCameraByRegionListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhWaterLevelDataListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhWaterQualityAlertListReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhWaterQualityDataListReqVO;
import com.sydigit.yzwater.module.iot.service.openapi.XfhhOpenApiService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 幸福河湖开放接口 Controller。
 *
 * 保留当前控制器类名，对外同时兼容旧项目真实路径与 Swagger 友好的显式别名接口。
 */
@Tag(name = "开放接口 - 幸福河湖")
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/jm-data")
public class XfhhOpenController {
    private final XfhhOpenApiService xfhhOpenApiService;

    /**
     * 旧项目脚本类列表接口。
     */
    @PermitAll
    @PostMapping("/{appId}/{code}/list")
    @Operation(summary = "幸福河湖平台-旧协议脚本列表接口")
    public XfhhLegacyResult<Object> legacyList(@PathVariable("appId") String appId,
                                               @PathVariable("code") String code,
                                               @RequestBody(required = false) Map<String, Object> args) {
        try {
            return XfhhLegacyResult.ok(xfhhOpenApiService.list(appId, code, args));
        } catch (IllegalArgumentException ex) {
            return XfhhLegacyResult.error(ex.getMessage());
        }
    }

    /**
     * 水位站信息。
     */
    @PermitAll
    @PostMapping("/xfhh/monitoring-station/list")
    @Operation(summary = "幸福河湖平台-水位站信息")
    public XfhhLegacyResult<Object> getMonitoringStations(@RequestBody(required = false) XfhhOpenDataIdReqVO ignored) {
        return executeList(XfhhOpenApiConstants.CODE_MONITORING_STATION_LIST, Collections.emptyMap());
    }

    /**
     * 水位计信息。
     */
    @PermitAll
    @PostMapping("/xfhh/water-gauge/list")
    @Operation(summary = "幸福河湖平台-水位计信息")
    public XfhhLegacyResult<Object> getWaterGauges(@RequestBody(required = false) XfhhOpenDataIdReqVO ignored) {
        return executeList(XfhhOpenApiConstants.CODE_WATER_GAUGE_LIST, Collections.emptyMap());
    }

    /**
     * 水位监测数据。
     */
    @PermitAll
    @PostMapping("/xfhh/water-level-data/list")
    @Operation(summary = "幸福河湖平台-水位监测数据",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "请求示例", value = "{\n"
                            + "  \"id\": \"fb8cd0fe-addf-4364-82a1-8a0b11a9440c\",\n"
                            + "  \"params\": {\n"
                            + "    \"gaugeId\": 101,\n"
                            + "    \"bgnAlertTime\": \"2026-04-01 00:00:00\",\n"
                            + "    \"endAlertTime\": \"2026-04-02 00:00:00\"\n"
                            + "  }\n"
                            + "}"))))
    public XfhhLegacyResult<Object> getWaterLevelData(@Valid @RequestBody XfhhWaterLevelDataListReqVO reqVO) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("gaugeId", reqVO.getParams().getGaugeId());
        params.put("bgnRecordTime", reqVO.getParams().getBgnRecordTime());
        params.put("endRecordTime", reqVO.getParams().getEndRecordTime());
        return executeList(XfhhOpenApiConstants.CODE_WATER_LEVEL_DATA_LIST, params);
    }

    /**
     * 水位报警记录。
     */
    @PermitAll
    @PostMapping("/xfhh/alert-log/list")
    @Operation(summary = "幸福河湖平台-水位报警记录",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "请求示例", value = "{\n"
                            + "  \"id\": \"fb8cd0fe-addf-4364-82a1-8a0b11a9440c\",\n"
                            + "  \"params\": {\n"
                            + "    \"gaugeId\": 101,\n"
                            + "    \"bgnAlertTime\": \"2026-04-01 00:00:00\",\n"
                            + "    \"endAlertTime\": \"2026-04-02 00:00:00\"\n"
                            + "  }\n"
                            + "}"))))
    public XfhhLegacyResult<Object> getAlertLogs(@Valid @RequestBody XfhhAlertLogListReqVO reqVO) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("gaugeId", reqVO.getParams().getGaugeId());
        params.put("bgnAlertTime", reqVO.getParams().getBgnAlertTime());
        params.put("endAlertTime", reqVO.getParams().getEndAlertTime());
        return executeList(XfhhOpenApiConstants.CODE_ALERT_LOG_LIST, params);
    }

    /**
     * 水质站点信息。
     */
    @PermitAll
    @PostMapping("/xfhh/water-quality-station/list")
    @Operation(summary = "幸福河湖平台-水质站点信息")
    public XfhhLegacyResult<Object> getWaterQualityStations(@RequestBody(required = false) XfhhOpenDataIdReqVO ignored) {
        return executeList(XfhhOpenApiConstants.CODE_WATER_QUALITY_STATION_LIST, Collections.emptyMap());
    }

    /**
     * 水质参数信息。
     */
    @PermitAll
    @PostMapping("/xfhh/water-quality-parameter/list")
    @Operation(summary = "幸福河湖平台-水质参数信息")
    public XfhhLegacyResult<Object> getWaterQualityParameters(@RequestBody(required = false) XfhhOpenDataIdReqVO ignored) {
        return executeList(XfhhOpenApiConstants.CODE_WATER_QUALITY_PARAMETER_LIST, Collections.emptyMap());
    }

    /**
     * 水质监测数据。
     */
    @PermitAll
    @PostMapping("/xfhh/water-quality-data/list")
    @Operation(summary = "幸福河湖平台-水质监测数据",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "请求示例", value = "{\n"
                            + "  \"id\": \"fb8cd0fe-addf-4364-82a1-8a0b11a9440c\",\n"
                            + "  \"params\": {\n"
                            + "    \"stationId\": 4,\n"
                            + "    \"bgnRecordTime\": \"2026-04-01 00:00:00\",\n"
                            + "    \"endRecordTime\": \"2026-04-02 00:00:00\"\n"
                            + "  }\n"
                            + "}"))))
    public XfhhLegacyResult<Object> getWaterQualityData(@Valid @RequestBody XfhhWaterQualityDataListReqVO reqVO) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("stationId", reqVO.getParams().getStationId());
        params.put("bgnRecordTime", reqVO.getParams().getBgnRecordTime());
        params.put("endRecordTime", reqVO.getParams().getEndRecordTime());
        return executeList(XfhhOpenApiConstants.CODE_WATER_QUALITY_DATA_LIST, params);
    }

    /**
     * 水质报警记录。
     */
    @PermitAll
    @PostMapping("/xfhh/water-quality-alert/list")
    @Operation(summary = "幸福河湖平台-水质报警记录",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "请求示例", value = "{\n"
                            + "  \"id\": \"fb8cd0fe-addf-4364-82a1-8a0b11a9440c\",\n"
                            + "  \"params\": {\n"
                            + "    \"stationId\": 4,\n"
                            + "    \"bgnAlertTime\": \"2026-04-01 00:00:00\",\n"
                            + "    \"endAlertTime\": \"2026-04-02 00:00:00\"\n"
                            + "  }\n"
                            + "}"))))
    public XfhhLegacyResult<Object> getWaterQualityAlerts(@Valid @RequestBody XfhhWaterQualityAlertListReqVO reqVO) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("stationId", reqVO.getParams().getStationId());
        params.put("bgnAlertTime", reqVO.getParams().getBgnAlertTime());
        params.put("endAlertTime", reqVO.getParams().getEndAlertTime());
        return executeList(XfhhOpenApiConstants.CODE_WATER_QUALITY_ALERT_LIST, params);
    }

    /**
     * 水质阈值配置。
     */
    @PermitAll
    @PostMapping("/xfhh/water-quality-threshold/list")
    @Operation(summary = "幸福河湖平台-水质阈值配置")
    public XfhhLegacyResult<Object> getWaterQualityThresholds(@RequestBody(required = false) XfhhOpenDataIdReqVO ignored) {
        return executeList(XfhhOpenApiConstants.CODE_WATER_QUALITY_THRESHOLD_LIST, Collections.emptyMap());
    }

    /**
     * 视频区域列表。
     */
    @PermitAll
    @PostMapping("/xfhh/video-region/list")
    @Operation(summary = "幸福河湖平台-视频区域列表")
    public XfhhLegacyResult<Object> getVideoRegions(@RequestBody(required = false) XfhhOpenDataIdReqVO ignored) {
        return executeList(XfhhOpenApiConstants.CODE_VIDEO_REGIONS_LIST, Collections.emptyMap());
    }

    /**
     * 按区域获取监控点设备信息。
     */
    @PermitAll
    @PostMapping("/xfhh/video-cameras/list")
    @Operation(summary = "幸福河湖平台-按区域获取监控点设备信息")
    public XfhhLegacyResult<Object> getVideoCameras(@Valid @RequestBody XfhhVideoCameraByRegionListReqVO reqVO) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("regionIndexCode", reqVO.getParams().getRegionIndexCode());
        return executeList(XfhhOpenApiConstants.CODE_VIDEO_CAMERAS_BY_REGION_LIST, params);
    }

    /**
     * 同步视频区域。
     */
    @PermitAll
    @PostMapping("/yz/video/callPostApiGetRegions")
    @Operation(summary = "幸福河湖平台-同步视频区域")
    public XfhhLegacyResult<Void> callPostApiGetRegions() {
        return xfhhOpenApiService.syncVideoRegions() ? XfhhLegacyResult.ok() : XfhhLegacyResult.error();
    }

    /**
     * 同步视频点位。
     */
    @PermitAll
    @PostMapping("/yz/video/callPostApiGetCameras")
    @Operation(summary = "幸福河湖平台-同步视频点位")
    public XfhhLegacyResult<Void> callPostApiGetCameras() {
        return xfhhOpenApiService.syncVideoCameras() ? XfhhLegacyResult.ok() : XfhhLegacyResult.error();
    }

    /**
     * 获取视频预览流。
     */
    @PermitAll
    @PostMapping("/yz/video/callPostApiGetPreviewURLs")
    @Operation(summary = "幸福河湖平台-获取视频预览流")
    public XfhhLegacyResult<String> callPostApiGetPreviewURLs(@Valid @RequestBody VideoPreviewUrlReqVO reqVO) {
        String result = xfhhOpenApiService.getPreviewUrl(reqVO);
        return StrUtil.isNotBlank(result) ? XfhhLegacyResult.ok(result) : XfhhLegacyResult.error();
    }

    /**
     * 获取视频回放流。
     */
    @PermitAll
    @PostMapping("/yz/video/callPostApiPlaybackURLs")
    @Operation(summary = "幸福河湖平台-获取视频回放流")
    public XfhhLegacyResult<String> callPostApiPlaybackURLs(@Valid @RequestBody VideoPlaybackUrlReqVO reqVO) {
        String result = xfhhOpenApiService.getPlaybackUrl(reqVO);
        return StrUtil.isNotBlank(result) ? XfhhLegacyResult.ok(result) : XfhhLegacyResult.error();
    }

    /**
     * 同步视频在线状态。
     */
    @PermitAll
    @PostMapping("/yz/video/getCameraOnline")
    @Operation(summary = "幸福河湖平台-同步视频在线状态")
    public void getCameraOnline() {
        xfhhOpenApiService.syncCameraOnline();
    }

    /**
     * 云台控制。
     */
    @PermitAll
    @PostMapping("/yz/video/controlCameras")
    @Operation(summary = "幸福河湖平台-云台控制")
    public XfhhLegacyResult<Void> controlCameras(@Valid @RequestBody VideoPtzControlReqVO reqVO) {
        return xfhhOpenApiService.controlCameras(reqVO) ? XfhhLegacyResult.ok() : XfhhLegacyResult.error();
    }

    /**
     * 接收海康事件回调。
     */
    @PermitAll
    @PostMapping("/yz/video/eventRcv")
    @Operation(summary = "幸福河湖平台-接收视频事件回调")
    public XfhhLegacyResult<Void> eventRcv(HttpServletRequest request) {
        return xfhhOpenApiService.eventRcv(request) ? XfhhLegacyResult.ok() : XfhhLegacyResult.error();
    }

    /**
     * 订阅视频事件。
     */
    @PermitAll
    @PostMapping("/yz/video/eventSubscriptionByEventTypes")
    @Operation(summary = "幸福河湖平台-订阅视频事件")
    public XfhhLegacyResult<String> eventSubscriptionByEventTypes(@RequestBody(required = false) VideoEventSubscribeReqVO reqVO) {
        return XfhhLegacyResult.ok(xfhhOpenApiService.eventSubscriptionByEventTypes(reqVO));
    }

    /**
     * 根据授权码换取 accessToken。
     */
    @PermitAll
    @PostMapping("/yz/usercenter/getAccessToken")
    @Operation(summary = "幸福河湖平台-用户中心换取 accessToken")
    public XfhhLegacyResult<String> getAccessToken(@Valid @RequestBody XfhhUserCenterAccessTokenReqVO reqVO) {
        return XfhhLegacyResult.ok(xfhhOpenApiService.getAccessToken(reqVO));
    }

    /**
     * 根据 accessToken 获取用户信息。
     */
    @PermitAll
    @PostMapping("/yz/usercenter/getUserinfo")
    @Operation(summary = "幸福河湖平台-用户中心获取用户信息")
    public XfhhLegacyResult<String> getUserinfo(@Valid @RequestBody XfhhUserCenterUserinfoReqVO reqVO) {
        return XfhhLegacyResult.ok(xfhhOpenApiService.getUserinfo(reqVO));
    }

    private XfhhLegacyResult<Object> executeList(String code, Map<String, Object> params) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("params", params);
        return XfhhLegacyResult.ok(xfhhOpenApiService.list(XfhhOpenApiConstants.DEFAULT_APP_ID, code, args));
    }
}
