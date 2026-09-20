package com.sydigit.yzwater.module.iot.controller.admin.faultrepair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 故障维修工单 Response VO")
@Data
public class IotFaultRepairRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "设备名称", example = "闸门电机")
    private String deviceName;

    @Schema(description = "工单编号")
    private String orderNo;

    @Schema(description = "设备类型", example = "泵站设备")
    private String deviceType;

    @Schema(description = "故障类型")
    private String faultType;

    @Schema(description = "故障时间")
    private LocalDateTime faultTime;

    @Schema(description = "故障现象")
    private String faultSymptom;

    @Schema(description = "上报人姓名")
    private String reporterName;

    @Schema(description = "上报人用户ID")
    private Long reporterUserId;

    @Schema(description = "维修人姓名")
    private String repairName;

    @Schema(description = "维修人用户ID")
    private Long repairUserId;

    @Schema(description = "处理状态")
    private String status;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "计划完成时间")
    private LocalDateTime planFinishTime;

    @Schema(description = "故障图片")
    private List<String> faultImages;

    @Schema(description = "备件消耗列表")
    private List<IotFaultRepairSpareUsageVO> spareUsages;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
