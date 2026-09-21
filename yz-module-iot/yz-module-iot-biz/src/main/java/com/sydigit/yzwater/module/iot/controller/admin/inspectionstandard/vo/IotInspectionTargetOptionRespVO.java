package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "IoT - 巡检标准适用对象下拉 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IotInspectionTargetOptionRespVO {

    @Schema(description = "对象 ID")
    private Long id;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象类型")
    private String targetType;

    @Schema(description = "所属站点（仅 device 类型返回）")
    private String stationId;

}
