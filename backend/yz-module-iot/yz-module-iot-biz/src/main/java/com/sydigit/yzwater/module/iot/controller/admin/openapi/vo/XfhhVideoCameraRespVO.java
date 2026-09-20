package com.sydigit.yzwater.module.iot.controller.admin.openapi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 幸福河湖平台-监控点设备信息响应
 */
@Data
@Schema(description = "幸福河湖平台-监控点设备信息响应")
public class XfhhVideoCameraRespVO {

    @Schema(description = "摄像头编码", example = "camera-1")
    private String cameraIndexCode;

    @Schema(description = "摄像头名称", example = "一号球机")
    private String cameraName;

    @Schema(description = "在线状态", example = "1")
    private String online;
}
