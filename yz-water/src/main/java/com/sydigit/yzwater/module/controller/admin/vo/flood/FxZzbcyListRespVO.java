package com.sydigit.yzwater.module.controller.admin.vo.flood;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 防汛抗旱组织部成员列表 Response VO
 */
@Data
@Schema(description = "管理后台 - 防汛抗旱组织部成员列表 Response VO")
public class FxZzbcyListRespVO {

    @Schema(description = "主键 ID")
    private String id;

    @Schema(description = "岗位（字典值，zd_zzbgw）")
    private String position;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "职务")
    private String title;
}
