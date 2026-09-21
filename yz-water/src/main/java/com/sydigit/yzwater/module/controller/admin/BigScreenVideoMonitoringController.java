package com.sydigit.yzwater.module.controller.admin;

import com.sydigit.yzwater.framework.common.pojo.CommonResult;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenVideoDeviceSummaryRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenVideoPreviewReqVO;
import com.sydigit.yzwater.module.controller.admin.vo.screen.BigScreenVideoStationCameraRespVO;
import com.sydigit.yzwater.module.controller.admin.vo.video.VideoPtzControlReqVO;
import com.sydigit.yzwater.module.service.screen.BigScreenVideoMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.sydigit.yzwater.framework.common.pojo.CommonResult.success;

/**
 * 大屏统计-视频监控
 */
@Tag(name = "大屏统计-视频监控")
@RestController
@RequestMapping("/screen/statistics/video-monitoring")
@Validated
@RequiredArgsConstructor
public class BigScreenVideoMonitoringController {

    private final BigScreenVideoMonitoringService videoMonitoringService;

    @GetMapping("/station-camera-list")
    @Operation(summary = "查询各闸站及对应监控设备")
    @PermitAll
    public CommonResult<List<BigScreenVideoStationCameraRespVO>> getStationCameraList() {
        return success(videoMonitoringService.getStationCameraList());
    }

    @GetMapping("/device-summary")
    @Operation(summary = "视频监控设备统计汇总")
    @PermitAll
    public CommonResult<BigScreenVideoDeviceSummaryRespVO> getDeviceSummary() {
        return success(videoMonitoringService.getDeviceSummary());
    }

    @PostMapping("/control-cameras")
    @Operation(summary = "控制监控设备云台（上下左右、焦距、缩放、速度）")
    @PermitAll
    public CommonResult<Map<String, Object>> controlCameras(@Valid @RequestBody VideoPtzControlReqVO reqVO) {
        return success(videoMonitoringService.controlCamera(reqVO));
    }

    @PostMapping("/preview-url")
    @Operation(summary = "点击监控设备获取视频预览流")
    @PermitAll
    public CommonResult<Map<String, Object>> getPreviewUrl(@Valid @RequestBody BigScreenVideoPreviewReqVO reqVO) {
        return success(videoMonitoringService.getPreviewUrl(reqVO));
    }
}
