package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 防汛责任人批量保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 防汛责任人批量保存 Request VO")
public class FxZrrBatchSaveReqVO {

    @Schema(description = "类型：1 市级 2 园区", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "类型不能为空")
    private String type;

    @Schema(description = "责任人列表")
    @Valid
    private List<FxZrrBatchSaveReqVO.Item> items;

    @Data
    @Schema(description = "防汛责任人列表项")
    public static class Item {

        @Schema(description = "主键 ID（编辑时必填）")
        private String id;

        @Schema(description = "区划代码（园区必填）")
        private String divisionCode;

        @Schema(description = "行政姓名")
        private String administrativeName;

        @Schema(description = "行政职务")
        private String administrativeTitle;

        @Schema(description = "技术姓名")
        private String technicalName;

        @Schema(description = "技术职务")
        private String technicalTitle;

        @Schema(description = "排序号")
        private Integer sort;
    }
}
