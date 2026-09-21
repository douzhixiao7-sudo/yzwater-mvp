package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "IoT - 巡检标准记录项模板新增/修改 Request VO")
@Data
public class IotInspectionStandardItemRecordSaveReqVO {

    @Schema(description = "记录项 ID", example = "1024")
    private Long id;

    @Schema(description = "属性名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "压力")
    @NotBlank(message = "属性名称不能为空")
    private String attrName;

    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "kPa")
    @NotBlank(message = "单位不能为空")
    private String attrUnit;

    @Schema(description = "值类型（TEXT/NUMBER/SELECT）", example = "TEXT")
    private String valueType;

    @Schema(description = "具体数值", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotBlank(message = "具体数值不能为空")
    private String defaultValue;

    @Schema(description = "是否必填（0 否，1 是）", example = "1")
    private Integer requiredFlag;

    @Schema(description = "备注说明", example = "请拍照留证")
    private String remark;

    @Schema(description = "排序", example = "1")
    private Integer sort;

}
