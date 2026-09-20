package com.sydigit.yzwater.module.iot.controller.admin.deviceaccident.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 设备事故 Response VO")
@Data
public class IotDeviceAccidentRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "事故发生时间")
    private LocalDateTime accidentTime;

    @Schema(description = "事故地点")
    private String accidentLocation;

    @Schema(description = "事故类型")
    private String accidentType;

    @Schema(description = "事故描述")
    private String accidentDesc;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "损失评估")
    private String lossAssessment;

    @Schema(description = "责任人用户 ID")
    private Long responsibleUserId;

    @Schema(description = "责任人")
    private String responsibleName;

    @Schema(description = "附件")
    private List<String> attachments;

    @Schema(description = "登记人")
    private String creator;

    @Schema(description = "登记人姓名")
    private String creatorName;

    @Schema(description = "登记时间")
    private LocalDateTime createTime;

    @Schema(description = "修改人")
    private String updater;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}