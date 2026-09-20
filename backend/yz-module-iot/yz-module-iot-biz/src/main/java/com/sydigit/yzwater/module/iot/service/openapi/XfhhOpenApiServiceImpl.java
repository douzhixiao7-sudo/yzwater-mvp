package com.sydigit.yzwater.module.iot.service.openapi;

import com.sydigit.yzwater.module.controller.admin.vo.video.VideoEventSubscribeReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPlaybackUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPreviewUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPtzControlReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.XfhhOpenApiConstants;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhUserCenterAccessTokenReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhUserCenterUserinfoReqVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 幸福河湖开放接口 Service 实现。
 */
@Service
@Validated
@RequiredArgsConstructor
public class XfhhOpenApiServiceImpl implements XfhhOpenApiService {

    private final XfhhMysqlQueryService xfhhMysqlQueryService;
    private final XfhhVideoBridgeService xfhhVideoBridgeService;
    private final XfhhUserCenterClient userCenterClient;

    @Override
    public Object list(String appId, String code, Map<String, Object> args) {
        if (!XfhhOpenApiConstants.DEFAULT_APP_ID.equals(appId)) {
            throw new IllegalArgumentException("不支持的应用ID：" + appId);
        }
        return xfhhMysqlQueryService.list(code, extractParams(args));
    }

    @Override
    public boolean syncVideoRegions() {
        return xfhhVideoBridgeService.syncRegions();
    }

    @Override
    public boolean syncVideoCameras() {
        return xfhhVideoBridgeService.syncCameras();
    }

    @Override
    public String getPreviewUrl(VideoPreviewUrlReqVO reqVO) {
        return xfhhVideoBridgeService.getPreviewUrl(reqVO);
    }

    @Override
    public String getPlaybackUrl(VideoPlaybackUrlReqVO reqVO) {
        return xfhhVideoBridgeService.getPlaybackUrl(reqVO);
    }

    @Override
    public void syncCameraOnline() {
        xfhhVideoBridgeService.syncCameraOnline();
    }

    @Override
    public boolean controlCameras(VideoPtzControlReqVO reqVO) {
        return xfhhVideoBridgeService.controlCameras(reqVO);
    }

    @Override
    public boolean eventRcv(HttpServletRequest request) {
        return xfhhVideoBridgeService.receiveEvent(request);
    }

    @Override
    public String eventSubscriptionByEventTypes(VideoEventSubscribeReqVO reqVO) {
        return xfhhVideoBridgeService.subscribeEvents(reqVO);
    }

    @Override
    public String getAccessToken(XfhhUserCenterAccessTokenReqVO reqVO) {
        return userCenterClient.getAccessToken(reqVO.getCode(), reqVO.getRedirectUri());
    }

    @Override
    public String getUserinfo(XfhhUserCenterUserinfoReqVO reqVO) {
        return userCenterClient.getUserinfo(reqVO.getAccessToken());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParams(Map<String, Object> args) {
        if (args == null || args.isEmpty()) {
            return Collections.emptyMap();
        }
        Object params = args.get("params");
        if (!(params instanceof Map<?, ?> rawMap)) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        rawMap.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }
}
