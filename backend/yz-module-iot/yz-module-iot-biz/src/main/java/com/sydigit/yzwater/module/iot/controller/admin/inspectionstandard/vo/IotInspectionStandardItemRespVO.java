package com.sydigit.yzwater.module.iot.controller.admin.inspectionstandard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "IoT - 巡检标准检查项 Response VO")
@Data
public class IotInspectionStandardItemRespVO {

    @Schema(description = "检查项 ID")
    private Long id;

    @Schema(description = "检查项名称")
    private String itemName;

    @Schema(description = "检查描述")
    private String itemDesc;

    @Schema(description = "合格规则")
    private String qualifiedRule;

    @Schema(description = "是否需要上传附件")
    private Integer needUploadAttachment;

    @Schema(description = "默认检查结果")
    private String defaultResult;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "记录项数量")
    private Integer recordCount;

    @Schema(description = "记录项模板")
    private List<IotInspectionStandardItemRecordRespVO> recordTemplates;

}
