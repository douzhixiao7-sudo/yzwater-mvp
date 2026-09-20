package com.sydigit.yzwater.module.iot.controller.admin.devicerating.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 设备评级 Response VO")
@Data
public class IotDeviceRatingRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "评级时间")
    private LocalDate ratingTime;

    @Schema(description = "评级人用户 ID")
    private Long ratingUserId;

    @Schema(description = "评级人")
    private String ratingUserName;

    @Schema(description = "评级依据")
    private String ratingBasis;

    @Schema(description = "评级结果")
    private String ratingResult;

    @Schema(description = "整改建议")
    private String rectifyAdvice;

    @Schema(description = "整改期限")
    private LocalDate rectifyDeadline;

    @Schema(description = "附件")
    private List<String> attachments;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
