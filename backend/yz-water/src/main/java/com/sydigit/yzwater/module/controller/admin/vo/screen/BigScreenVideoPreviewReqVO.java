package com.sydigit.yzwater.module.controller.admin.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 大屏统计-视频监控 预览流请求参数
 */
@Data
@Schema(description = "大屏统计-视频监控 预览流请求参数")
public class BigScreenVideoPreviewReqVO {

    @Schema(description = "监控设备编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "cameraIndexCode 不能为空")
    private String cameraIndexCode;

    @Schema(description = "码流类型：0 主码流，1 子码流，2 第三码流", example = "0")
    private Integer streamType;

    @Schema(description = "取流协议，默认 ws", example = "ws")
    private String protocol;

    @Schema(description = "传输协议：0 UDP，1 TCP，默认 1", example = "1")
    private Integer transmode;

    @Schema(description = "扩展参数，默认 transcode=0", example = "transcode=0")
    private String expand;

    @Schema(description = "封装格式，默认 ps", example = "ps")
    private String streamform;
}
