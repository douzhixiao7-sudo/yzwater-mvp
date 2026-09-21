package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 大屏水库容量列表 Response VO")
@Data
public class IotScreenReservoirCapacityRespVO {

    @Schema(description = "水库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "龙河水库")
    private String reservoirName;

    @Schema(description = "水库容量(totalCapacity)", requiredMode = Schema.RequiredMode.REQUIRED, example = "123.45")
    private BigDecimal totalCapacity;
}
