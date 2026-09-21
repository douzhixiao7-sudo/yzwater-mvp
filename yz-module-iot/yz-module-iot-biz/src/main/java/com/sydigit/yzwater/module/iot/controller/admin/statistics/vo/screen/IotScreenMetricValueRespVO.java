package com.sydigit.yzwater.module.iot.controller.admin.statistics.vo.screen;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 大屏统计指标值 Response VO")
@Data
public class IotScreenMetricValueRespVO {

    @Schema(description = "属性名称", example = "A相电流")
    private String propertyName;

    @Schema(description = "物模型属性标识符", example = "i_a")
    private String identifier;

    @Schema(description = "属性值", example = "0.12")
    private Object value;

}
