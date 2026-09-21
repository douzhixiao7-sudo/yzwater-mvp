package com.sydigit.yzwater.module.controller.admin.vo.video;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 视频回放地址请求
 */
@Data
@Schema(description = "管理后台 - 视频回放地址请求")
public class VideoPlaybackUrlReqVO {

    @Schema(description = "监控点编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "cameraIndexCode 不能为空")
    private String cameraIndexCode;

    @Schema(description = "开始时间（ISO8601）", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2026-03-24T00:00:00.000+08:00")
    @NotBlank(message = "beginTime 不能为空")
    private String beginTime;

    @Schema(description = "结束时间（ISO8601）", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2026-03-24T23:59:59.000+08:00")
    @NotBlank(message = "endTime 不能为空")
    private String endTime;

    @Schema(description = "存储类型，0中心存储 1设备存储，默认 0", example = "0")
    private Integer recordLocation;

    @Schema(description = "取流协议，默认 ws", example = "ws")
    private String protocol;

    @Schema(description = "传输协议，0 UDP 1 TCP，默认 1", example = "1")
    private Integer transmode;

    @Schema(description = "分页 UUID，默认空字符串")
    private String uuid;

    @Schema(description = "扩展参数，默认 transcode=0", example = "transcode=0")
    private String expand;

    @Schema(description = "封装格式，默认 ps", example = "ps")
    private String streamform;

    @Schema(description = "锁定类型，0全部 1未锁定 2已锁定，默认 0", example = "0")
    private Integer lockType;
}

