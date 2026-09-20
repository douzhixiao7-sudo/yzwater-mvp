package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 防汛责任人列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 防汛责任人列表 Response VO")
public class FxZrrListRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "类型：1 市级 2 园区")
    private String type;

    @Schema(description = "区划代码")
    private String divisionCode;

    @Schema(description = "行政姓名")
    private String administrativeName;

    @Schema(description = "行政职务")
    private String administrativeTitle;

    @Schema(description = "技术姓名")
    private String technicalName;

    @Schema(description = "技术职务")
    private String technicalTitle;
}
