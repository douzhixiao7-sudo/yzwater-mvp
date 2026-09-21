package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoEventSubscribeReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPlaybackUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPreviewUrlReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPtzControlReqVO;
import com.sydigit.yzwater.module.service.video.YzVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 视频监控
 */
@Tag(name = "管理后台 - 视频监控")
@RestController
@RequestMapping("/video")
@Validated
@RequiredArgsConstructor
public class YzVideoController {

    private final YzVideoService videoService;

    @PostMapping("/sync-regions")
    @Operation(summary = "同步视频区域")
    @PermitAll
    public CommonResult<Map<String, Object>> syncRegions() {
        return success(videoService.syncRegions());
    }

    @PostMapping("/sync-cameras")
    @Operation(summary = "同步视频摄像头")
    @PermitAll
    public CommonResult<Map<String, Object>> syncCameras() {
        return success(videoService.syncCameras());
    }

    @PostMapping("/preview-url")
    @Operation(summary = "获取预览流地址")
    @PermitAll
    public CommonResult<Map<String, Object>> previewUrl(@Valid @RequestBody VideoPreviewUrlReqVO reqVO) {
        return success(videoService.getPreviewUrl(reqVO));
    }

    @PostMapping("/playback-url")
    @Operation(summary = "获取回放流地址")
    @PermitAll
    public CommonResult<Map<String, Object>> playbackUrl(@Valid @RequestBody VideoPlaybackUrlReqVO reqVO) {
        return success(videoService.getPlaybackUrl(reqVO));
    }

    @PostMapping("/sync-online-status")
    @Operation(summary = "同步摄像头在线状态")
    @PermitAll
    public CommonResult<Map<String, Object>> syncOnlineStatus() {
        return success(videoService.syncCameraOnlineStatus());
    }

    @PostMapping("/ptz-control")
    @Operation(summary = "云台控制")
    @PermitAll
    public CommonResult<Map<String, Object>> ptzControl(@Valid @RequestBody VideoPtzControlReqVO reqVO) {
        return success(videoService.controlCamera(reqVO));
    }

    @PostMapping("/event-subscribe")
    @Operation(summary = "订阅海康事件")
    @PermitAll
    public CommonResult<Map<String, Object>> eventSubscribe(
            @RequestBody(required = false) VideoEventSubscribeReqVO reqVO) {
        return success(videoService.subscribeEvents(reqVO));
    }

    @PostMapping("/event-rcv")
    @Operation(summary = "海康事件回调入库")
    @PermitAll
    public CommonResult<Map<String, Object>> eventRcv(HttpServletRequest request) {
        return success(videoService.receiveEvent(request));
    }
}

