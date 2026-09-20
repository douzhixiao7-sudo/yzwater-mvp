package com.sydigit.yzwater.module.iot.controller.admin.runlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运行日志默认信息响应
 */
@Schema(description = "IoT - 运行日志默认信息 Response VO")
@Data
public class IotRunLogDefaultRespVO {

    @Schema(description = "值班班组 ID")
    private Long dutyTeamId;

    @Schema(description = "值班班组")
    private String dutyTeamName;

    @Schema(description = "记录人用户 ID")
    private Long recorderUserId;

    @Schema(description = "记录人")
    private String recorderUserName;

    @Schema(description = "记录时间")
    private LocalDateTime recordTime;
}
