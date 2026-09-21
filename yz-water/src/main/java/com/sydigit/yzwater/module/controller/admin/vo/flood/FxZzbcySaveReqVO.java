package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理后台 - 防汛抗旱组织部成员保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 防汛抗旱组织部成员保存 Request VO")
public class FxZzbcySaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private String id;

    @Schema(description = "岗位（字典值，zd_zzbgw）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "岗位不能为空")
    private String position;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "职务")
    private String title;

    @Schema(description = "排序号")
    private Integer sort;
}
