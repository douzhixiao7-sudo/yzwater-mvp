package com.sydigit.yzwater.module.controller.admin.vo.video;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 视频事件订阅请求
 */
@Data
@Schema(description = "管理后台 - 视频事件订阅请求")
public class VideoEventSubscribeReqVO {

    @Schema(description = "事件回调地址，不传则使用配置 yz.video.hk.event-dest")
    private String eventDest;

    @Schema(description = "事件类型列表，不传则使用配置 yz.video.hk.event-types", example = "[42200211236000]")
    private List<Long> eventTypes;

    @Schema(description = "事件级别列表，不传则使用配置 yz.video.hk.event-levels", example = "[0]")
    private List<Integer> eventLevels;

    @Schema(description = "订阅类型，默认 0", example = "0")
    private Integer subType;
}

