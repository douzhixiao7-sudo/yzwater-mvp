package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 市级防汛抢险队伍列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 市级防汛抢险队伍列表 Response VO")
public class FxQxdwListRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "单位")
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
}
