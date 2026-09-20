package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "IoT - 巡检标准记录项模板 Response VO")
@Data
public class IotInspectionStandardItemRecordRespVO {

    @Schema(description = "记录项 ID")
    private Long id;

    @Schema(description = "属性名称")
    private String attrName;

    @Schema(description = "单位")
    private String attrUnit;

    @Schema(description = "值类型")
    private String valueType;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "是否必填")
    private Integer requiredFlag;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "排序")
    private Integer sort;

}
