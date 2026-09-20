package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - IoT 大屏水库汇总 Response VO")
@Data
public class IotScreenReservoirSummaryRespVO {

    @Schema(description = "总库容（totalCapacity，保留两位小数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345.68")
    private BigDecimal totalCapacity;

    @Schema(description = "总集水面积（catchmentArea，保留两位小数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "789.12")
    private BigDecimal catchmentArea;

    @Schema(description = "总灌溉面积（irrigationArea，保留两位小数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "4567.89")
    private BigDecimal irrigationArea;
}
