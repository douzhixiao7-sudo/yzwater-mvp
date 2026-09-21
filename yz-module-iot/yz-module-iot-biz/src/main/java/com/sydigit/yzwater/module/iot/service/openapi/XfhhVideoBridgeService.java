package com.sydigit.yzwater.module.iot.service.openapi;

import com.sydigit.yzwater.module.controller.admin.vo.video.VideoEventSubscribeReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPlaybackUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPreviewUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPtzControlReqVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 幸福河湖视频桥接服务。
 */
public interface XfhhVideoBridgeService {

    boolean syncRegions();

    boolean syncCameras();

    String getPreviewUrl(VideoPreviewUrlReqVO reqVO);

    String getPlaybackUrl(VideoPlaybackUrlReqVO reqVO);

    void syncCameraOnline();

    boolean controlCameras(VideoPtzControlReqVO reqVO);

    boolean receiveEvent(HttpServletRequest request);

    String subscribeEvents(VideoEventSubscribeReqVO reqVO);
}
