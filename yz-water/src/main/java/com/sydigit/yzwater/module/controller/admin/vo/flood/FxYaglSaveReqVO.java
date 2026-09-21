package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 预案管理保存 Request VO
 */
@Data
@Schema(description = "管理后台 - 预案管理保存 Request VO")
public class FxYaglSaveReqVO {

    @Schema(description = "主键 ID（编辑时必填）")
    private String id;

    @Schema(description = "预案名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "预案名称不能为空")
    private String name;

    @Schema(description = "文件 URL 数组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "请至少上传一个文件")
    private List<String> files;

    @Schema(description = "排序号")
    private Integer sort;
}
