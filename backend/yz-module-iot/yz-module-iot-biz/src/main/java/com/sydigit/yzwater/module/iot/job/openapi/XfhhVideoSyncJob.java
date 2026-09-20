package com.sydigit.yzwater.module.iot.job.openapi;

import cn.hutool.core.util.StrUtil;
import com.sydigit.yzwater.framework.quartz.core.handler.JobHandler;
import com.sydigit.yzwater.module.config.video.YzVideoHkProperties;
import com.sydigit.yzwater.module.iot.service.openapi.XfhhOpenApiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * XFHH 视频区域、点位和在线状态同步任务
 */
@Slf4j
@Component("xfhhVideoSyncJob")
public class XfhhVideoSyncJob implements JobHandler {

    @Resource
    private XfhhOpenApiService xfhhOpenApiService;
    @Resource
    private YzVideoHkProperties yzVideoHkProperties;

    @Override
    public String execute(String param) {
        log.info("[execute][开始执行 XFHH 视频同步任务]");
        if (!yzVideoHkProperties.isEnabled()) {
            String result = "XFHH视频同步跳过：海康视频能力未启用";
            log.info("[execute][XFHH 视频同步任务跳过][result={}]", result);
            return result;
        }

        boolean regionSuccess = xfhhOpenApiService.syncVideoRegions();
        if (!regionSuccess) {
            throw new IllegalStateException("XFHH视频同步失败：区域同步失败，未继续执行点位和在线状态同步");
        }

        boolean cameraSuccess = xfhhOpenApiService.syncVideoCameras();
        if (!cameraSuccess) {
            throw new IllegalStateException("XFHH视频同步失败：区域同步成功，点位同步失败，未继续执行在线状态同步");
        }

        xfhhOpenApiService.syncCameraOnline();
        String result = StrUtil.format("XFHH视频同步完成：区域同步成功，点位同步成功，在线状态同步成功");
        log.info("[execute][XFHH 视频同步任务执行完成][result={}]", result);
        return result;
    }
}
