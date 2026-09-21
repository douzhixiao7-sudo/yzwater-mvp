package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import com.sydigit.yzwater.framework.common.validation.InEnum;
import com.sydigit.yzwater.module.iot.enums.inspection.IotInspectionCheckResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Schema(description = "IoT - 巡检标准检查项新增/修改 Request VO")
@Data
public class IotInspectionStandardItemSaveReqVO {

    @Schema(description = "检查项 ID", example = "1024")
    private Long id;

    @Schema(description = "检查项名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "检查项名称不能为空")
    private String itemName;

    @Schema(description = "检查描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "检查描述不能为空")
    private String itemDesc;

    @Schema(description = "合格规则", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "合格规则不能为空")
    private String qualifiedRule;

    @Schema(description = "是否需要上传附件（0 否，1 是）")
    private Integer needUploadAttachment;

    @Schema(description = "默认检查结果（优秀/良好/合格/不合格）")
    @InEnum(value = IotInspectionCheckResultEnum.class, message = "检查结果必须是 {value}")
    private String defaultResult;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "记录项模板")
    @Valid
    private List<IotInspectionStandardItemRecordSaveReqVO> recordTemplates;

}
