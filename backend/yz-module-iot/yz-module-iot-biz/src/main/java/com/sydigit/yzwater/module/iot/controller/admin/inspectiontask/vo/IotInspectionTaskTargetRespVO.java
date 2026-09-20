package com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "IoT - 巡检任务对象 Response VO")
@Data
public class IotInspectionTaskTargetRespVO {

    @Schema(description = "对象明细 ID")
    private Long id;

    @Schema(description = "对象排序")
    private Integer targetSort;

    @Schema(description = "对象类型（1 设备，2 位置）")
    private Integer targetType;

    @Schema(description = "对象业务 ID")
    private Long targetId;

    @Schema(description = "设备 ID")
    private Long deviceId;

    @Schema(description = "位置 ID")
    private Long locationId;

    @Schema(description = "对象名称")
    private String targetName;

    @Schema(description = "所属站点")
    private String stationId;

}
