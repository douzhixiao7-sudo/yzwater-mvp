package com.sydigit.yzwater.module.iot.job.openapi;

import com.sydigit.yzwater.module.config.video.YzVideoHkProperties;
import com.sydigit.yzwater.module.iot.service.openapi.XfhhOpenApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XfhhVideoSyncJobTest {

    @Mock
    private XfhhOpenApiService xfhhOpenApiService;
    @Mock
    private YzVideoHkProperties yzVideoHkProperties;

    @InjectMocks
    private XfhhVideoSyncJob job;

    @Test
    void execute_shouldSyncRegionsCamerasAndOnlineInOrder() {
        when(yzVideoHkProperties.isEnabled()).thenReturn(true);
        when(xfhhOpenApiService.syncVideoRegions()).thenReturn(true);
        when(xfhhOpenApiService.syncVideoCameras()).thenReturn(true);

        String result = job.execute(null);

        InOrder inOrder = inOrder(xfhhOpenApiService);
        inOrder.verify(xfhhOpenApiService).syncVideoRegions();
        inOrder.verify(xfhhOpenApiService).syncVideoCameras();
        inOrder.verify(xfhhOpenApiService).syncCameraOnline();
        assertTrue(result.contains("区域同步成功"));
        assertTrue(result.contains("点位同步成功"));
        assertTrue(result.contains("在线状态同步成功"));
    }

    @Test
    void execute_shouldStopWhenRegionSyncFailed() {
        when(yzVideoHkProperties.isEnabled()).thenReturn(true);
        when(xfhhOpenApiService.syncVideoRegions()).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> job.execute(null));

        verify(xfhhOpenApiService, never()).syncVideoCameras();
        verify(xfhhOpenApiService, never()).syncCameraOnline();
        assertEquals("XFHH视频同步失败：区域同步失败，未继续执行点位和在线状态同步", exception.getMessage());
    }

    @Test
    void execute_shouldStopWhenCameraSyncFailed() {
        when(yzVideoHkProperties.isEnabled()).thenReturn(true);
        when(xfhhOpenApiService.syncVideoRegions()).thenReturn(true);
        when(xfhhOpenApiService.syncVideoCameras()).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> job.execute(null));

        verify(xfhhOpenApiService).syncVideoRegions();
        verify(xfhhOpenApiService).syncVideoCameras();
        verify(xfhhOpenApiService, never()).syncCameraOnline();
        assertEquals("XFHH视频同步失败：区域同步成功，点位同步失败，未继续执行在线状态同步", exception.getMessage());
    }

    @Test
    void execute_shouldSkipWhenHkDisabled() {
        when(yzVideoHkProperties.isEnabled()).thenReturn(false);

        String result = job.execute(null);

        verifyNoInteractions(xfhhOpenApiService);
        assertEquals("XFHH视频同步跳过：海康视频能力未启用", result);
    }
}
