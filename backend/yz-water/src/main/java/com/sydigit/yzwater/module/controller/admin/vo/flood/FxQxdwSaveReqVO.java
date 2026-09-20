package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理后台 - 市级防汛抢险队伍保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 市级防汛抢险队伍保存 Request VO")
public class FxQxdwSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private String id;

    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "单位不能为空")
    private String unitName;

    @Schema(description = "队伍名称")
    private String teamName;

    @Schema(description = "人数")
    private Integer planCount;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "排序号")
    private Integer sort;
}
