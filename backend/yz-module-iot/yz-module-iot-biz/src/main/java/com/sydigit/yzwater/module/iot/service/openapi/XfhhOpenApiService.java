package com.sydigit.yzwater.module.iot.service.openapi;

import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPlaybackUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoEventSubscribeReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPreviewUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPtzControlReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhUserCenterAccessTokenReqVO;
import com.sydigit.yzwater.module.iot.controller.admin.openapi.vo.XfhhUserCenterUserinfoReqVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 幸福河湖开放接口 Service
 */
public interface XfhhOpenApiService {

    /**
     * 按旧脚本编码查询列表数据。
     */
    Object list(String appId, String code, Map<String, Object> args);

    /**
     * 同步视频区域。
     */
    boolean syncVideoRegions();

    /**
     * 同步视频点位。
     */
    boolean syncVideoCameras();

    /**
     * 获取视频预览流
     */
    String getPreviewUrl(VideoPreviewUrlReqVO reqVO);

    /**
     * 获取视频回放流
     */
    String getPlaybackUrl(VideoPlaybackUrlReqVO reqVO);

    /**
     * 同步视频在线状态。
     */
    void syncCameraOnline();

    /**
     * 云台控制。
     */
    boolean controlCameras(VideoPtzControlReqVO reqVO);

    /**
     * 接收事件回调。
     */
    boolean eventRcv(HttpServletRequest request);

    /**
     * 订阅视频事件。
     */
    String eventSubscriptionByEventTypes(VideoEventSubscribeReqVO reqVO);

    /**
     * 获取 accessToken
     */
    String getAccessToken(XfhhUserCenterAccessTokenReqVO reqVO);

    /**
     * 获取用户名
     */
    String getUserinfo(XfhhUserCenterUserinfoReqVO reqVO);
}
