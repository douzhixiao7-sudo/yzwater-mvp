package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "IoT - 巡检检查结果等级配置 VO")
@Data
public class IotInspectionCheckResultConfigVO {

    @Schema(description = "检查结果值")
    private String value;

    @Schema(description = "检查结果名称")
    private String label;

    @Schema(description = "检查结果描述")
    private String remark;

}
