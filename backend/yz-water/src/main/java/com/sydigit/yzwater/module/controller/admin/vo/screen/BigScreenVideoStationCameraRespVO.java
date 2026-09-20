package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 大屏统计-视频监控 站点及监控设备响应
 */
@Data
@Schema(description = "大屏统计-视频监控 站点及监控设备响应")
public class BigScreenVideoStationCameraRespVO {

    @Schema(description = "闸站(视频区域)编码")
    private String regionIndexCode;

    @Schema(description = "闸站名称")
    private String stationName;

    @Schema(description = "监控设备数量")
    private Integer cameraCount;

    @Schema(description = "监控设备列表")
    private List<CameraItem> cameraList;

    @Data
    @Schema(description = "监控设备信息")
    public static class CameraItem {

        @Schema(description = "监控设备编码")
        private String cameraIndexCode;

        @Schema(description = "监控设备名称")
        private String cameraName;

        @Schema(description = "在线状态")
        private String online;
    }
}
