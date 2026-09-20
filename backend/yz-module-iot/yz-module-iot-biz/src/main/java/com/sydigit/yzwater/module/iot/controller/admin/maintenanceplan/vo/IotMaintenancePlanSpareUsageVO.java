package com.sydigit.yzwater.module.iot.controller.admin.maintenanceplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "IoT - 养护计划备件消耗 VO")
@Data
public class IotMaintenancePlanSpareUsageVO {

    @Schema(description = "备件 ID", example = "1024")
    private Long spareId;

    @Schema(description = "消耗数量", example = "1")
    private Integer qty;
}
