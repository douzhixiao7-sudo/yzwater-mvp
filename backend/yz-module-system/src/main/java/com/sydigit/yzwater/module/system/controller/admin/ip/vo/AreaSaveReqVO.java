package com.sydigit.yzwater.module.system.controller.admin.ip.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 行政区划新增/修改 Request VO")
@Data
public class AreaSaveReqVO {

    @Schema(description = "行政区划编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "321081")
    @NotNull(message = "行政区划编码不能为空")
    private Long id;

    @Schema(description = "父级行政区划编码（0 表示根节点）", example = "1")
    private Long parentId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "仪征市")
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "类型（对应 Area.type）", example = "0")
    private Integer type;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "空间信息（WKT，SRID=4490）", example = "POINT(119.2 32.3)")
    private String gemo;
}
