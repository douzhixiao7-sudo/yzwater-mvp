package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 预案管理列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 预案管理列表 Response VO")
public class FxYaglListRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "预案名称")
    private String name;

    @Schema(description = "文件 URL 数组")
    private List<String> files;

    @Schema(description = "排序号")
    private Integer sort;
}
