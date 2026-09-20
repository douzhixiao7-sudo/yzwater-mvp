package com.sydigit.yzwater.module.iot.controller.admin.inspectiontask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "IoT - 巡检任务提交结果记录项明细 VO")
@Data
public class IotInspectionTaskSubmitResultRecordVO {

    @Schema(description = "属性名称", example = "压力值")
    private String attrName;

    @Schema(description = "属性单位", example = "kPa")
    private String attrUnit;

    @Schema(description = "标准值", example = "10-20")
    private String standardValue;

    @Schema(description = "实际测量值", example = "18")
    private String actualValue;
}
