package com.sydigit.yzwater.module.iot.controller.admin.runlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 运行日志巡检结果记录项
 */
@Schema(description = "IoT - 运行日志巡检结果记录项")
@Data
public class IotRunLogInspectionResultRecordVO {

    @Schema(description = "属性名")
    private String attrName;

    @Schema(description = "属性单位")
    private String attrUnit;

    @Schema(description = "标准值")
    private String standardValue;

    @Schema(description = "实际值")
    private String actualValue;
}

