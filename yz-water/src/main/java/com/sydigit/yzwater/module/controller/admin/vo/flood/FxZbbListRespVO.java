package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 值班表列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 值班表列表 Response VO")
public class FxZbbListRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "值班说明")
    private String description;

    @Schema(description = "值班明细")
    private List<FxZbbItemRespVO> items;
}
