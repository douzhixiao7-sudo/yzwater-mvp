package com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "IoT - 养护计划 Response VO")
@Data
public class IotMaintenancePlanRespVO {

    @Schema(description = "主键 ID", example = "1024")
    private Long id;

    @Schema(description = "设备 ID", example = "1024")
    private Long deviceId;

    @Schema(description = "设备名称", example = "闸门电机")
    private String deviceName;

    @Schema(description = "设备类型", example = "1")
    private String deviceType;

    @Schema(description = "所属闸站字典值", example = "1001")
    private String stationId;

    @Schema(description = "计划养护日期", example = "2026-02-03")
    private LocalDate planDate;

    @Schema(description = "养护类型", example = "periodic")
    private String maintainType;

    @Schema(description = "养护项目")
    private String maintainItems;

    @Schema(description = "备件消耗列表")
    private List<IotMaintenancePlanSpareUsageVO> spareUsages;

    @Schema(description = "养护人")
    private String maintainerName;

    @Schema(description = "完成日期")
    private LocalDateTime finishTime;

    @Schema(description = "养护状态", example = "pending")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "数据来源", example = "auto")
    private String sourceType;
    @Schema(description = "\u521b\u5efa\u65f6\u95f4")
    private LocalDateTime createTime;


    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
